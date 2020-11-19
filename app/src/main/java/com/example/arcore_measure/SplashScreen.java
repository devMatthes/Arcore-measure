package com.example.arcore_measure;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;


public class SplashScreen extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_splash);
        Intent i = new Intent(SplashScreen.this, MainActivity.class);
        SplashScreen.this.startActivity(i);
        SplashScreen.this.finish();
        /*new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(SplashScreen.this, MainActivity.class);
                SplashScreen.this.startActivity(i);
                SplashScreen.this.finish();
            }
        }, 1000);*/
    }
}
