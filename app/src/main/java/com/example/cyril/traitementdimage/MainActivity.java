package com.example.cyril.traitementdimage;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Button;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.example.cyril.traitementdimage.Traitement.*;

public class MainActivity extends AppCompatActivity {

    /* request codes */
    static final int PICK_RGB_COLOR = 0;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int PICK_KERNEL_SIZE = 2;
    Bitmap mutableBitmap, image_bitmap;
    TouchImageView iv;
    TextView tv;
    Button bt, buttonPicture;
    int indexImage;
    String mCurrentPhotoPath;
    ArrayList<Bitmap> images = new ArrayList<Bitmap>(); //liste des images disponibles

    private void callHisto() {
        //intent avec l'histogramme de la bitmap actuelle
        int[] histo = getHisto(mutableBitmap);
        Intent showHistoIntent = new Intent(MainActivity.this, HistoActivity.class);
        showHistoIntent.putExtra("histogramme", histo);
        startActivity(showHistoIntent);
        overridePendingTransition(R.anim.slidertl2, R.anim.slidertl);
    }

    private void callFiltrer() {
        Intent colorIntent = new Intent(MainActivity.this, SelectColorActivity.class);
        startActivityForResult(colorIntent, PICK_RGB_COLOR);
        overridePendingTransition(R.anim.slidertl2, R.anim.slidertl);
    }

    private void callKernel(String filtre) {
        //intent avec le type de kernel
        Intent kernelIntent = new Intent(MainActivity.this, KernelActivity.class);
        kernelIntent.putExtra("filtre", filtre);
        startActivityForResult(kernelIntent, PICK_KERNEL_SIZE);
        overridePendingTransition(R.anim.slidertl2, R.anim.slidertl);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void take_picture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                finish();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                Log.v("photouri", photoFile.toString());
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v("LIFECYCLE_LOG", "onActivityResult");

        /* récupération de la photo */
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bitmap b = BitmapFactory.decodeFile(mCurrentPhotoPath);
            image_bitmap = b.copy(Bitmap.Config.ARGB_8888, false);
            mutableBitmap = b.copy(Bitmap.Config.ARGB_8888, true);
            images.add(image_bitmap);
            indexImage = images.size() - 1;
            iv.setImageBitmap(mutableBitmap);
            iv.resetZoom();
            tv.setText(""+iv.getCurrentZoom());
        }

        /* récupération de la couleur du filtre */
        if (requestCode == PICK_RGB_COLOR && resultCode == RESULT_OK) {
            int color = data.getIntExtra("color", 0);
            color_filter(mutableBitmap, color);
        }

        /* récupération de la taille et du type du filtre */
        if (requestCode == PICK_KERNEL_SIZE && resultCode == RESULT_OK) {
            String filtre = data.getStringExtra("filtre");
            int n = data.getIntExtra("taille_kernel", 1);
            switch (filtre) {
                case "flou":
                    flou(mutableBitmap, n);
                    break;
                case "gaussien":
                    flougaussien(mutableBitmap, n);
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
        Log.v("LIFECYCLE_LOG", "onCreate");

        iv = findViewById(R.id.imageView);
        iv.setMaxZoom(5.0f);
        tv = findViewById(R.id.textView);
        bt = findViewById(R.id.button);
        buttonPicture = findViewById(R.id.buttonPicture);

        /* ajout des images dispos à la liste */
        images.add(BitmapFactory.decodeResource(getResources(), R.drawable.maison));
        images.add(BitmapFactory.decodeResource(getResources(), R.drawable.planete));
        images.add(BitmapFactory.decodeResource(getResources(), R.drawable.pommes));
        images.add(BitmapFactory.decodeResource(getResources(), R.drawable.plage));
        images.add(BitmapFactory.decodeResource(getResources(), R.drawable.carre));

        image_bitmap = images.get(indexImage); //non mutable, sert à restaurer la bitmap
        mutableBitmap = image_bitmap.copy(Bitmap.Config.ARGB_8888, true); //mutable, on travaille sur cette bitmap

        iv.setImageBitmap(mutableBitmap);
        tv.setText(""+iv.getCurrentZoom());

        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* au click, on passe à l'image suivante dans la liste */
                indexImage++;
                indexImage = indexImage % (images.size());
                image_bitmap = images.get(indexImage);
                mutableBitmap = image_bitmap.copy(Bitmap.Config.ARGB_8888, true);
                iv.setImageBitmap(mutableBitmap);
                iv.resetZoom();
                tv.setText(""+iv.getCurrentZoom());
            }
        });

        iv.setOnTouchImageViewListener(new TouchImageView.OnTouchImageViewListener() {
            @Override
            public void onMove() {
                tv.setText(""+iv.getCurrentZoom());
            }
        });

        buttonPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

        switch (id) {
            case R.id.action_griser:
                Traitement.toGray(mutableBitmap);
                break;
            case R.id.action_griserextension:
                Traitement.toGrayExtension(mutableBitmap, 0, 255);
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
                histoExtension(mutableBitmap, 0, 255);
                break;
            case R.id.action_egaliser:
                egalisation_couleurs(mutableBitmap);
                break;
            case R.id.action_restaurer:
                mutableBitmap = image_bitmap.copy(Bitmap.Config.ARGB_8888, true);
                iv.resetZoom();
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

    @Override
    public void onDestroy(){
        super.onDestroy();
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        for(File file: dir.listFiles()){
            file.delete();
        }
        File tempfile = new File(mCurrentPhotoPath);
        tempfile.delete();
    }
}
