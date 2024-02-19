package com.example.majorproject;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.majorproject.databinding.ActivityMarketDetailBinding;

public class MarketDetail extends AppCompatActivity {
    ActivityMarketDetailBinding binding;
    String name,symbol,type;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMarketDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        name = getIntent().getStringExtra("name").toString();
        symbol = getIntent().getStringExtra("symbol").toString();
        type = getIntent().getStringExtra("type").toString();

        binding.txtName.setText(name + "\n" + symbol + "\n" + type);
    }
}