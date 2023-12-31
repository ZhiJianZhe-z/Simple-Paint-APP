package com.example.capactive;

/**
 * 第六次修改
 * 隐藏了顶部状态栏和底部导航栏，使APP界面全屏显示
 * 在主题中加入<item name="android:windowBackground">@android:color/transparent</item>，功能是移除默认的Window背景
 * 效果：GPU过度绘制分析中可以看到所有布局少了一次覆盖和绘制
 * */

import static android.view.MotionEvent.AXIS_TILT;
import static android.view.MotionEvent.AXIS_X;
import static android.view.MotionEvent.AXIS_Y;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


public class OldTwo extends Activity {
    private Button btn1, btn2, btnr, btng, btnb, small, mid, big, clean;
    private TextView t1, t2, t3, t4, t5, t6;
    private View smallback,midback;
    private static Paint paint1, paint2, paintp;//定义画笔
    private static Canvas canvas1, canvas2;//画布
    private static Bitmap bitmap1, bitmap2, bitmapSave;//位图(画板),用于置放画布，画布内容在此呈现
    private ImageView imageview;//用于将画板和布局联系在一起，从而显示出来
    //    private DrawView view_draw_point;//用于将画板和布局联系在一起，从而显示出来
    private float startX, startY, endX, endY, startPress, endPress, changePress;
    private int pointCount;
    private static Path movePath;
    private static boolean isNeedpress, threeFingers;

    private  RelativeLayout BG, textgroup;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main);
        View decorView = getWindow().getDecorView();
        int option = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(option);

        //使图片延申至刘海屏内，解决隐藏状态栏黑边问题
        if (Build.VERSION.SDK_INT >= 28) {
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            getWindow().setAttributes(params);
        }


        //横竖屏转换,根据不同情况设置不同背景图片及控件位置。
        if (screenLandPort(this) == 0) {
            BG = findViewById(R.id.BG);
            BG.setBackgroundResource(R.drawable.bghori);
            textgroup = findViewById(R.id.textgroup);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(textgroup.getLayoutParams());
            layoutParams.topMargin = (imageview.getHeight() / 4);
            textgroup.setLayoutParams(layoutParams);
        } else {
            BG = findViewById(R.id.BG);
            BG.setBackgroundResource(R.drawable.bgver);
        }


        btn1 = findViewById(R.id.line);
        btn2 = findViewById(R.id.press);
        btnr = findViewById(R.id.red);
        btng = findViewById(R.id.green);
        btnb = findViewById(R.id.blue);
        small = findViewById(R.id.small);
        mid = findViewById(R.id.mid);
        big = findViewById(R.id.big);
        smallback = findViewById(R.id.smallback);
        midback = findViewById(R.id.midback);
        clean = findViewById(R.id.clean);
        imageview = findViewById(R.id.imageview);
        t1 = findViewById(R.id.t1);
        t2 = findViewById(R.id.t2);
        t3 = findViewById(R.id.t3);
        t4 = findViewById(R.id.t4);
        t5 = findViewById(R.id.t5);
        t6 = findViewById(R.id.t6);
        View TOP = findViewById(R.id.TOP);


        btn1.setOnClickListener(v -> initBackground((Button) v));
        btn2.setOnClickListener(v -> initBackground((Button) v));
        btnr.setOnClickListener(c -> initColor((Button) c));
        btng.setOnClickListener(c -> initColor((Button) c));
        btnb.setOnClickListener(c -> initColor((Button) c));
        small.setOnClickListener(s -> initSize((Button) s));
        mid.setOnClickListener(s -> initSize((Button) s));
        big.setOnClickListener(s -> initSize((Button) s));



        Log.i("DianJi", imageview.getWidth() + " " + imageview.getHeight());
        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);//内置屏幕宽高，内置屏高 = 顶部状态栏高 + bitmap（即可绘制区域）高
        Log.i("DianJi",point.x + " " + point.y);

        //创建空白图片和画布，将图片放在画布上
        imageview.post(() -> {
            bitmap1 = Bitmap.createBitmap(imageview.getWidth(), imageview.getHeight(),Bitmap.Config.ARGB_8888);//配置位图1,画轨迹
            bitmap2 = Bitmap.createBitmap(imageview.getWidth(), imageview.getHeight(),Bitmap.Config.ARGB_8888);//配置位图2,画光标
            canvas1 = new Canvas(bitmap1);//创建画布1
            canvas2 = new Canvas(bitmap2);//创建画布2
            canvas1.drawColor(Color.argb(0, 255, 255, 255));//设置画布背景颜色为白色，全透明
            canvas2.drawColor(Color.argb(0, 255, 255, 255));//设置画布背景颜色为白色，全透明
            canvas1.drawBitmap(bitmap1, new Matrix(), paint1);
            canvas2.drawBitmap(bitmap2, new Matrix(), paint2);
            paint1 = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);//创建画笔1,画轨迹。抗锯齿，抗抖动。
            paintp = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);//创建画笔p,压感状态下画轨迹。抗锯齿，抗抖动。
            paint2 = new Paint();//创建画笔2,画光标
            canvas1.drawColor(Color.argb(0, 255, 255, 255));//设置画布背景颜色为白色，全透明
            canvas2.drawColor(Color.argb(0, 255, 255, 255));//设置画布背景颜色为白色，全透明
            canvas1.drawBitmap(bitmap1, new Matrix(), paint1);//使用指定的矩阵绘制位图
            canvas2.drawBitmap(bitmap2, new Matrix(), paint2);
            paint1.setColor(Color.parseColor("#FF0000"));//画笔默认颜色红色
            paint2.setColor(Color.argb(255,0,0,0));//画笔默认颜色黑色
            paint1.setStrokeWidth(8);//画笔默认尺寸中等
            paint2.setStrokeWidth(1);//画笔默认尺寸
            paint1.setStrokeJoin(Paint.Join.ROUND);//连接的外边缘以圆弧的方式相交
            paintp.setStrokeJoin(Paint.Join.ROUND);//连接的外边缘以圆弧的方式相交
//            paint1.setFilterBitmap(true);//bitmap抗锯齿
//            paintp.setFilterBitmap(true);//bitmap抗锯齿
            canvas1.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG));//画布抗锯齿
            paint1.setStrokeCap(Paint.Cap.ROUND);//线条结束处绘制一个半圆
            paintp.setStrokeCap(Paint.Cap.ROUND);//线条结束处绘制一个半圆
            paint1.setStyle(Paint.Style.STROKE);//绘画样式，描边
            paintp.setStyle(Paint.Style.STROKE);//绘画样式，描边
            paint2.setStyle(Paint.Style.STROKE);//绘画样式，描边
            imageview.setImageBitmap(mergeBitmap(bitmap1,bitmap2));//展示出来

        });


        threeFingers = false;
        //注册触摸监听事件，获取按下时的坐标和移动后的坐标，在开始和结束之间画一条直线并更新画布图片
        imageview.setOnTouchListener((v, event) -> {//根据触摸动作执行不同操作

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN://第一根手指点击屏幕
                    Log.i("DianJi", "ACTION_DOWN");
                    startX =  event.getX();
                    startY =  event.getY();
                    if ( movePath == null){
                        movePath = new Path();
                    }
                    movePath.moveTo( startX, startY );
                    break;

                case MotionEvent.ACTION_POINTER_DOWN://三根手指同时触屏(大于三根手指时也会触发)
                    if (event.getPointerCount() == 3) {
                        threeFingers = true;
                        canvas1.drawColor(0, PorterDuff.Mode.CLEAR);//清屏，清除触摸行为产生的轨迹数据，本次触摸行为前的位图数据也将一并清除
                        if (!(bitmapSave == null)){
                            //将之前留存的位图数据赋给bitmap1,使得只有本次触摸行为的数据被清除.若屏幕转换后最先执行进入全屏模式操作，此时bitmapSave保留的宽高仍为屏幕转换前的，所以得先通过合并改变位图宽高使其与当前View宽高一致
                            bitmap1 = mergeBitmap(Bitmap.createBitmap(imageview.getWidth(), imageview.getHeight(), Bitmap.Config.ARGB_8888), bitmapSave);
                        }
                        canvas1 = new Canvas(bitmap1);
                        canvas1.drawColor(Color.argb(0, 255, 255, 255));
                        canvas1.drawBitmap(bitmap1, new Matrix(), paint1);
                        imageview.setImageBitmap(bitmap1);
                        if (TOP.getVisibility() == VISIBLE) {//如果顶部工具栏此时可见，则将它改为不可见
                            TOP.setVisibility(View.INVISIBLE);
                        } else if (TOP.getVisibility() == View.INVISIBLE) {//如果顶部工具栏此时不可见，则将它改为可见
                            TOP.setVisibility(VISIBLE);
                        }
                        Log.i("ThreeFingers", "全屏");
                    }
                    break;

                case MotionEvent.ACTION_MOVE://手指滑动
                    if ( !threeFingers ){//三指隐藏工具栏时在Pointer_Down事件后不执行MOVE
                        t1.setText("X：" + (int) event.getAxisValue(AXIS_X));
                        t2.setText("Y：" + (int) event.getAxisValue(AXIS_Y));
                        t3.setText("压力：" + event.getPressure());
                        t4.setText("倾斜：" + event.getAxisValue(AXIS_TILT));
                        t5.setText("角度：" + (int) (90 - 57.3 * (event.getAxisValue(AXIS_TILT))));
                        t6.setText("触摸点数：" + event.getPointerCount());
                        canvas2.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);//清除十字架
                        //记录移动位置的点的坐标
                        endX = event.getX();
                        endY = event.getY();
                        endPress = event.getPressure();
                        movePath.quadTo(startX, startY, (startX + endX) / 2, (startY + endY) / 2);//这里终点设为两点的中心点的目的在于使绘制的曲线更平滑
                        float[] position = new float[2];
                        if ( !isNeedpress ) {//判断是否进入压感状态
                            canvas1.drawPath(movePath, paint1);//根据两点坐标绘制连线
                        } else {//进入压感状态
                            paintp.setColor(paint1.getColor());
                            PathMeasure pathMeasure = new PathMeasure(movePath, false);
                            pointCount = (int)(pathMeasure.getLength() * 4);//将要绘制的插入点的数量
                            changePress = (startPress  - endPress) / pointCount;//插入的每个点平均的压力改变值
                            for (int i = 1; i <= pointCount; i++) {
                                if ( pathMeasure.getPosTan((float) (0.25 * i), position, new float[2]) ) {
                                    paintp.setStrokeWidth((startPress - (changePress * i)) * 20);//为使插入的每点压力均匀变化，用起始点压力减去当前绘制点压力改变值
                                    canvas1.drawPoint(position[0], position[1], paintp);
                                } else {//防止因pathMeasure.getPosTan获取失败造成有点没绘制
                                    i--;
                                }
                            }
                            movePath.reset();//清除路径中的所有直线和曲线，使其变空
                            movePath.moveTo((startX + endX)/2, (startY + endY)/2);
                        }

                        //更新起始点的位置
                        startX = event.getX();
                        startY = event.getY();
                        startPress = endPress;//更新起始点的压力
                        canvas2.drawLine(startX, (startY + 15), startX, (startY - 15), paint2);
                        canvas2.drawLine((startX + 15), startY, (startX - 15), startY, paint2);
                        imageview.setImageBitmap(mergeBitmap(bitmap1,bitmap2));
                    }
                    break;


                case MotionEvent.ACTION_UP://最后一根手指离开屏幕
                    t1.setText("");
                    t2.setText("");
                    t3.setText("");
                    t4.setText("");
                    t5.setText("");
                    t6.setText("");
                    movePath.reset();
                    bitmapSave = bitmap1.copy(Bitmap.Config.ARGB_8888, true);//每次触摸事件结束，复制一份位图数据另外保存下来
                    canvas2.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);//清除十字架
                    imageview.setImageBitmap(mergeBitmap(bitmap1,bitmap2));
                    threeFingers = false;
                    Log.i("DianJi", "ACTION_UP");
                    break;
            }

//            imageview.invalidate();
            return true;
        });


        imageview.setOnHoverListener((v, event) -> {//监控笔的悬停
            switch (event.getAction()) {
                case MotionEvent.ACTION_HOVER_ENTER://笔进入view范围
                    Log.i("Hover", "ACTION_HOVER_ENTER");
                    t1.setText("X：" + (int) event.getAxisValue(AXIS_X));
                    t2.setText("Y：" + (int) event.getAxisValue(AXIS_Y));
                    canvas2.drawColor(Color.TRANSPARENT,PorterDuff.Mode.CLEAR);//清除十字架
                    imageview.setImageBitmap(mergeBitmap(bitmap1,bitmap2));
                    startX = (int) (event.getX());
                    startY = (int) (event.getY());
                    canvas2.drawLine(startX, (startY + 15),startX, (startY - 15), paint2);
                    canvas2.drawLine((startX + 15), startY,(startX - 15), startY, paint2);
                    imageview.setImageBitmap(mergeBitmap(bitmap1,bitmap2));
                    break;
                case MotionEvent.ACTION_HOVER_MOVE:
                    t1.setText("X：" + (int) event.getAxisValue(AXIS_X));
                    t2.setText("Y：" + (int) event.getAxisValue(AXIS_Y));
                    Log.i("Hover", "ACTION_HOVER_MOVE");
                    canvas2.drawColor(Color.TRANSPARENT,PorterDuff.Mode.CLEAR);//清除十字架
                    startX = (int) (event.getX());
                    startY = (int) (event.getY());
                    canvas2.drawLine(startX, (startY + 15),startX, (startY - 15), paint2);
                    canvas2.drawLine((startX + 15), startY,(startX - 15), startY, paint2);
                    imageview.setImageBitmap(mergeBitmap(bitmap1,bitmap2));
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    t1.setText("");
                    t2.setText("");
                    canvas2.drawColor(Color.TRANSPARENT,PorterDuff.Mode.CLEAR);
                    imageview.setImageBitmap(mergeBitmap(bitmap1,bitmap2));
                    break;

            }
            return false;
        });


        //清屏，重新生成一块画布
        clean.setOnClickListener(v -> {
            canvas1.drawColor(0, PorterDuff.Mode.CLEAR);//清屏
            bitmap1 = Bitmap.createBitmap(imageview.getWidth(), imageview.getHeight(), Bitmap.Config.ARGB_8888);
            canvas1 = new Canvas(bitmap1);
            canvas1.drawColor(Color.argb(0, 255, 255, 255));
            canvas1.drawBitmap(bitmap1, new Matrix(), paint1);
            bitmapSave = bitmap1;//将保留的位图数据重新赋值，防止随后被全屏事件恢复已清除的位图数据。
            imageview.setImageBitmap(bitmap1);
        });


    }


    //选择了直线还是压感
    @SuppressLint("UseCompatLoadingForDrawables")
    private void initBackground(Button v) {
        if (v == btn2) {
            isNeedpress = true;
            v.setBackground(this.getResources().getDrawable(R.drawable.presson));
            btn1.setBackground(this.getResources().getDrawable(R.drawable.penoff));
        }else if(v == btn1){
            isNeedpress = false;
            v.setBackground(this.getResources().getDrawable(R.drawable.penon));
            btn2.setBackground(this.getResources().getDrawable(R.drawable.pressoff));
            paint1.setStrokeWidth(8);
        }
    }

    //选择了何种颜色按钮
    @SuppressLint("UseCompatLoadingForDrawables")
    private void initColor(Button c) {
        btng.setBackground(this.getResources().getDrawable(R.drawable.greenoff));
        btnb.setBackground(this.getResources().getDrawable(R.drawable.blueoff));
        if (c == btnr) {
            c.setBackground(this.getResources().getDrawable(R.drawable.redon));
            paint1.setColor(c.getTextColors().getDefaultColor());
        }else if (c == btng) {
            c.setBackground(this.getResources().getDrawable(R.drawable.greenon));
            btnr.setBackground(this.getResources().getDrawable(R.drawable.redoff));
            paint1.setColor(c.getTextColors().getDefaultColor());
        }else if (c == btnb) {
            c.setBackground(this.getResources().getDrawable(R.drawable.blueon));
            btnr.setBackground(this.getResources().getDrawable(R.drawable.redoff));
            paint1.setColor(c.getTextColors().getDefaultColor());
        }
    }

    //选择了什么尺寸的笔
    @SuppressLint("UseCompatLoadingForDrawables")
    private void initSize(Button s) {
        small.setBackground(this.getResources().getDrawable(R.drawable.smalloff));
        smallback.setBackground(this.getResources().getDrawable(R.drawable.linetypeoff));
        mid.setBackground(this.getResources().getDrawable(R.drawable.midoff));
        midback.setBackground(this.getResources().getDrawable(R.drawable.linetypeoff));
        big.setBackground(this.getResources().getDrawable(R.drawable.bigoff));
        if (s == small) {
            paint1.setStrokeWidth(2);
            small.setBackground(this.getResources().getDrawable(R.drawable.smallon));
            smallback.setBackground(this.getResources().getDrawable(R.drawable.linetypeon));
        } else if (s == mid) {
            paint1.setStrokeWidth(8);
            mid.setBackground(this.getResources().getDrawable(R.drawable.midon));
            midback.setBackground(this.getResources().getDrawable(R.drawable.linetypeon));
        } else if (s == big) {
            paint1.setStrokeWidth(20);
            big.setBackground(this.getResources().getDrawable(R.drawable.bigon));
        }
    }



    //合并位图
    public static Bitmap mergeBitmap(Bitmap backBitmap, Bitmap frontBitmap) {
        if (backBitmap == null || backBitmap.isRecycled()
                || frontBitmap == null || frontBitmap.isRecycled()) {
            return null;
        }
        Bitmap bitmap = backBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bitmap);
        Rect baseRect = new Rect(0, 0, backBitmap.getWidth(), backBitmap.getHeight());
        Rect frontRect = new Rect(0, 0, frontBitmap.getWidth(), frontBitmap.getHeight());
        canvas.drawBitmap(frontBitmap, frontRect, baseRect, null);
        return bitmap;
    }


    //实现横竖屏切换
    public  int screenLandPort(Activity activity) {
        WindowManager windowManager = activity.getWindowManager();
        Display display= windowManager.getDefaultDisplay();
        int screenWidth = display.getWidth();
        int screenHeight = display.getHeight();
        //return  0:横屏；1:竖屏
        return screenWidth < screenHeight ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
    }


    //横竖屏切换不执行onCreate时,会自动调用此方法
    @Override
    public void onConfigurationChanged( Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int W = imageview.getHeight();//屏幕旋转后宽高互换
        int H = imageview.getWidth();
        Log.i("WWWWW", String.valueOf(W));
        Log.i("HHHHH", String.valueOf(H));
        BG = findViewById(R.id.BG);
        textgroup = findViewById(R.id.textgroup);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(textgroup.getLayoutParams());
        if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            bitmap1 = mergeBitmap(Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888), bitmap1);//此时imageview宽高仍然为切换前的宽高
            bitmap2 = mergeBitmap(Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888), bitmap2);
            canvas1 = new Canvas(bitmap1);
            canvas2 = new Canvas(bitmap2);
            canvas1.drawColor(Color.argb(0, 255, 255, 255));
            canvas2.drawColor(Color.argb(0, 255, 255, 255));
            canvas1.drawBitmap(bitmap1, new Matrix(), paint1);
            canvas2.drawBitmap(bitmap2, new Matrix(), paint2);
            imageview.setLayoutParams(new RelativeLayout.LayoutParams(W,H) );
            imageview.requestLayout();
            imageview.setImageBitmap(mergeBitmap(bitmap1,bitmap2));
            layoutParams.topMargin = (H / 4);
            textgroup.setLayoutParams(layoutParams);
            BG.setBackgroundResource(R.drawable.bghori);
        } else if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            bitmap1 = mergeBitmap(Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888), bitmap1);
            bitmap2 = mergeBitmap(Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888), bitmap2);
            canvas1 = new Canvas(bitmap1);
            canvas2 = new Canvas(bitmap2);
            canvas1.drawColor(Color.argb(0, 255, 255, 255));
            canvas2.drawColor(Color.argb(0, 255, 255, 255));
            canvas1.drawBitmap(bitmap1, new Matrix(), paint1);
            canvas2.drawBitmap(bitmap2, new Matrix(), paint2);
            imageview.setLayoutParams(new RelativeLayout.LayoutParams(W,H) );
            imageview.requestLayout();
            Log.i("ImageView", imageview.getWidth() + " " + imageview.getHeight());
            imageview.setImageBitmap(mergeBitmap(bitmap1,bitmap2));
            layoutParams.topMargin = (H  / 4);
            textgroup.setLayoutParams(layoutParams);
            BG.setBackgroundResource(R.drawable.bgver);
        }
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }




}