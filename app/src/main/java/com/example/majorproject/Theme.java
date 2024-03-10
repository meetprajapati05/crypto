package com.example.majorproject;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.majorproject.databinding.ActivityThemeBinding;

public class Theme extends AppCompatActivity {
    ActivityThemeBinding binding;
    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityThemeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbarTheme.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences("MajorProject",MODE_PRIVATE);
        boolean theme = sharedPreferences.getBoolean("themeDark", false);

        if(theme){
            binding.btnDark.setBackgroundColor(R.color.light_green);
            binding.txtDark.setTextColor(Color.WHITE);
            binding.btnLight.setBackgroundColor(Color.TRANSPARENT);
            binding.txtLight.setTextColor(Color.BLACK);
        }else{
            binding.btnLight.setBackgroundColor(R.color.light_green);
            binding.txtLight.setTextColor(Color.WHITE);
            binding.btnDark.setBackgroundColor(Color.TRANSPARENT);
            binding.txtDark.setTextColor(Color.BLACK);
        }

        binding.btnDark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

                binding.btnDark.setBackgroundColor(R.color.light_green);
                binding.txtDark.setTextColor(Color.WHITE);
                binding.btnLight.setBackgroundColor(Color.TRANSPARENT);
                binding.txtLight.setTextColor(Color.BLACK);

                SharedPreferences preferences = getSharedPreferences("MajorProject",MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("themeDark", true);
                editor.apply();

            }
        });

        binding.btnLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

                binding.btnLight.setBackgroundColor(R.color.light_green);
                binding.txtLight.setTextColor(Color.WHITE);
                binding.btnDark.setBackgroundColor(Color.TRANSPARENT);
                binding.txtDark.setTextColor(Color.BLACK);

                SharedPreferences preferences = getSharedPreferences("MajorProject",MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("themeDark", false);
                editor.apply();
            }
        });
    }
}