package com.example.cyril.traitementdimage;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.GestureDetector;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ImageView;
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
    int actionBarHeight = 0;
    int statusBarHeight = 0;
    Bitmap mutableBitmap, image_bitmap;
    ImageView iv;
    int ivWidth, ivHeight = 0;
    TextView tv;
    Button bt, buttonPicture;
    ScaleGestureDetector SGD;
    GestureDetector GD;
    float scale = 1f;
    float tX = 0, tY = 0;
    boolean ZOOMED_IN = false;
    boolean bitmap_ready = false;
    Matrix matrix = new Matrix();
    int indexImage;
    String mCurrentPhotoPath;
    ArrayList<Bitmap> images = new ArrayList<Bitmap>(); //liste des images disponibles

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        //SGD.onTouchEvent(ev);
        GD.onTouchEvent(ev);
        return true;
    }

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

    public void onWindowFocusChanged(boolean hasFocus) {
        iv = findViewById(R.id.imageView);
        ivWidth = iv.getWidth();
        ivHeight = iv.getHeight();
        if(!bitmap_ready)
            initBitmap();
    }

    private void initBitmap() {
        ZOOMED_IN = false;
        Rect rectDrawable = iv.getDrawable().getBounds();
        scale = Math.min((float) ivWidth / (float) rectDrawable.width(), (float) ivHeight / (float) rectDrawable.height());

        matrix.setScale(scale, scale);
        iv.setImageMatrix(matrix);
        float[] mvalues = new float[9];
        matrix.getValues(mvalues);
        tX = mvalues[2];
        tY = mvalues[5];
        bitmap_ready = true;
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
        tv = findViewById(R.id.textView);
        bt = findViewById(R.id.button);
        buttonPicture = findViewById(R.id.buttonPicture);

        //SGD = new ScaleGestureDetector(this, new simpleOnScaleGestureListener());
        GD = new GestureDetector(this, new simpleGestureListener());

        /* ajout des images dispos à la liste */
        images.add(BitmapFactory.decodeResource(getResources(), R.drawable.maison));
        images.add(BitmapFactory.decodeResource(getResources(), R.drawable.planete));
        images.add(BitmapFactory.decodeResource(getResources(), R.drawable.pommes));
        images.add(BitmapFactory.decodeResource(getResources(), R.drawable.plage));
        images.add(BitmapFactory.decodeResource(getResources(), R.drawable.carre));

        image_bitmap = images.get(indexImage); //non mutable, sert à restaurer la bitmap
        mutableBitmap = image_bitmap.copy(Bitmap.Config.ARGB_8888, true); //mutable, on travaille sur cette bitmap

        iv.setImageBitmap(mutableBitmap);

        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* au click, on passe à l'image suivante dans la liste */
                indexImage++;
                indexImage = indexImage % (images.size());
                image_bitmap = images.get(indexImage);
                mutableBitmap = image_bitmap.copy(Bitmap.Config.ARGB_8888, true);
                iv.setImageBitmap(mutableBitmap);
                initBitmap();
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

    /* actionBar + satusBar Height */
    public void getTopHeight() {
        Window window = getWindow();
        TypedValue tval = new TypedValue();
        Rect rectangle = new Rect();

            /* get actionBar Height */
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tval, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tval.data, getResources().getDisplayMetrics());
        }

            /* get statusBar Height */
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        statusBarHeight = rectangle.top;
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
                scale = 1f;
                matrix.setScale(scale, scale);
                iv.setImageBitmap(mutableBitmap);
                iv.setImageMatrix(matrix);
                tX = tY = 0;
                initBitmap();
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

    /* coordonnées de l'écran vers coordonnées de la bitmap */
    public void mapPointToBitmap(float point[]) {
        //get status bar and action bar height
        getTopHeight();

        point[0] = (point[0] - iv.getLeft() - tX) / scale;
        point[1] = (point[1] - iv.getTop() - actionBarHeight - statusBarHeight - tY) / scale;
    }

    public class simpleGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            tX += -distanceX;
            tY += -distanceY;
            matrix.postTranslate(-distanceX, -distanceY);

            iv.setImageMatrix(matrix);

            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            float[] eCoords = {e.getX(), e.getY()};
            mapPointToBitmap(eCoords);

            try {

                int x = (int) eCoords[0];
                int y = (int) eCoords[1];

                for (int i = x - 10; i <= x + 10; i++) {
                    for (int j = y - 10; j <= y + 10; j++) {
                        mutableBitmap.setPixel(i, j, Color.RED);
                    }
                }
                iv.invalidate();
            } catch (java.lang.IllegalArgumentException exc) {

            }

            return super.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            float[] eCoords = {e.getX(), e.getY()};
            mapPointToBitmap(eCoords);

            try {
                int x = (int) eCoords[0];
                int y = (int) eCoords[1];

                if (!ZOOMED_IN)
                    scale *= 3;
                else
                    scale /= 3;
                ZOOMED_IN = !ZOOMED_IN;
                matrix.setScale(scale, scale, x, y);
                iv.setImageMatrix(matrix);
                float[] mvalues = new float[9];
                matrix.getValues(mvalues);
                tX = mvalues[2];
                tY = mvalues[5];
            } catch (java.lang.IllegalArgumentException exc) {

            }
            return super.onDoubleTap(e);
        }
    }

    /*classe non fonctionnelle */
    public class simpleOnScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        //float[] focusCoords = new float[2];

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            /*focusCoordsOut[0] = detector.getFocusX();
            focusCoordsOut[1] = detector.getFocusY();
            mapPointToBitmap(focusCoordsOut);*/
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            /*focusCoords[0] = detector.getFocusX();
            focusCoords[1] = detector.getFocusY();
            mapPointToBitmap(focusCoords);

            if(detector.getScaleFactor() > 1) {
                scale = scale * detector.getScaleFactor();
                scale = Math.max(0.1f, Math.min(scale, 5f));
                if (scale < 4.9f && scale > 0.11f ) {
                    matrix.setScale(scale, scale, focusCoords[0], focusCoords[1]);
                    iv.setImageMatrix(matrix);
                    float[] mvalues = new float[9];
                    matrix.getValues(mvalues);
                    tX = mvalues[2];
                    tY = mvalues[5];
                }
            }*/
            return super.onScale(detector);
        }
    }
}
