package com.example.majorproject;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.SeekBar;

import com.example.majorproject.databinding.ActivityBuyPageBinding;

public class BuyPage extends AppCompatActivity {
    ActivityBuyPageBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBuyPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.seekbarBuyPageLaverage.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                binding.txtBuyPageSeekbarValue.setText(String.valueOf(i)+"x");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }
}