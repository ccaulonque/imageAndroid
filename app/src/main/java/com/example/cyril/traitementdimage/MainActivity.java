package com.example.cyril.traitementdimage;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.example.cyril.traitementdimage.Traitement.color_filter;
import static com.example.cyril.traitementdimage.Traitement.colorize;
import static com.example.cyril.traitementdimage.Traitement.egalisation_couleurs;
import static com.example.cyril.traitementdimage.Traitement.flou;
import static com.example.cyril.traitementdimage.Traitement.flougaussien;
import static com.example.cyril.traitementdimage.Traitement.getHisto;
import static com.example.cyril.traitementdimage.Traitement.gradient;
import static com.example.cyril.traitementdimage.Traitement.histoExtension;
import static com.example.cyril.traitementdimage.Traitement.laplace;
import static com.example.cyril.traitementdimage.Traitement.toBW;

public class MainActivity extends AppCompatActivity {

    /* request codes */
    static final int PICK_RGB_COLOR = 0;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int PICK_KERNEL_SIZE = 2;
    static final int WRITE_REQUEST_CODE = 10;

    Bitmap mutableBitmap, image_bitmap;
    TouchImageView iv;
    TextView tv;
    Button bt, buttonPicture, buttonSave;
    int indexImage;
    String mCurrentPhotoPath;
    ArrayList<Bitmap> images = new ArrayList<Bitmap>(); //liste des images disponibles

    //sauvegarde bitmap dans la mémoire externe
    private void saveImageToExternalStorage(Bitmap finalBitmap) {
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        File dossier = new File(root + "/traitementdimage");
        dossier.mkdirs();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fname = "image_" + timeStamp + ".jpg";
        File file = new File(dossier, fname);
        if (file.exists())
            file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


        //détecter le nouveau fichier immédiatement
        MediaScannerConnection.scanFile(this, new String[]{file.toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });

    }

    //appelle l'activité histogramme
    private void callHisto() {
        //intent avec l'histogramme de la bitmap actuelle
        int[] histo = getHisto(mutableBitmap);
        Intent showHistoIntent = new Intent(MainActivity.this, HistoActivity.class);
        showHistoIntent.putExtra("histogramme", histo);
        startActivity(showHistoIntent);
        overridePendingTransition(R.anim.slidertl2, R.anim.slidertl);
    }

    //appelle l'activité filtre couleur
    private void callFiltrer() {
        Intent colorIntent = new Intent(MainActivity.this, SelectColorActivity.class);
        startActivityForResult(colorIntent, PICK_RGB_COLOR);
        overridePendingTransition(R.anim.slidertl2, R.anim.slidertl);
    }

    //appelle l'activité kernel
    private void callKernel(String filtre) {
        //intent avec le type de kernel
        Intent kernelIntent = new Intent(MainActivity.this, KernelActivity.class);
        kernelIntent.putExtra("filtre", filtre);
        startActivityForResult(kernelIntent, PICK_KERNEL_SIZE);
        overridePendingTransition(R.anim.slidertl2, R.anim.slidertl);
    }

    //fichier dans la mémoire interne pour stocker les photos de la caméra
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefixe */
                ".jpg",   /* suffixe */
                storageDir      /* dossier */
        );

        mCurrentPhotoPath = image.getAbsolutePath();
        //  /storage/emulated/0/Android/data/com.example.cyril.traitementdimage/files/Pictures/JPEG_etc.jpg
        return image;
    }

    //démarre l'activité de l'appareil photo
    private void take_picture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;

            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                finish();
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }

    }

    //active ou désactive le bouton save en fonction de la permission
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case WRITE_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    buttonSave.setEnabled(true);
                } else {
                    buttonSave.setEnabled(false);
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /* récupération de la photo */
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bitmap b = BitmapFactory.decodeFile(mCurrentPhotoPath);
            image_bitmap = b.copy(Bitmap.Config.ARGB_8888, false);
            mutableBitmap = b.copy(Bitmap.Config.ARGB_8888, true);
            images.add(image_bitmap);
            indexImage = images.size() - 1;
            iv.setImageBitmap(mutableBitmap);
            iv.resetZoom();
            tv.setText("" + iv.getCurrentZoom());
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

        iv = findViewById(R.id.imageView);
        iv.setMaxZoom(5.0f);
        tv = findViewById(R.id.textView);
        bt = findViewById(R.id.button);
        buttonPicture = findViewById(R.id.buttonPicture);
        buttonSave = findViewById(R.id.buttonSave);

        /* ajout des images dispos à la liste */
        images.add(BitmapFactory.decodeResource(getResources(), R.drawable.maison));
        images.add(BitmapFactory.decodeResource(getResources(), R.drawable.planete));
        images.add(BitmapFactory.decodeResource(getResources(), R.drawable.pommes));
        images.add(BitmapFactory.decodeResource(getResources(), R.drawable.plage));
        images.add(BitmapFactory.decodeResource(getResources(), R.drawable.carre));

        image_bitmap = images.get(indexImage); //non mutable, sert à restaurer la bitmap
        mutableBitmap = image_bitmap.copy(Bitmap.Config.ARGB_8888, true); //mutable, on travaille sur cette bitmap

        iv.setImageBitmap(mutableBitmap);
        tv.setText("" + iv.getCurrentZoom());

        //demande la permission d'enregistrer des fichiers
        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        requestPermissions(permissions, WRITE_REQUEST_CODE);


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
                tv.setText("" + iv.getCurrentZoom());
            }
        });

        iv.setOnTouchImageViewListener(new TouchImageView.OnTouchImageViewListener() {
            @Override
            public void onMove() {
                tv.setText("" + iv.getCurrentZoom());
            }
        });

        buttonPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                take_picture();
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImageToExternalStorage(mutableBitmap);
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
                iv.setImageBitmap(mutableBitmap);
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
}
