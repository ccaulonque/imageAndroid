package com.example.cyril.traitementdimage;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.example.cyril.traitementdimage.Traitement.color_filter;
import static com.example.cyril.traitementdimage.Traitement.flou;
import static com.example.cyril.traitementdimage.Traitement.flougaussien;

public class KernelActivity extends AppCompatActivity {

    SeekBar seekBarN;
    Button send;
    TextView nView;
    String filtre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kernel);

        seekBarN = findViewById(R.id.seekBarN);
        send = findViewById(R.id.send);
        nView = findViewById(R.id.viewN);

        filtre = getIntent().getStringExtra("filtre");

        seekBarN.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
                nView.setText("n = " + (seekBarN.getProgress() + 1));
            }
        });

        send.setOnClickListener(new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            end();
        }
    });
    }

    public void end(){
        /* après le click, on retourne la taille et le type du filtre */
        Intent i = new Intent(KernelActivity.this, MainActivity.class);
        int n = seekBarN.getProgress() + 1;
        i.putExtra("taille_kernel",n);
        i.putExtra("filtre",filtre);
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
