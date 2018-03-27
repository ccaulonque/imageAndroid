package com.example.cyril.traitementdimage;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import static com.example.cyril.traitementdimage.Traitement.color_filter;

public class SelectColorActivity extends AppCompatActivity {
    SeekBar seekRed,seekGreen,seekBlue;
    Button send;
    ImageView viewColor;
    TextView textColor;
    Bitmap colorBitmap = Bitmap.createBitmap(100,100,Bitmap.Config.ARGB_8888);

    void bmFromColor(Bitmap bm, int r, int g, int b){
        int w = bm.getWidth(), h = bm.getHeight();
        int[] pixels = new int[w*h];
        Arrays.fill(pixels,Color.rgb(r,g,b));
        bm.setPixels(pixels,0,w,0,0,w,h);
        viewColor.setImageBitmap(bm);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

                setContentView(R.layout.selectcolor_layout);
        seekRed =findViewById(R.id.seekBarRed);
        seekGreen =findViewById(R.id.seekBarGreen);
        seekBlue =findViewById(R.id.seekBarBlue);
        send = findViewById(R.id.ok);
        viewColor = findViewById(R.id.viewColor);
        textColor = findViewById(R.id.textColor);


        seekRed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                bmFromColor(colorBitmap, seekRed.getProgress(), seekGreen.getProgress(), seekBlue.getProgress());
                textColor.setText(seekRed.getProgress() + ";" + seekGreen.getProgress() + ";" + seekBlue.getProgress());
            }
        });

        seekBlue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                bmFromColor(colorBitmap, seekRed.getProgress(), seekGreen.getProgress(), seekBlue.getProgress());
                textColor.setText(seekRed.getProgress() + ";" + seekGreen.getProgress() + ";" + seekBlue.getProgress());
            }
        });

        seekGreen.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                bmFromColor(colorBitmap, seekRed.getProgress(), seekGreen.getProgress(), seekBlue.getProgress());
                textColor.setText(seekRed.getProgress() + ";" + seekGreen.getProgress() + ";" + seekBlue.getProgress());
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                end();
            }
        });
    }

    public void end() {
        Intent i = new Intent(SelectColorActivity.this, MainActivity.class);
        int color = Color.rgb(seekRed.getProgress(), seekGreen.getProgress(), seekBlue.getProgress());
        i.putExtra("color",color);
        setResult(Activity.RESULT_OK, i);
        finish();
        overridePendingTransition(R.anim.slideltr,R.anim.slideltr2);
    }

    public void onBackPressed(){
        /* si l'utilisateur annule, on ne fait rien */
        setResult(Activity.RESULT_CANCELED);
        finish();
        overridePendingTransition(R.anim.slideltr,R.anim.slideltr2);
    }

}
