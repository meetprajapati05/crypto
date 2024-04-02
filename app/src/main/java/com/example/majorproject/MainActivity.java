package com.example.majorproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;


public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPreferences = getSharedPreferences("MajorProject",MODE_PRIVATE);
        boolean theme = sharedPreferences.getBoolean("themeDark", false);

        if(theme){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }


        //Get user id from SharedPreference that store in Home page

        SharedPreferences getUserId = getSharedPreferences("MajorProject", MODE_PRIVATE);
        String user_id = getUserId.getString("email", null);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(user_id == null){
                    startActivity(new Intent(MainActivity.this,SingIn.class));
                    finish();
                }else{
                    Intent iHome = new Intent(MainActivity.this, HomePage.class);
                    iHome.putExtra("email", user_id);
                    iHome.putExtra("previous", true);
                    startActivity(iHome);
                    finish();
                }
            }
        },4000);
    }


}