package com.example.cyril.traitementdimage;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Arrays;

public class HistoActivity extends AppCompatActivity {

    ImageView iv;

    public void onBackPressed(){
        super.onBackPressed();
        Intent i = new Intent(HistoActivity.this, MainActivity.class);
        setResult(Activity.RESULT_OK, i);
        finish();
        overridePendingTransition(R.anim.slideltr,R.anim.slideltr2);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_histo);

        int histo[] = getIntent().getIntArrayExtra("histogramme");

        iv = findViewById(R.id.viewHisto);

        int width = getWindowManager().getDefaultDisplay().getWidth();
        int height = width*4/3;

        int color_black = Color.rgb(0,0,0);
        int color_white = Color.rgb(255,255,255);

        int[] colors = new int[width*height];
        Arrays.fill(colors, color_white);
        Bitmap histogramme = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);

        int max=histo[0];

        /* l'histogramme prend toute la hauteur de la bitmap, peut importe la valeur maximale */
        for(int i = 0; i < histo.length; i++){
            if(max < histo[i])
                max = histo[i];
        }

       for(int x = 0; x<width; x++){
           int hauteur = (int)((double)(histo[(int)((double)x/(double)width*255)])/(double)max*(height - 100));
           /* tracé de l'histogramme */
           for(int y = height - hauteur; y < height; y++){
                colors[y * width + x] = color_black;
           }
           /* tracé de la bade du dégradé */
           for(int y = 0 ; y < 100 ; y++){
               int greylvl = (int)((double)x/(double)width*255);
               colors[y * width + x ] = Color.rgb(greylvl,greylvl,greylvl);
           }
       }

        histogramme.setPixels(colors,0,width,0,0,width,height);
        iv.setImageBitmap(histogramme);
    }
}
