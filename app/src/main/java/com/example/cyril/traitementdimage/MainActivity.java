package com.example.cyril.traitementdimage;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;

import java.util.ArrayList;

import static com.example.cyril.traitementdimage.Traitement.*;

public class MainActivity extends AppCompatActivity {

    Bitmap mutableBitmap, image_bitmap;
    ImageView iv;
    TextView tv;
    Button bt, buttonPicture;

    int indexImage;

    /* request codes */
    static final int PICK_RGB_COLOR = 0;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int PICK_KERNEL_SIZE = 2;

    ArrayList<Integer> images = new ArrayList<Integer>(); //liste des images disponibles

    private void callHisto(){
        //intent avec l'histogramme de la bitmap actuelle
        int[] histo = getHisto(mutableBitmap);
        Intent showHistoIntent = new Intent(MainActivity.this, HistoActivity.class);
        showHistoIntent.putExtra("histogramme",histo);
        startActivity(showHistoIntent);
        overridePendingTransition(R.anim.slidertl2,R.anim.slidertl);
    }

    private void callFiltrer(){
        Intent colorIntent = new Intent(MainActivity.this, SelectColorActivity.class);
        startActivityForResult(colorIntent, PICK_RGB_COLOR);
        overridePendingTransition(R.anim.slidertl2, R.anim.slidertl);
    }

    private void callKernel(String filtre){
        //intent avec le type de kernel
        Intent kernelIntent = new Intent(MainActivity.this, KernelActivity.class);
        kernelIntent.putExtra("filtre",filtre);
        startActivityForResult(kernelIntent, PICK_KERNEL_SIZE);
        overridePendingTransition(R.anim.slidertl2, R.anim.slidertl);
    }

    private void take_picture(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v("LIFECYCLE_LOG","onActivityResult");

        /* récupération de la miniature de la photo */
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            image_bitmap = imageBitmap.copy(Bitmap.Config.ARGB_8888, false);
            mutableBitmap = imageBitmap.copy(Bitmap.Config.ARGB_8888, true);
            iv.setImageBitmap(mutableBitmap);
        }

        /* récupération de la couleur du filtre */
        if(requestCode == PICK_RGB_COLOR && resultCode == RESULT_OK){
            int color = data.getIntExtra("color",0);
            color_filter(mutableBitmap, color);
        }

        /* récupération de la taille et du type du filtre */
        if(requestCode == PICK_KERNEL_SIZE && resultCode == RESULT_OK){
            String filtre = data.getStringExtra("filtre");
            int n = data.getIntExtra("taille_kernel",1);
            switch(filtre){
                case "flou":
                    flou(mutableBitmap,n);
                    break;
                case "gaussien":
                    flougaussien(mutableBitmap,n);
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Log.v("LIFECYCLE_LOG","onCreate");

        iv = findViewById(R.id.imageView);
        tv = findViewById(R.id.textView);
        bt = findViewById(R.id.button);
        buttonPicture = findViewById(R.id.buttonPicture);

        /* ajout des images dispos à la liste */
        images.add(R.drawable.maison);
        images.add(R.drawable.planete);
        images.add(R.drawable.pommes);
        images.add(R.drawable.plage);
        images.add(R.drawable.carre);


        image_bitmap = BitmapFactory.decodeResource(getResources(), images.get(indexImage)); //non mutable, sert à restaurer la bitmap
        mutableBitmap = image_bitmap.copy(Bitmap.Config.ARGB_8888, true); //mutable, on travaille sur cette bitmap

        iv.setImageBitmap(mutableBitmap);
        tv.setText(image_bitmap.getWidth() + "/" + image_bitmap.getHeight());

        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* au click, on passe à l'image suivante dans la liste */
                indexImage++;
                indexImage = indexImage%(images.size());
                image_bitmap = BitmapFactory.decodeResource(getResources(), images.get(indexImage));
                mutableBitmap = image_bitmap.copy(Bitmap.Config.ARGB_8888, true);
                iv.setImageBitmap(mutableBitmap);
                tv.setText(image_bitmap.getWidth() + "/" + image_bitmap.getHeight());
            }
        });

        buttonPicture.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                take_picture();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.action_griser:
                Traitement.toGray(mutableBitmap);
                break;
            case R.id.action_griserextension:
                Traitement.toGrayExtension(mutableBitmap,0,255);
                break;
            case R.id.action_coloriser:
                colorize(mutableBitmap);
                break;
            case R.id.action_filtrer:
                callFiltrer();
                break;
            case R.id.action_histogramme:
                callHisto();
                break;
            case R.id.action_extensioncouleur:
                histoExtension(mutableBitmap,0,255);
                break;
            case R.id.action_egaliser:
                egalisation_couleurs(mutableBitmap);
                break;
            case R.id.action_restaurer:
                mutableBitmap = image_bitmap.copy(Bitmap.Config.ARGB_8888, true);
                iv.setImageBitmap(mutableBitmap);
                break;
            case R.id.action_flou:
                callKernel("flou");
                break;
            case R.id.action_flougaussien:
                callKernel("gaussien");
                break;
            case R.id.action_gradient:
                gradient(mutableBitmap);
                break;
            case R.id.action_laplace:
                laplace(mutableBitmap);
                break;
            case R.id.action_bw:
                toBW(mutableBitmap);
                break;
            default:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
