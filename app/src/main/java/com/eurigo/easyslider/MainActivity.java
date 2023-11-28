package com.eurigo.easyslider;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.eurigo.easyslider.databinding.ActivityMainBinding;

import kotlin.random.Random;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.btnChangeValue.setOnClickListener(v -> binding.progress
                .setValue(Random.Default.nextInt(binding.progress.getMinValue(), binding.progress.getMaxValue())));
        binding.progress.setOnValueChangeListener(new OnValueChangeListener() {
            @Override
            public void onValueChange(int value, String percent, boolean isTouch) {
                Log.e("xxx", "onValueChange: " + value + "      percent: " + percent);
            }

            @Override
            public void onStopTrackingTouch(int value, String percent) {
                Log.e("xxx", "onStopTrackingTouch: " + value + "      percent: " + percent);
            }
        });
    }
}