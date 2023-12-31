package com.example.capactive;

import static android.view.MotionEvent.AXIS_ORIENTATION;
import static android.view.MotionEvent.AXIS_TILT;
import static android.view.MotionEvent.AXIS_X;
import static android.view.MotionEvent.AXIS_Y;
import static android.view.View.VISIBLE;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Old  extends Activity{
    private Button btn1, btn2, btnr, btng, btnb, small, mid, big, clean;
    private TextView t1, t2, t3, t4, t5, t6;
    private View bigback,smallback,midback;
    private static Paint paint1, paint2;//定义画笔
    private static Canvas canvas1, canvas2;//画布
    private static Bitmap bitmap1, bitmap2, bitmap3;//位图(画板),用于置放画布，画布内容在此呈现
    private ImageView imageview;//用于将画板和布局联系在一起，从而显示出来
    float startX, startY, endX, endY;
    private MotionEvent motionEvent;
    private  boolean isNeedpress;

    private RelativeLayout BG, textgroup, buttongroup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //横竖屏转换
        if (screenLandPort(this) == 0) {
            BG = findViewById(R.id.BG);
            BG.getResources().getDrawable(R.drawable.bghori);
            textgroup = findViewById(R.id.textgroup);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(textgroup.getLayoutParams());
            layoutParams.topMargin = 350;
            textgroup.setLayoutParams(layoutParams);
        } else {
            BG = findViewById(R.id.BG);
            BG.getResources().getDrawable(R.drawable.bgver);
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


        btn1.setOnClickListener(v -> {
            initBackground((Button) v);});
        btn2.setOnClickListener(v -> {
            initBackground((Button) v);});
        btnr.setOnClickListener(c -> {
            initColor((Button) c);});
        btng.setOnClickListener(c -> {
            initColor((Button) c);});
        btnb.setOnClickListener(c -> {
            initColor((Button) c);});
        small.setOnClickListener(s -> {
            initSize((Button) s);});
        mid.setOnClickListener(s -> {
            initSize((Button) s);});
        big.setOnClickListener(s -> {
            initSize((Button) s);});


//创建空白图片和画布，将图片放在画布上
        Log.i("DianJiActivity", imageview.getWidth() + " " + imageview.getHeight());
        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        Log.i("DianJiActivity",point.x + " " + point.y);


        imageview.post(new Runnable() {
            public void run() {
                bitmap1 = Bitmap.createBitmap(imageview.getWidth(), imageview.getHeight(),Bitmap.Config.ARGB_8888);//配置位图1,画轨迹
                bitmap2 = Bitmap.createBitmap(imageview.getWidth(), imageview.getHeight(),Bitmap.Config.ARGB_8888);//配置位图2,画光标
                canvas1 = new Canvas(bitmap1);//创建画布1
                canvas2 = new Canvas(bitmap2);//创建画布2
                canvas1.drawColor(Color.argb(0, 255, 255, 255));//设置画布背景颜色为白色，全透明
                canvas2.drawColor(Color.argb(0, 255, 255, 255));//设置画布背景颜色为白色，全透明
                canvas1.drawBitmap(bitmap1, new Matrix(), paint1);
                canvas2.drawBitmap(bitmap2, new Matrix(), paint2);
                paint1 = new Paint();//创建画笔,画轨迹
                paint2 = new Paint();//创建画笔2,画光标
                paint1.setAntiAlias(true);//设置画笔抗锯齿
                paint2.setAntiAlias(true);//设置画笔抗锯齿
                paint1.setColor(Color.parseColor("#E60000"));//画笔默认颜色红色
                paint2.setColor(Color.argb(255,0,0,0));//画笔默认颜色黑色
                paint1.setStrokeWidth(8);//画笔默认尺寸中等
                paint2.setStrokeWidth(1);//画笔默认尺寸
                paint1.setStrokeCap(Paint.Cap.ROUND);//点的形状为圆形
                paint2.setStrokeCap(Paint.Cap.ROUND);//点的形状为圆形
                paint1.setStyle(Paint.Style.STROKE);//绘画样式，描边
                paint2.setStyle(Paint.Style.STROKE);//绘画样式，描边
//                bitmap3 = mergeBitmap(bitmap1, bitmap2);//合并
                imageview.setImageBitmap(bitmap3);//展示出来

            }
        });


//注册触摸监听事件，获取按下时的坐标和移动后的坐标，在开始和结束之间画一条直线并更新画布图片
        imageview.setOnTouchListener((v, event) -> {//根据触摸动作执行不同操作
            motionEvent = event;

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN://第一根手指点击屏幕
                    Log.i("DianJiActivity", "ACTION_DOWN");
                    startX = (int) (event.getX());
                    startY = (int) (event.getY());
                    break;

                case MotionEvent.ACTION_POINTER_DOWN://三根手指同时触屏(大于三根手指时也会触发)
                    if (event.getPointerId(event.getActionIndex()) == 2){
                        if (TOP.getVisibility() == VISIBLE){//如果顶部工具栏此时可见，则将它改为不可见
                            TOP.setVisibility(View.INVISIBLE);
                        } else if (TOP.getVisibility() == View.INVISIBLE){//如果顶部工具栏此时不可见，则将它改为可见
                            TOP.setVisibility(VISIBLE);
                        }
                    }
                    break;

                case MotionEvent.ACTION_MOVE://手指滑动
                    t1.setText("X：" + (int) event.getAxisValue(AXIS_X));
                    t2.setText("Y：" + (int) event.getAxisValue(AXIS_Y));
                    t3.setText("压力：" + event.getPressure());
                    t4.setText("倾斜：" + event.getAxisValue(AXIS_TILT));
                    t5.setText("角度：" + event.getAxisValue(AXIS_ORIENTATION));
                    t6.setText("触摸点数：" + event.getPointerCount());
                    canvas2.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);//清除十字架
                    endX = (int) (event.getX());
                    endY = (int) (event.getY());
                    if(this.isNeedpress){
                        paint1.setStrokeWidth(motionEvent.getPressure() * 25);
                    }else{
                    }
                    canvas1.drawLine(startX, startY, endX, endY, paint1);
                    startX = (int) (event.getX());
                    startY = (int) (event.getY());
                    canvas2.drawLine(startX, (startY + 15),startX, (startY - 15), paint2);
                    canvas2.drawLine((startX + 15), startY,(startX - 15), startY, paint2);
                    imageview.setImageBitmap(mergeBitmap(bitmap1,bitmap2));
                    break;

                case MotionEvent.ACTION_UP://最后一根手指离开屏幕
                    t1.setText("");
                    t2.setText("");
                    t3.setText("");
                    t4.setText("");
                    t5.setText("");
                    t6.setText("");
                    canvas2.drawColor(Color.TRANSPARENT,PorterDuff.Mode.CLEAR);//清除十字架
                    imageview.setImageBitmap(mergeBitmap(bitmap1,bitmap2));
                    Log.i("DianJiActivity", "ACTION_UP");
                    break;
            }
            bitmap3 = mergeBitmap(bitmap1,bitmap2);
            imageview.invalidate();
            return true;
        });


        imageview.setOnHoverListener((v, event) -> {//监控笔的悬停
            switch (event.getAction()) {
                case MotionEvent.ACTION_HOVER_ENTER://笔进入view范围
                    Log.i("DianJiActivity", "ACTION_HOVER_ENTER");
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
                    Log.i("DianJiActivity", "ACTION_HOVER_MOVE");
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
        clean.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                canvas1.drawColor(0, PorterDuff.Mode.CLEAR);//清屏
                bitmap1 = Bitmap.createBitmap(imageview.getWidth(), imageview.getHeight(), Bitmap.Config.ARGB_8888);
                canvas1 = new Canvas(bitmap1);
                canvas1.drawColor(Color.argb(0, 255, 255, 255));
                canvas1.drawBitmap(bitmap1, new Matrix(), paint1);
                imageview.setImageBitmap(bitmap1);
            }
        });


    }


    //选择了直线还是压感
    private void initBackground(Button v) {
        if (v == btn2) {
            this.isNeedpress = true;
            v.setBackground(this.getResources().getDrawable(R.drawable.presson));
            btn1.setBackground(this.getResources().getDrawable(R.drawable.penoff));
        }else if(v == btn1){
            this.isNeedpress = false;
            v.setBackground(this.getResources().getDrawable(R.drawable.penon));
            btn1.setBackground(this.getResources().getDrawable(R.drawable.pressoff));
            paint1.setStrokeWidth(8);
        }
    }


    //选择了何种颜色按钮
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


    @Override
    public void onConfigurationChanged( Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Toast.makeText(this, "onConfigurationChanged", Toast.LENGTH_SHORT).show();
        Display display = this.getWindowManager().getDefaultDisplay();
//        RelativeLayout.LayoutParams layoutParams=new RelativeLayout.LayoutParams(display.getWidth(),display.getHeight());
//        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        textgroup = findViewById(R.id.textgroup);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(textgroup.getLayoutParams());
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            bitmap1 = mergeBitmap(Bitmap.createBitmap(2560, 1436, Bitmap.Config.ARGB_8888), bitmap1);
            bitmap2 = mergeBitmap(Bitmap.createBitmap(2560, 1436, Bitmap.Config.ARGB_8888), bitmap2);
            canvas1 = new Canvas(bitmap1);
            canvas2 = new Canvas(bitmap2);
            canvas1.drawColor(Color.argb(0, 255, 255, 255));
            canvas2.drawColor(Color.argb(0, 255, 255, 255));
            canvas1.drawBitmap(bitmap1, new Matrix(), paint1);
            canvas2.drawBitmap(bitmap2, new Matrix(), paint2);
            imageview.setImageBitmap(mergeBitmap(bitmap1, bitmap2));
            layoutParams.topMargin = 350;
            textgroup.setLayoutParams(layoutParams);
        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
//            imageview.setLayoutParams(layoutParams);
//            imageview.requestLayout();
            bitmap1 = mergeBitmap(Bitmap.createBitmap(1600, 2400, Bitmap.Config.ARGB_8888), bitmap1);
            bitmap2 = mergeBitmap(Bitmap.createBitmap(1600, 2400, Bitmap.Config.ARGB_8888), bitmap2);
            canvas1 = new Canvas(bitmap1);
            canvas2 = new Canvas(bitmap2);
            canvas1.drawColor(Color.argb(0, 255, 255, 255));
            canvas2.drawColor(Color.argb(0, 255, 255, 255));
            canvas1.drawBitmap(bitmap1, new Matrix(), paint1);
            canvas2.drawBitmap(bitmap2, new Matrix(), paint2);
            imageview.setImageBitmap(mergeBitmap(bitmap1, bitmap2));
            layoutParams.topMargin = 586;
            textgroup.setLayoutParams(layoutParams);
        }
    }


}
