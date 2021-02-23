package com.codewithmab.classroomclient;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        Thread t = new Thread(){
            @Override
            public void run() {
                try{
                    sleep(2 * 1000);
                    startActivity(new Intent(SplashScreenActivity.this, SignInActivity.class));
                    finish();
                } catch (InterruptedException e){ e.printStackTrace(); }
            }
        };
        t.start();
    }
}