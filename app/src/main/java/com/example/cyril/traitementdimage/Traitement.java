package com.example.cyril.traitementdimage;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.RenderScript;
import android.util.Log;

import com.android.rssample.ScriptC_grey;

import java.util.Arrays;

/**
 * Created by cyril on 23/03/18.
 */

public class Traitement {

    public static int[] getHisto(Bitmap b) {
        int greylvl, color, R, G, B;
        int[] histo = new int[256];
        Arrays.fill(histo, 0);
        int[] pixels = new int[b.getWidth() * b.getHeight()];
        b.getPixels(pixels, 0, b.getWidth(), 0, 0, b.getWidth(), b.getHeight());
        for (int i = 0; i < pixels.length; i++) {
            color = pixels[i];
            R = (color >> 16) & 0xff;
            G = (color >> 8) & 0xff;
            B = (color) & 0xff;
            greylvl = (R + G + B) / 3;
            histo[greylvl]++;
        }
        return histo;
    }

    public static void toGray(Bitmap b) {
        /*int[] pixels = new int[b.getWidth()*b.getHeight()];
        b.getPixels(pixels,0,b.getWidth(),0,0,b.getWidth(),b.getHeight());
        int color,greylvl,R,G,B;
        for(int k = 0; k < pixels.length; k++){
            color = pixels[k];
            R = (color >> 16) & 0xff;
            G = (color >>  8) & 0xff;
            B = (color      ) & 0xff;
            greylvl = (int)((0.3 * (double)R + 0.59 * (double)G + 0.11 * (double)B));
            pixels[k] = (0 & 0xff) << 24 | (greylvl & 0xff) << 16 | (greylvl & 0xff) << 8 | (greylvl & 0xff);
        }
        b.setPixels(pixels,0,b.getWidth(),0,0,b.getWidth(),b.getHeight());*/
        RenderScript rs = RenderScript.create(MyApplication.getAppContext());

        Allocation input = Allocation.createFromBitmap(rs, b);
        Allocation output = Allocation.createTyped(rs, input.getType());
        ScriptC_grey randomScript = new ScriptC_grey(rs);

        //todo
        //todo

        randomScript.forEach_toGrey(input, output);
        output.copyTo(b);
        output.destroy();
        randomScript.destroy();
        rs.destroy();
    }

    public static void toGrayExtension(Bitmap b, int contrasteMin, int contrasteMax) {
        int[] pixels = new int[b.getWidth() * b.getHeight()];
        b.getPixels(pixels, 0, b.getWidth(), 0, 0, b.getWidth(), b.getHeight());
        int color, greylvl, newgreylvl, R, G, B;
        int max = -1;
        int min = 256;
        for (int k = 0; k < pixels.length; k++) {
            color = pixels[k];
            R = (color >> 16) & 0xff;
            G = (color >> 8) & 0xff;
            B = (color) & 0xff;
            greylvl = (int) ((0.3 * (double) R + 0.59 * (double) G + 0.11 * (double) B));
            if (max < greylvl)
                max = greylvl;
            if (min > greylvl)
                min = greylvl;
        }

        int lookuptable[] = new int[256];
        for (int i = 0; i < 256; i++) {
            lookuptable[i] = ((contrasteMax - contrasteMin) * (i - min)) / (max - min) + contrasteMin;
        }

        for (int i = 0; i < pixels.length; i++) {
            color = pixels[i];
            R = (color >> 16) & 0xff;
            G = (color >> 8) & 0xff;
            B = (color) & 0xff;
            greylvl = (int) ((0.3 * (double) R + 0.59 * (double) G + 0.11 * (double) B));
            newgreylvl = lookuptable[greylvl];
            pixels[i] = (255 & 0xff) << 24 | (newgreylvl & 0xff) << 16 | (newgreylvl & 0xff) << 8 | (newgreylvl & 0xff);
        }

        b.setPixels(pixels, 0, b.getWidth(), 0, 0, b.getWidth(), b.getHeight());

    }

    public static void histoExtension(Bitmap b, int contrasteMin, int contrasteMax) {
        int[] pixels = new int[b.getWidth() * b.getHeight()];
        b.getPixels(pixels, 0, b.getWidth(), 0, 0, b.getWidth(), b.getHeight());
        int color, greylvl, R, G, B;
        int max = -1;
        int min = 256;
        for (int k = 0; k < pixels.length; k++) {
            color = pixels[k];
            R = (color >> 16) & 0xff;
            G = (color >> 8) & 0xff;
            B = (color) & 0xff;
            greylvl = (int) ((0.3 * (double) R + 0.59 * (double) G + 0.11 * (double) B));
            if (max < greylvl)
                max = greylvl;
            if (min > greylvl)
                min = greylvl;
        }

        int lookuptable[] = new int[256];
        for (int i = 0; i < 256; i++) {
            lookuptable[i] = ((contrasteMax - contrasteMin) * (i - min)) / (max - min) + contrasteMin;
        }

        for (int i = 0; i < pixels.length; i++) {
            color = pixels[i];
            R = (color >> 16) & 0xff;
            G = (color >> 8) & 0xff;
            B = (color) & 0xff;
            pixels[i] = (255 & 0xff) << 24 | (lookuptable[R] & 0xff) << 16 | (lookuptable[G] & 0xff) << 8 | (lookuptable[B] & 0xff);
        }

        b.setPixels(pixels, 0, b.getWidth(), 0, 0, b.getWidth(), b.getHeight());


    }

    public static void colorize(Bitmap b) {
        int[] pixels = new int[b.getWidth() * b.getHeight()];
        b.getPixels(pixels, 0, b.getWidth(), 0, 0, b.getWidth(), b.getHeight());
        int teinte = (int) (Math.random() * 360);
        float[] hsv = new float[3];
        int color;
        for (int k = 0; k < pixels.length; k++) {
            color = pixels[k];
            Color.colorToHSV(color, hsv);
            hsv[0] = teinte;
            pixels[k] = Color.HSVToColor(hsv);
        }
        b.setPixels(pixels, 0, b.getWidth(), 0, 0, b.getWidth(), b.getHeight());
    }

    public static void color_filter(Bitmap b, int c) {
        int[] pixels = new int[b.getWidth() * b.getHeight()];
        b.getPixels(pixels, 0, b.getWidth(), 0, 0, b.getWidth(), b.getHeight());
        int R, G, B, distance, greylvl;

        int R0 = (c >> 16) & 0xff;
        int G0 = (c >> 8) & 0xff;
        int B0 = (c) & 0xff;

        int seuil = 100;

        for (int k = 0; k < pixels.length; k++) {
            int color = pixels[k];
            R = (color >> 16) & 0xff;
            G = (color >> 8) & 0xff;
            B = (color) & 0xff;

            distance = (int) Math.sqrt(Math.pow((R0 - R), 2) + Math.pow((G0 - G), 2) + Math.pow((B0 - B), 2));

            if (distance > seuil) {
                greylvl = (R + G + B) / 3;
                pixels[k] = (255 & 0xff) << 24 | (greylvl & 0xff) << 16 | (greylvl & 0xff) << 8 | (greylvl & 0xff);
            }
        }
        b.setPixels(pixels, 0, b.getWidth(), 0, 0, b.getWidth(), b.getHeight());
    }

    public static void egalisation_couleurs(Bitmap b) {
        int[] pixels = new int[b.getWidth() * b.getHeight()];
        b.getPixels(pixels, 0, b.getWidth(), 0, 0, b.getWidth(), b.getHeight());
        int histo[] = getHisto(b);
        int R, G, B, color;

        int lookuptable[] = new int[256];
        int c;
        for (int i = 0; i < 256; i++) {
            c = 0;
            for (int j = 0; j < i; j++) {
                c += histo[j];
            }
            lookuptable[i] = c * 255 / (pixels.length);
        }

        for (int i = 0; i < pixels.length; i++) {
            color = pixels[i];
            R = (color >> 16) & 0xff;
            G = (color >> 8) & 0xff;
            B = (color) & 0xff;
            pixels[i] = (255 & 0xff) << 24 | (lookuptable[R] & 0xff) << 16 | (lookuptable[G] & 0xff) << 8 | (lookuptable[B] & 0xff);
        }

        b.setPixels(pixels, 0, b.getWidth(), 0, 0, b.getWidth(), b.getHeight());
    }

    //convolution qui renvoie un pixel modifié avec les composantes RGB
    private static int convolution(int[] origin, int x, int y, int width, int height, double[][] kernel, int n) {
        int color;
        int u2, v2;
        double k;
        double convor = 0;
        double convog = 0;
        double convob = 0;
        int R, G, B;
        for (int u = -n; u <= n; u++) {
            for (int v = -n; v <= n; v++) {
                /* rélfexion des bords */
                u2 = u;
                v2 = v;
                if (x + u < 0) u2 = ((-2) * x) - u - 1;
                if (x + u >= width) u2 = 2 * width - (2 * x) - u - 1;
                if (y + v < 0) v2 = ((-2) * y) - v - 1;
                if (y + v >= height) v2 = 2 * height - (2 * y) - v - 1;

                color = (origin[(y + v2) * width + (x + u2)]);

                R = (color >> 16) & 0xff;
                G = (color >> 8) & 0xff;
                B = (color) & 0xff;


                k = kernel[u + n][v + n];

                convor += ((double) (R) * k);
                convog += ((double) (G) * k);
                convob += ((double) (B) * k);

            }
        }
        return ((int) convor & 0xff) << 16 | ((int) convog & 0xff) << 8 | ((int) convob & 0xff);
    }

    //convolution qui à partir de pixels en niveau de gris, renvoie un seul double non borné
    private static double convolutiongrey(int[] origin, int x, int y, int width, int height, double[][] kernel, int n) {
        int color;
        int u2, v2;
        double k;
        double convo = 0;
        int B;
        for (int u = -n; u <= n; u++) {
            for (int v = -n; v <= n; v++) {
                /* rélfexion des bords */
                u2 = u;
                v2 = v;
                if (x + u < 0) u2 = ((-2) * x) - u - 1;
                if (x + u >= width) u2 = 2 * width - (2 * x) - u - 1;
                if (y + v < 0) v2 = ((-2) * y) - v - 1;
                if (y + v >= height) v2 = 2 * height - (2 * y) - v - 1;

                color = (origin[(y + v2) * width + (x + u2)]);

                B = (color) & 0xff;

                k = kernel[u + n][v + n];

                convo += ((double) (B) * k);

            }
        }
        return convo;
    }

    public static void flou(Bitmap b, int n) {
        int l = n * 2 + 1;
        double[][] noyauflou = new double[l][l];
        for (int x = 0; x < l; x++) {
            for (int y = 0; y < l; y++) {
                noyauflou[x][y] = 1.0 / ((double) (l * l));
            }
        }
        int width = b.getWidth(), height = b.getHeight();
        int[] pixels = new int[width * height];
        int[] origin = new int[width * height];
        b.getPixels(origin, 0, width, 0, 0, width, height);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                pixels[y * width + x] = convolution(origin, x, y, width, height, noyauflou, n);
            }
        }
        b.setPixels(pixels, 0, width, 0, 0, width, height);
    }

    public static void flougaussien(Bitmap b, int n) {
        double[][] noyaugauss = new double[2 * n + 1][2 * n + 1];
        double coeff, somme = 0;
        String DEBUG = "";
        for (int x = -n; x <= n; x++) {
            for (int y = -n; y <= n; y++) {
                coeff = Math.exp(-((double) (x * x + y * y) / ((double) (2 * n * n))));
                DEBUG += (String.format("%.3f", coeff) + "; ");
                somme += coeff;
            }
            DEBUG += "\n";
        }
        Log.v("FILTRE", DEBUG);
        double k = 1 / somme;
        for (int x = -n; x <= n; x++) {
            for (int y = -n; y <= n; y++) {
                coeff = Math.exp(-((double) (x * x + y * y) / ((double) (2 * n * n))));
                noyaugauss[x + n][y + n] = coeff * k;
            }
        }
        int width = b.getWidth(), height = b.getHeight();
        int[] pixels = new int[width * height];
        int[] origin = new int[width * height];
        b.getPixels(origin, 0, width, 0, 0, width, height);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                pixels[y * width + x] = convolution(origin, x, y, width, height, noyaugauss, n);
            }
        }
        b.setPixels(pixels, 0, width, 0, 0, width, height);
    }

    private static void gradienty(int[] pixels, int width, int height) {
        double[][] noyaugradient = {{-1, 0, 1}, {-1, 0, 1}, {-1, 0, 1}};
        int[] origin = new int[pixels.length];
        for (int i = 0; i < origin.length; i++) {
            origin[i] = pixels[i];
        }
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                pixels[y * width + x] = (int) convolutiongrey(origin, x, y, width, height, noyaugradient, 1);
            }
        }
    }

    private static void gradientx(int[] pixels, int width, int height) {
        double[][] noyaugradient = {{-1, -1, -1}, {0, 0, 0}, {1, 1, 1}};
        int[] origin = new int[pixels.length];
        for (int i = 0; i < origin.length; i++) {
            origin[i] = pixels[i];
        }
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                pixels[y * width + x] = (int) convolutiongrey(origin, x, y, width, height, noyaugradient, 1);
            }
        }
    }

    public static void toBW(Bitmap b) {
        int[] pixels = new int[b.getWidth() * b.getHeight()];
        b.getPixels(pixels, 0, b.getWidth(), 0, 0, b.getWidth(), b.getHeight());
        int color, greylvl, R, G, B;

        int histo[] = getHisto(b);
        int middle = 255;

        int c = 0;
        for (int i = 0; i < 256; i++) {
            c += histo[i];
            if (c > pixels.length / 2) {
                middle = i;
                break;
            }
        }

        for (int k = 0; k < pixels.length; k++) {
            color = pixels[k];
            R = (color >> 16) & 0xff;
            G = (color >> 8) & 0xff;
            B = (color) & 0xff;
            greylvl = (R + G + B) / 3;
            greylvl = (greylvl >= middle) ? 255 : 0;
            pixels[k] = (255 & 0xff) << 24 | (greylvl & 0xff) << 16 | (greylvl & 0xff) << 8 | (greylvl & 0xff);
        }
        b.setPixels(pixels, 0, b.getWidth(), 0, 0, b.getWidth(), b.getHeight());
    }

    public static void gradient(Bitmap b) {
        toGray(b);
        int width = b.getWidth(), height = b.getHeight();
        int greyx, greyy, m;
        int[] convosx = new int[width * height];
        int[] convosy = new int[width * height];
        int[] pixels = new int[width * height];
        b.getPixels(convosx, 0, width, 0, 0, width, height);
        b.getPixels(convosy, 0, width, 0, 0, width, height);
        gradientx(convosx, width, height);
        gradienty(convosy, width, height);
        for (int i = 0; i < pixels.length; i++) {
            greyx = convosx[i];
            greyy = convosy[i];

            m = (int) Math.sqrt(greyx * greyx + greyy * greyy);
            if (m > 255) m = 255;
            pixels[i] = (255 & 0xff) << 24 | (m & 0xff) << 16 | (m & 0xff) << 8 | (m & 0xff);
        }
        b.setPixels(pixels, 0, width, 0, 0, width, height);
    }

    public static void laplace(Bitmap b) {
        toGray(b);
        double[][] noyaulaplace = {{0, 1, 0}, {1, -8, 1}, {0, 1, 0}};
        for (int x = -1; x < 1; x++) {
            for (int y = -1; y < 1; y++) {
                noyaulaplace[1 + x][1 + y] = (double) x;
            }
        }
        int width = b.getWidth(), height = b.getHeight();
        int[] pixels = new int[width * height];
        int[] origin = new int[width * height];
        int convo, R, G, B;
        b.getPixels(origin, 0, width, 0, 0, width, height);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                convo = (int) convolutiongrey(origin, x, y, width, height, noyaulaplace, 1);
                if (convo < 0)
                    convo = -convo;
                if (convo > 255)
                    convo = 255;

                pixels[y * width + x] = (255 & 0xff) << 24 | (convo & 0xff) << 16 | (convo & 0xff) << 8 | (convo & 0xff);
            }
        }
        b.setPixels(pixels, 0, width, 0, 0, width, height);
    }
}
