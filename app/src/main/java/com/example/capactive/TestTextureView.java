package com.example.capactive;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;

import androidx.annotation.NonNull;

public class TestTextureView extends TextureView implements TextureView.SurfaceTextureListener {
    private DrawThread drawThread;
    public static Paint paint1, paint2, paintp;//定义画笔
    private static Canvas canvas1, canvas2;//画布
    private static Bitmap bitmap1, bitmap2, bitmapSave;//位图(画板),用于置放画布，画布内容在此呈现
    private float startX, startY, endX, endY, startPress, endPress, changePress;
    private int pointCount;
    private Path movePath;
    public static boolean isNeedpress, threeFingers, isTouch, isHover;
    public MotionEvent eventTouch, eventHover;
    private CustomListen.ListenerTouch mlistenerTouch;//在子对象中，为监听器的实现定义实例变量
    private CustomListen.ListenerHover mlistenerHover;
    private CustomListen.ListenerThreeFingers mlistenerThreeFingers;
    public TestTextureView(@NonNull Context context) {
        super(context);
        init();
    }

    public TestTextureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    public TestTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init(){
        setSurfaceTextureListener(this);
        setOpaque(false);
        paint1 = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);//创建画笔1,画轨迹。抗锯齿，抗抖动。
        paintp = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);//创建画笔p,压感状态下画轨迹。抗锯齿，抗抖动。
        paint2 = new Paint();//创建画笔2,画光标
        paint1.setColor(Color.parseColor("#FF0000"));//画笔默认颜色红色
        paint2.setColor(Color.argb(255,0,0,0));//画笔默认颜色黑色
        paint1.setStrokeWidth(8);//画笔默认尺寸中等
        paint2.setStrokeWidth(1);//画笔默认尺寸
        paint1.setStrokeJoin(Paint.Join.ROUND);//连接的外边缘以圆弧的方式相交
        paintp.setStrokeJoin(Paint.Join.ROUND);//连接的外边缘以圆弧的方式相交
        paint1.setFilterBitmap(true);//bitmap抗锯齿
        paintp.setFilterBitmap(true);//bitmap抗锯齿
        paint1.setStrokeCap(Paint.Cap.ROUND);//线条结束处绘制一个半圆
        paintp.setStrokeCap(Paint.Cap.ROUND);//线条结束处绘制一个半圆
        paint2.setStrokeCap(Paint.Cap.ROUND);
        paint1.setStyle(Paint.Style.STROKE);//绘画样式，描边
        paintp.setStyle(Paint.Style.STROKE);//绘画样式，描边
        paint2.setStyle(Paint.Style.STROKE);//绘画样式，描边
    }


    @Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
        drawThread = new DrawThread(surface, width, height);
        drawThread.start();
        isHover = false;
        isTouch = false;
        threeFingers = false;
            bitmap1 = Bitmap.createBitmap(this.getWidth(), this.getHeight(), Bitmap.Config.ARGB_8888);//配置位图1,画轨迹
            bitmap2 = Bitmap.createBitmap(this.getWidth(), this.getHeight(), Bitmap.Config.ARGB_8888);//配置位图2,画光标
            canvas1 = new Canvas(bitmap1);//创建画布1
            canvas2 = new Canvas(bitmap2);//创建画布2
            canvas1.drawColor(Color.argb(0, 255, 255, 255));//设置画布背景颜色为白色，全透明
            canvas2.drawColor(Color.argb(0, 255, 255, 255));//设置画布背景颜色为白色，全透明
            canvas1.drawBitmap(bitmap1, new Matrix(), paint1);//使用指定的矩阵绘制位图
            canvas2.drawBitmap(bitmap2, new Matrix(), paint2);
            canvas1.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG));//画布抗锯齿
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        eventTouch = event;

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN://第一根手指点击屏幕
                isTouch = true;
                Log.i("Down", "ACTION_DOWN");
                //记录开始触摸的点的坐标
                startX = event.getX();
                startY =  event.getY();
                if (movePath == null) {
                    movePath = new Path();
                }
                movePath.moveTo(startX,startY);//绘制线的起点坐标,如果不指定的话,系统默认是在原点(0,0)开始
                startPress = event.getPressure();//记录起始点压力
                drawThread.resumeThread();
                onListenTouch();
                break;
            case MotionEvent.ACTION_POINTER_DOWN://三根手指同时触屏(大于三根手指时也会触发)
                if (eventTouch.getPointerCount() == 3) {
                    threeFingers = true;
                    canvas1.drawColor(0, PorterDuff.Mode.CLEAR);//清屏，清除触摸行为产生的轨迹数据，本次触摸行为前的位图数据也将一并清除
                    if (bitmapSave != null) {
                        //将之前留存的位图数据赋给bitmap1,使得只有本次触摸行为的数据被清除.若屏幕转换后最先执行进入全屏模式操作，此时bitmapSave保留的宽高仍为屏幕转换前的，所以得先通过合并改变位图宽高使其与当前View宽高一致
                        bitmap1 = mergeBitmap(Bitmap.createBitmap(this.getWidth(), this.getHeight(), Bitmap.Config.ARGB_8888), bitmapSave);
                    }
                    canvas1 = new Canvas(bitmap1);
                    canvas1.drawColor(Color.argb(0, 255, 255, 255));
                    canvas1.drawBitmap(bitmap1, new Matrix(), paint1);
                    Log.i("DianJi", "三指");
                    onListenTouch();
                }
                break;
            case MotionEvent.ACTION_MOVE://手指滑动
                if ( !threeFingers ) {
                    Log.i("Move", event.getEventTime()+"ACTION_MOVE");
                    canvas2.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);//清除十字架
                    //记录移动位置的点的坐标
                    endX = event.getX();
                    endY = event.getY();
                    endPress = event.getPressure();
                    movePath.quadTo(startX, startY, (startX + endX) / 2, (startY + endY) / 2);//这里终点设为两点的中心点的目的在于使绘制的曲线更平滑
                    PathMeasure pathMeasure = new PathMeasure(movePath, false);
                    pointCount = (int)(pathMeasure.getLength() * 4);//将要绘制的插入点的数量
                    float[] position = new float[2];
                    if ( !isNeedpress ) {//判断是否进入压感状态
                        canvas1.drawPath(movePath, paint1);//根据两点坐标绘制连线
                    } else {//进入压感状态
                        paintp.setColor(paint1.getColor());
                        changePress = (startPress  - endPress) / pointCount;//插入的每个点平均的压力改变值
                        for (int i = 1; i <= pointCount; i++) {
                            if ( pathMeasure.getPosTan((float) (0.25 * i), position, new float[2]) ) {
                                paintp.setStrokeWidth((startPress - (changePress * i)) * 20);//为使插入的每点压力均匀变化，用起始点压力减去当前绘制点压力改变值
                                canvas1.drawPoint(position[0], position[1], paintp);
                            } else {//防止因pathMeasure.getPosTan获取失败造成有点没绘制
                                i--;
                            }
                        }
                    }
                    movePath.reset();//清除路径中的所有直线和曲线，使其变空
                    movePath.moveTo((startX + endX)/2, (startY + endY)/2);
                    //更新起始点的位置
                    startX = event.getX();
                    startY = event.getY();
                    startPress = endPress;//更新起始点的压力
                    canvas2.drawLine(startX, (startY + 15), startX, (startY - 15), paint2);
                    canvas2.drawLine((startX + 15), startY, (startX - 15), startY, paint2);
                    onListenTouch();
                }
                break;
            case MotionEvent.ACTION_UP://最后一根手指离开屏幕
                isTouch = false;
                movePath.reset();
                bitmapSave = bitmap1.copy(Bitmap.Config.ARGB_8888, true);
                canvas2.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);//清除十字架
                threeFingers = false;
                Log.i("UP", event.getEventTime()+"ACTION_UP");
                onListenTouch();
                try {//防止因线程暂停而来不及绘制UP中的操作
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                drawThread.pauseThread();
                break;
        }

//        onListenTouch();实际应用时发现三指点击有时不起效果，查看日志后发现因为每次Touch事件都会执行该方法，造成MainActivity中会多次执行为True的isThreeFingers
        return true;
    }

    @Override
    public boolean onHoverEvent(MotionEvent event) {
        eventHover = event;

        switch (event.getAction()) {
            case MotionEvent.ACTION_HOVER_ENTER://笔进入view范围
                isHover = true;
                Log.i("Hover-IN", "ACTION_HOVER_ENTER");
                canvas2.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);//清除十字架
                startX = (int) (event.getX());
                startY = (int) (event.getY());
                canvas2.drawLine(startX, (startY + 15), startX, (startY - 15), paint2);
                canvas2.drawLine((startX + 15), startY, (startX - 15), startY, paint2);
                drawThread.resumeThread();
                break;
            case MotionEvent.ACTION_HOVER_MOVE:
                Log.i("Hover-MOVE", "ACTION_HOVER_MOVE");
                canvas2.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);//清除十字架
                startX = (int) (event.getX());
                startY = (int) (event.getY());
                canvas2.drawLine(startX, (startY + 15), startX, (startY - 15), paint2);
                canvas2.drawLine((startX + 15), startY, (startX - 15), startY, paint2);
                break;
            case MotionEvent.ACTION_HOVER_EXIT:
                isHover = false;
                canvas2.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                Log.i("Hover-UP", "ACTION_HOVER_UP");
                try {//防止因线程暂停而来不及绘制UP中的操作
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                drawThread.pauseThread();
                break;
        }
        onListenHover();
        return false;
    }


    @Override
    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
        drawThread.resumeThread();
        onListenTouch();//当用手指在底部进行退出全屏、将应用转至后台操作时，手指可能因触碰而在画布上留下轨迹，但此时日志显示未进行MOVE_UP事件，
        onListenHover();//则监听的回调方法判断具体实现的布尔值仍将为True，将不会执行清除，在activity重回前台时坐标等参数将残留在画布上，
        int W = width;
        int H = height;//屏幕转换后宽高互换
        Log.i("WWWWW", String.valueOf(W));
        Log.i("HHHHH", String.valueOf(H));
        if ( H < W ) {
            bitmap1 = mergeBitmap(Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888), bitmap1);
            bitmap2 = mergeBitmap(Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888), bitmap2);
            canvas1 = new Canvas(bitmap1);
            canvas2 = new Canvas(bitmap2);
            canvas1.drawColor(Color.argb(0, 255, 255, 255));
            canvas2.drawColor(Color.argb(0, 255, 255, 255));
            canvas1.drawBitmap(bitmap1, new Matrix(), paint1);
            canvas2.drawBitmap(bitmap2, new Matrix(), paint2);
        } else {
            bitmap1 = mergeBitmap(Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888), bitmap1);
            bitmap2 = mergeBitmap(Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888), bitmap2);
            canvas1 = new Canvas(bitmap1);
            canvas2 = new Canvas(bitmap2);
            canvas1.drawColor(Color.argb(0, 255, 255, 255));
            canvas2.drawColor(Color.argb(0, 255, 255, 255));
            canvas1.drawBitmap(bitmap1, new Matrix(), paint1);
            canvas2.drawBitmap(bitmap2, new Matrix(), paint2);
        }
        drawThread.pauseThread();
    }
    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
        if(drawThread != null){
            drawThread.stopDrawing();
            surface.release();
        }
        return true;
    }
    @Override
    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {   }


    //清屏
    public void clean() {
        drawThread.resumeThread();
        canvas1.drawColor(0, PorterDuff.Mode.CLEAR);//清屏,PorterDuff.Mode.CLEAR:源覆盖的目标像素被清除为 0。
        bitmap1 = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        canvas1 = new Canvas(bitmap1);
        canvas1.drawColor(Color.argb(0, 255, 255, 255));
        canvas1.drawBitmap(bitmap1, new Matrix(), paint1);
        bitmapSave = bitmap1;//将保留数据的位图重新赋值，防止随后被全屏事件恢复已清除的位图数据。
        drawThread.pauseThread();
    }

    //合并位图
    public static Bitmap mergeBitmap(Bitmap backBitmap, Bitmap frontBitmap) {
        if (backBitmap == null || backBitmap.isRecycled()
                || frontBitmap == null || frontBitmap.isRecycled()) {
            Log.e(TAG, "backBitmap=" + backBitmap + ";frontBitmap=" + frontBitmap);
            return null;
        }
        Bitmap bitmap = backBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bitmap);
        Rect baseRect = new Rect(0, 0, backBitmap.getWidth(), backBitmap.getHeight());
        Rect frontRect = new Rect(0, 0, frontBitmap.getWidth(), frontBitmap.getHeight());
        canvas.drawBitmap(frontBitmap, frontRect, baseRect, null);
        return bitmap;
    }

    //硬件加速
    @Override
    public boolean isHardwareAccelerated() {
        return true;
    }


    //创建set方法，允许父对象中定义监听器回调
    public void setListener(CustomListen.ListenerTouch listenerTouch, CustomListen.ListenerHover listenerHover, CustomListen.ListenerThreeFingers listenerThreeFingers){
        mlistenerTouch = listenerTouch;
        mlistenerHover = listenerHover;
        mlistenerThreeFingers = listenerThreeFingers;
    }
    //该方法在Touch事件中触发ListenerTouch和ListenerThreeFingers监听器中的方法
    public void onListenTouch(){
        if (mlistenerTouch != null ) {
            mlistenerTouch.isTouch();
        }
        if (mlistenerThreeFingers != null) {
            mlistenerThreeFingers.isThreeFingers();
        }
    }
    //该方法在Hover事件中触发ListenerHover监听器中的方法
    public void onListenHover(){
        if (mlistenerHover != null) {
            mlistenerHover.isHover();
        }
    }


    //创建一个子线程用于进行绘制操作
    private static class DrawThread extends Thread{
        private final Surface surface;
        private volatile boolean running = true;
        private boolean isPaused;

        public DrawThread(SurfaceTexture surfaceTexture, int width, int height){
            this.surface = new Surface(surfaceTexture);
        }

        @Override
        public void run() {
            while (running){
                if ( !isPaused ) {
                    Log.i("DRAW", "draw");
                    Canvas canvas = surface.lockCanvas(null);
                    canvas.drawColor(0,PorterDuff.Mode.CLEAR);
                    canvas.drawBitmap(mergeBitmap(bitmap1,bitmap2),0,0, paint1);//将位图中保存的绘制数据画到TextureView上
                    surface.unlockCanvasAndPost(canvas);
                }
            }
        }

        public void pauseThread(){
            Log.i("Pause", "pause");
            isPaused = true;
        }

        public void resumeThread(){
            Log.i("RES", "resume");
            isPaused = false;
        }

        public void stopDrawing(){
            running = false;
        }


    }

}
