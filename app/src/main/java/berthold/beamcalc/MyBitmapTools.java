/*
 * MyBitmapTools.java
 *
 * Created by Berthold Fritz
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License:
 * https://creativecommons.org/licenses/by-nc-sa/4.0/
 *
 * Last modified 11/1/18 2:56 PM
 */

/*
 * @rem:A collection of gfx algorithm's: This is the latest Version! (29.03.2019)@@
 *
 */

package berthold.beamcalc;

/*
 * MyBitmapTools.java
 *
 * Created by Berthold Fritz
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License:
 * https://creativecommons.org/licenses/by-nc-sa/4.0/
 *
 * Last modified 12/18/18 11:32 PM
 */

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.util.Log;

public class MyBitmapTools {

    // Debug
    static String tag=MyBitmapTools.class.getSimpleName();

    /**
     * Calculate sample size.
     *
     * @rem: Calculates proper sample size for bitmaps according to their width and height.@@
     * @rem: This is the recomented method by google to reduce the bitmaps memory footprint.@@
     * @rem: Theory: The more the size is reduced, the more data compression can be done.@@
     *
     */

    public static int calcSampleSize(int height,int width,int outHeight,int outWidth)
    {
        int sampleSize=1;

        if(height>outHeight || width>outWidth) {
            while ((height > outHeight) && (width > outWidth)) {
                height = height / 2;
                width = width / 2;
                sampleSize = sampleSize * 2;
            }
        }
        return sampleSize;
    }

    /**
     * Scale bitmap
     *
     * Scales a bitmap and keeps it's aspect ratio
     *
     * ToDo: Gets wrong result when the destination size is bigger then the source size.....
     *
     * @param b         Bitmap to be scaled
     * @param destW     New size
     * @param destH
     * @return          Scaled bitmap
     */

    public static Bitmap scaleBitmap(Bitmap b, float destW, float destH, String fileName){

        // Calculate new width and height, keep aspect ratio
        float sourceRatio;
        float sourceW=b.getWidth();
        float sourceH=b.getHeight();

        // Calculate...
        sourceRatio=1;                      // Remains 1 if pic is rectancular
        if (sourceW > sourceH){             // Source picture is in landscape mode?
            sourceRatio=sourceH/sourceW;
            destH=destW*sourceRatio;
            destW=destH/sourceRatio;
        }
        if (sourceW< sourceH){
            // Source picture is in portrait mode
            sourceRatio=sourceW/sourceH;
            destW=destH*sourceRatio;
            destH=destW/sourceRatio;
        }
        if (sourceH==sourceW){
            // Source picture is rectancular
            sourceRatio=destW/sourceW;
            destW=sourceW*sourceRatio;
            destH=sourceH*sourceRatio;
        }
        Log.v(tag,"Name:"+fileName+"Ratio:"+sourceRatio+" source width "+sourceW+"   source height"+sourceH);

        // Scale picture to screen size
        Bitmap changedBitmap= Bitmap.createScaledBitmap(b,(int)destW,(int)destH,false);
        Log.v(tag,"Src dpi:"+b.getDensity() +"  Destination picture => Ratio:"+sourceRatio+"  destW "+(int)destW+"   destH "+(int)destH);

        return changedBitmap;
    }

    /**
     * Get dominant color at top of a Bitmap
     *
     * @param b     Bitmap to get the color from
     * @return      Dominant color at top of image
     *
     */

    public static int getDominatColorAtTop(Bitmap b) {

        // Get dominant color of picture and set background color of toolbar and layout accordingly
        // This is a neat little trick I got from 'stackOverflow'.
        Bitmap c = Bitmap.createScaledBitmap(b, 1, b.getHeight(), true);
        int colorTop = c.getPixel(0, 0);
        c.recycle();                        // Remove image from memory, don't wait for GC

        return colorTop;
    }

    /**
     * Get dominant color at bottom of a Bitmap
     *
     */

    public static int getDominantColorAtBottom(Bitmap b){

        // Get dominant color of picture and set background color of toolbar and layout accordingly
        // This is a neat little trick I got from 'stackOverflow'.
        Bitmap c = Bitmap.createScaledBitmap(b, 1, b.getHeight(), true);
        int colorBottom = c.getPixel(0, b.getHeight()-1);
        c.recycle();                        // Remove image from memory, don't wait for GC

        return colorBottom;
    }

    /**
     * Returns a circular picture from the given bitmap
     *
     * @param   b               Bitmap to process
     * @return  changedBitmap   Processed bitmap
     *
     */

    public static Bitmap toRoundedImage(Bitmap b, DisplayMetrics m)
    {
        // Create a mutable bitmap
        Bitmap changedBitmap= Bitmap.createBitmap(m,b.getWidth(),b.getHeight(), Bitmap.Config.ARGB_8888);

        // Calc max rad for pic
        int radius;

        int width=b.getWidth();         // Size
        int height=b.getHeight();

        int cx=width/2;                  // Center of pic
        int cy=b.getHeight()/2;

        if(width>b.getHeight()) radius=height/2;
        else radius=width/2;

        for (int y=0;y<height;y++){
            for (int x=0;x<width;x++){

                if (((x-cx)*(x-cx)+(y-cy)*(y-cy))<radius*radius ) {

                    int color = b.getPixel(x, y);        // Get pixel from source
                    changedBitmap.setPixel(x, y, color);
                } else {
                    changedBitmap.setPixel(x,y, Color.TRANSPARENT);
                }
            }
        }
        // Cut edges. This way the pic will be as heigh and as width the radius is
        changedBitmap=toRectangle(changedBitmap);
        return changedBitmap;
    }

    /**
     * Draws a bitmap into another. Allows to select transparent color which
     * will not be draw......
     */

    public static Bitmap drawTransparentBitmap(Bitmap destBitmap,Bitmap transparentBitmap,int transparentColor,float xPos,float yPos){

        int transparentBitmapWidth=transparentBitmap.getWidth();
        int transparentBitmapHeight=transparentBitmap.getHeight();

        for (int y=0;y<transparentBitmapHeight;y++) {
            for (int x = 0; x <transparentBitmapWidth; x++) {
                int color = transparentBitmap.getPixel(x, y);
                if (color != transparentColor)
                    destBitmap.setPixel((int)(xPos+x), (int)(y+yPos), color);
            }
        }
        return destBitmap;
    }

    /**
     * Returns rectangular bitmap, which is centered on the source bitmap.
     */

    public static Bitmap toRectangle (Bitmap b){

        int width=b.getWidth();         // Size
        int height=b.getHeight();
        int destB;                      // Destination size => destB x destB
        Bitmap changedBitmap=null;

        int sx;
        int sy;

        if (width>height) destB=height; // Landscape mode?
        else destB=width;               // Portraid mode?

        if (width==height) return b;    // If already rectancular, return bitmap unchanged.....

        changedBitmap = Bitmap.createBitmap(destB, destB, Bitmap.Config.ARGB_8888);

        sy = height / 2 - destB / 2;
        for (int y = 0; y <= destB - 1; y++) {
            sx = width / 2 - destB / 2;
            for (int x = 0; x <= destB - 1; x++) {
                changedBitmap.setPixel(x, y, b.getPixel(sx++, sy));
            }
            sy++;
        }
        return changedBitmap;
    }

    /**
     * Returns a monochrome image of the image given.
     * Uses Floyd- Steinberg single line dithering algorythmen
     *
     * @param   b               Bitmap to process
     * @param   thr             Threshold
     * @return  changedBitmap   Changed bitmap
     *
     */

    public static Bitmap toMonocrome (Bitmap b, float thr, DisplayMetrics m)
    {
        // Create a mutable bitmap

        Bitmap changedBitmap= Bitmap.createBitmap(m,b.getWidth(),b.getHeight(), Bitmap.Config.ARGB_8888);


        int width=b.getWidth();         // Size
        int height=b.getHeight();

        // even lines

        for (int y=0;y<height;y=y+2) {
            float error = 0;
            for (int x = 0; x < width; x++) {
                int color = b.getPixel(x, y)*-1;

                int r=(color>>>16) & 0xFF;
                int g=(color>>>8) & 0xFF;
                int bl=(color>>>0) & 0xFF;

                float lum=(r*0.2126f+g*0.7125f+bl*0.0722f)/255;

                lum=lum+ error;

                if (lum <= thr) {
                    error = lum;
                    changedBitmap.setPixel(x, y, 0);
                }
                if (lum > thr) {
                    //error = lum ;
                    changedBitmap.setPixel(x, y, Color.BLACK);
                    error = 0;
                }
            }
        }

        // Odd lines

        for (int y = 1; y <= height-1; y = y + 2) {
            float error = 0;
            for (int x = width-1; x!=0; x--) {
                int color = b.getPixel(x, y)*-1;

                int r=(color>>>16) & 0xFF;
                int g=(color>>>8) & 0xFF;
                int bl=(color>>>0) & 0xFF;

                float lum=(r*0.2126f+g*0.7125f+bl*0.0722f)/255;

               lum=lum+ error;

                if (lum <= thr) {
                    error = lum;
                    changedBitmap.setPixel(x, y, 0);
                }
                if (color > thr) {
                    //error = lum ;
                    changedBitmap.setPixel(x, y, Color.BLACK);
                    error = 0;

                }
            }
        }
        return changedBitmap;
    }

    /**
     * Returns a pixelated picture from the given bitmap
     *
     * @param   b               Bitmap to process
     * @return  changedBitmap   Processed bitmap
     *
     * todo: Display metrics, why?
     *
     */

    public static Bitmap toPixelatedImage(Bitmap b, DisplayMetrics m, int pixelSize)
    {
        // Create a mutable bitmap
        Bitmap changedBitmap= Bitmap.createBitmap(m,b.getWidth(),b.getHeight(), Bitmap.Config.ARGB_8888);

        int width=b.getWidth();         // Size
        int height=b.getHeight();

        Canvas c=new Canvas(changedBitmap);

        Paint p=new Paint();
        p.setAntiAlias(false);

        for (int y=0;y<height;y=y+pixelSize){
            for (int x=0;x<width;x=x+pixelSize){

                int color = b.getPixel(x, y);        // Get pixel from source
                p.setColor(color);
                c.drawRect(x,y,x+pixelSize,y+pixelSize,p);
            }
        }
        return changedBitmap;
    }
}
