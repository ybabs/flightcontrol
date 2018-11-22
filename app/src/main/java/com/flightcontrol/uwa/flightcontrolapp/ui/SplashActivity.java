package com.flightcontrol.uwa.flightcontrolapp.ui;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.flightcontrol.uwa.flightcontrolapp.R;

import com.flightcontrol.uwa.flightcontrolapp.MainActivity;
/**
 * Created by rosboxone on 16/03/18.
 */

public class SplashActivity extends AppCompatActivity {
    private TextView textView;
    private ImageView imageView;
    @Override
    protected  void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                |View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_FULLSCREEN );
        setContentView(R.layout.activity_splash);
        textView = (TextView) findViewById(R.id.app_name_textview);
        textView.setTypeface(Typeface.createFromAsset(getAssets(), "Univers-light-normal.ttf"));
        imageView = (ImageView) findViewById(R.id.app_name_imageview);
        Animation myAnimation = AnimationUtils.loadAnimation(this, R.anim.anim_in);
        textView.startAnimation(myAnimation);
        imageView.startAnimation(myAnimation);

        final Intent intent = new Intent(this, MainActivity.class);

        Thread timer = new Thread()
        {
            public void run()
            {
                try
                {
                    sleep(2000);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }

                finally {
                    startActivity(intent);
                    finish();

                }

            }

        };

        timer.start();
    }

}