package com.example.majorproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SharedMemory;
import android.view.MenuItem;

import com.example.majorproject.Fragment.HomeFragment;
import com.example.majorproject.Fragment.MarketFragment;
import com.example.majorproject.databinding.ActivityHomePageBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomePage extends AppCompatActivity {

    ActivityHomePageBinding binding;
    String user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomePageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        user_id = getIntent().getStringExtra("user_id");

        if(user_id != null){
            SharedPreferences setUserId = getSharedPreferences("MajorProject", MODE_PRIVATE);
            SharedPreferences.Editor editor = setUserId.edit();
            editor.putString("user_id", user_id);
            editor.apply();
        }

        //set bottom navigation view  select item event
        binding.bottomNavView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int itemId = item.getItemId();

               if(itemId == R.id.bottomOptHome){
                   setFragment(new HomeFragment());
               }
               else{
                   setFragment(new MarketFragment());
               }

                return true;
            }
        });

        binding.bottomNavView.setSelectedItemId(R.id.bottomOptHome);
    }

    public void setFragment(Fragment fragment){
        FragmentManager fm =getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        ft.replace(R.id.frameLayout, fragment);

        ft.commit();

    }
}