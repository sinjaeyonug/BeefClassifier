package com.example.beefclassifier;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.beefclassifier.databinding.ActivityLodingBinding;

public class LodingActivity extends AppCompatActivity {
    private ActivityLodingBinding LodingBinding;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        LodingBinding= ActivityLodingBinding.inflate(getLayoutInflater());
        setContentView(LodingBinding.getRoot());

        try{
            Thread.sleep(5000);
        }catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        startActivity(new Intent(this,LoginActivity.class));
        finish();
    }
}