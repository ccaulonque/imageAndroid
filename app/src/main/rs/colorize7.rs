#pragma  version(1)
#pragma  rs  java_package_name(com.android.rssample)

float randomH;
static const float4 weight = {0.299f, 0.587f, 0.114f, 0.0f};

uchar4 RS_KERNEL colorize(uchar4 in) {
    const float4 pixel = rsUnpackColor8888(in);
    uchar4 out;

    float r = pixel.r;
    float g = pixel.g;
    float b = pixel.b;

    float minRGB = min( r, min( g, b ) );
    float maxRGB = max( r, max( g, b ) );
    float deltaRGB = maxRGB - minRGB;

    float s = maxRGB == 0 ? 0 : (maxRGB - minRGB) / maxRGB;
    float v = maxRGB;

    //toutes les valeurs au dessus sont comprises entre 0 et 1

    float Hi = fmod((randomH / 60.0),6.0f);
    float f = randomH/60 - Hi;
    float l = v * (1 - s);
    float m = v * (1 - f*s);
    float n = v * (1 - (1-f) * s);
    if(Hi < 1){
        return rsPackColorTo8888(v,n,l,pixel.a);
    }if(Hi < 2){
        return rsPackColorTo8888(m,v,l,pixel.a);
    }if(Hi < 3){
        return rsPackColorTo8888(l,v,n,pixel.a);
    }if(Hi < 4){
        return rsPackColorTo8888(l,m,v,pixel.a);
    }if(Hi < 5){
        return rsPackColorTo8888(n,l,v,pixel.a);
    }if(Hi < 6){
        return rsPackColorTo8888(v,l,m,pixel.a);
    }
}
