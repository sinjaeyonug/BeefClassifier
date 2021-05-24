package com.example.beefclassifier;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


import com.example.beefclassifier.ImageUtils.*;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.Surface;
import android.widget.ImageView;
import android.widget.TextView;

import static com.example.beefclassifier.ImageUtils.byteArrayToBitmap;

public class ResultActivity extends AppCompatActivity {
    private ImageView resultimgView;
    private TextView resultTitleView,resultconfView;
    private Bitmap resultimg;
    private String resultTitle;
    private float resultConf;
    private Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        resultimgView = findViewById(R.id.resultimg);
        resultTitleView = findViewById(R.id.resultTitle);
        resultconfView = findViewById(R.id.resultconf);

        Intent intent = getIntent();

        resultimg= byteArrayToBitmap(intent.getByteArrayExtra("image"));
        resultTitle=intent.getStringExtra("title");
        resultConf=intent.getFloatExtra("Confidence",0);

        Matrix matrix = new Matrix();
        matrix.preRotate(90,0,0);

        resultimg = Bitmap.createBitmap(resultimg,0,0,resultimg.getWidth(),resultimg.getHeight(),matrix,false);
        resultimgView.setImageBitmap(resultimg);
        resultTitleView.setText(resultTitle);
        resultconfView.setText(resultConf+"");

        resultTitleView.bringToFront();
        resultconfView.bringToFront();
    }

}