package com.example.majorproject;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.airbnb.lottie.LottieAnimationView;


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

        onRestart();
    }

    private  boolean isConnected(){
        ConnectivityManager manager = (ConnectivityManager) getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
        return manager.getActiveNetworkInfo() != null && manager.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        //Get user id from SharedPreference that store in Home page
        SharedPreferences getUserId = getSharedPreferences("MajorProject", MODE_PRIVATE);
        String user_id = getUserId.getString("email", null);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isConnected()) {
                    if (user_id == null) {
                        startActivity(new Intent(MainActivity.this, SingIn.class));
                        finish();
                    } else {
                        Intent iHome = new Intent(MainActivity.this, HomePage.class);
                        iHome.putExtra("email", user_id);
                        iHome.putExtra("previous", true);
                        startActivity(iHome);
                        finish();
                    }
                }else{
                    LottieAnimationView animationView = findViewById(R.id.spleetAnimation);
                    animationView.pauseAnimation();

                    Dialog dialog = new Dialog(MainActivity.this);
                    dialog.setContentView(R.layout.layout_no_internet);
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.getWindow().setBackgroundDrawable(null);
                    dialog.setCancelable(false);

                    dialog.findViewById(R.id.dailogNoInternetButton).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            animationView.playAnimation();
                            dialog.cancel();
                            onRestart();
                        }
                    });

                    dialog.show();
                }
            }
        },4000);
    }
}