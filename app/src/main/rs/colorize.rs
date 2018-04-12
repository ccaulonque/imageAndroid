#pragma  version(1)
#pragma  rs  java_package_name(com.android.rssample)

uchar4  RS_KERNEL  colorize(uchar4 in) {
    const float4 pixelf = rsUnpackColor8888(in);
    uchar4 temp;
    uchar r = in.r / 255;
    uchar g = in.g / 255;
    uchar b = in.b / 255;
    uchar minRGB = min(r, min(g, b));
    uchar maxRGB = max(r, max(g, b));
    uchar deltaRGB = maxRGB - minRGB;
    uchar H,S,V;

    if(deltaRGB == 0){
      H = 0;
    }else{
      if(r == maxRGB){

      }else{
        if(g == maxRGB){

        }else{
          if(b == maxRGB){
            
          }
        }
      }
    }

    V = maxRGB;


    const float grey = dot(pixelf, weight);
    return rsPackColorTo8888(grey, grey, grey, pixelf.a);
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
