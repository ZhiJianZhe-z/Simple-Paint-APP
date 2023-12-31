package com.example.capactive;
/**
 * 第八次修改
 * 使用TextureView开启一个子线程进行绘制
 * 开启了硬件加速
 *
 * */

import static android.view.MotionEvent.AXIS_TILT;
import static android.view.MotionEvent.AXIS_X;
import static android.view.MotionEvent.AXIS_Y;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {
    private Button btn1, btn2, btnr, btng, btnb, small, mid, big, clean;
    private TextView t1, t2, t3, t4, t5, t6;
    private View smallback, midback, TOP;
    private  RelativeLayout BG, textgroup;
    private TestTextureView textureView;


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
            layoutParams.topMargin = (this.getWindowManager().getDefaultDisplay().getHeight() / 4);
            textgroup.setLayoutParams(layoutParams);
        } else {
            BG = findViewById(R.id.BG);
            BG.setBackgroundResource(R.drawable.bgver);
            textgroup = findViewById(R.id.textgroup);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(textgroup.getLayoutParams());
            layoutParams.topMargin = (this.getWindowManager().getDefaultDisplay().getHeight() / 4);
            textgroup.setLayoutParams(layoutParams);
        }

        //启用硬件加速
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        );


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
        t1 = findViewById(R.id.t1);
        t2 = findViewById(R.id.t2);
        t3 = findViewById(R.id.t3);
        t4 = findViewById(R.id.t4);
        t5 = findViewById(R.id.t5);
        t6 = findViewById(R.id.t6);
        TOP = findViewById(R.id.TOP);
        textureView = findViewById(R.id.TestTextureView);


        btn1.setOnClickListener(v -> initBackground((Button) v));
        btn2.setOnClickListener(v -> initBackground((Button) v));
        btnr.setOnClickListener(c -> initColor((Button) c));
        btng.setOnClickListener(c -> initColor((Button) c));
        btnb.setOnClickListener(c -> initColor((Button) c));
        small.setOnClickListener(s -> initSize((Button) s));
        mid.setOnClickListener(s -> initSize((Button) s));
        big.setOnClickListener(s -> initSize((Button) s));



        //定义监听器接口的具体实现
        textureView.setListener(
                new CustomListen.ListenerTouch() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void isTouch() {
                        if ( textureView.isTouch ) {
                            t1.setText("X：" + (int) textureView.eventTouch.getAxisValue(AXIS_X));
                            t2.setText("Y：" + (int) textureView.eventTouch.getAxisValue(AXIS_Y));
                            t3.setText("压力：" + (4096 * textureView.eventTouch.getPressure()));
                            t4.setText("倾斜：" + textureView.eventTouch.getAxisValue(AXIS_TILT));
                            t5.setText("角度：" + (int) (90 - (57.3 * textureView.eventTouch.getAxisValue(AXIS_TILT))));
                            t6.setText("触摸点数：" + textureView.eventTouch.getPointerCount());
                        } else {
                            t1.setText("");
                            t2.setText("");
                            t3.setText("");
                            t4.setText("");
                            t5.setText("");
                            t6.setText("");
                        }
                    }
                },
                new CustomListen.ListenerHover() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void isHover() {
                        if ( textureView.isHover ) {
                            t1.setText("X：" + (int) textureView.eventHover.getAxisValue(AXIS_X));
                            t2.setText("Y：" + (int) textureView.eventHover.getAxisValue(AXIS_Y));
                        } else {
                            t1.setText("");
                            t2.setText("");
                        }
                    }
                },
                new CustomListen.ListenerThreeFingers() {
                    @Override
                    public void isThreeFingers() {
                        if (textureView.threeFingers) {
                            if (TOP.getVisibility() == VISIBLE) {//如果顶部工具栏此时可见，则将它改为不可见
                                TOP.setVisibility(View.INVISIBLE);
                            } else if (TOP.getVisibility() == View.INVISIBLE) {//如果顶部工具栏此时不可见，则将它改为可见
                                TOP.setVisibility(VISIBLE);
                            }
                        }
                    }
                });

        //清屏，重新生成一块画布
        clean.setOnClickListener(v -> {
            textureView.clean();
        });

    }


    //选择了直线还是压感
    @SuppressLint("UseCompatLoadingForDrawables")
    private void initBackground(Button v) {
        if (v == btn2) {
            textureView.isNeedpress = true;
            v.setBackground(this.getResources().getDrawable(R.drawable.presson));
            btn1.setBackground(this.getResources().getDrawable(R.drawable.penoff));
        }else if(v == btn1){
            textureView.isNeedpress = false;
            v.setBackground(this.getResources().getDrawable(R.drawable.penon));
            btn2.setBackground(this.getResources().getDrawable(R.drawable.pressoff));
            textureView.paint1.setStrokeWidth(8);
        }
    }

    //选择了何种颜色按钮
    @SuppressLint("UseCompatLoadingForDrawables")
    private void initColor(Button c) {
        btng.setBackground(this.getResources().getDrawable(R.drawable.greenoff));
        btnb.setBackground(this.getResources().getDrawable(R.drawable.blueoff));
        if (c == btnr) {
            c.setBackground(this.getResources().getDrawable(R.drawable.redon));
            textureView.paint1.setColor(c.getTextColors().getDefaultColor());
        }else if (c == btng) {
            c.setBackground(this.getResources().getDrawable(R.drawable.greenon));
            btnr.setBackground(this.getResources().getDrawable(R.drawable.redoff));
            textureView.paint1.setColor(c.getTextColors().getDefaultColor());
        }else if (c == btnb) {
            c.setBackground(this.getResources().getDrawable(R.drawable.blueon));
            btnr.setBackground(this.getResources().getDrawable(R.drawable.redoff));
            textureView.paint1.setColor(c.getTextColors().getDefaultColor());
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
            textureView.paint1.setStrokeWidth(2);
            small.setBackground(this.getResources().getDrawable(R.drawable.smallon));
            smallback.setBackground(this.getResources().getDrawable(R.drawable.linetypeon));
        } else if (s == mid) {
            textureView.paint1.setStrokeWidth(8);
            mid.setBackground(this.getResources().getDrawable(R.drawable.midon));
            midback.setBackground(this.getResources().getDrawable(R.drawable.linetypeon));
        } else if (s == big) {
            textureView.paint1.setStrokeWidth(20);
            big.setBackground(this.getResources().getDrawable(R.drawable.bigon));
        }
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
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int W = textureView.getHeight();//此时得到的SurfaceView的宽高仍为转换前宽高，所以得让宽高互换
        int H = textureView.getWidth();
        super.onConfigurationChanged(newConfig);

        textgroup = findViewById(R.id.textgroup);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(textgroup.getLayoutParams());
        if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            layoutParams.topMargin = (H / 4);
            textgroup.setLayoutParams(layoutParams);
            BG.setBackgroundResource(R.drawable.bghori);
        } else if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            layoutParams.topMargin = (H / 4);
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