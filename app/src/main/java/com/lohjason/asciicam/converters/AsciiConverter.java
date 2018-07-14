package com.lohjason.asciicam.converters;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * AsciiConverter
 * Created by Jason on 14/7/2018.
 */
public class AsciiConverter {

    private int targetHeight;
    private int targetWidth;

    public AsciiConverter(int targetHeight, int targetWidth){
        this.targetHeight = targetHeight;
        this.targetWidth = targetWidth;
    }

    //Bitmap must be ARG_B8888
    public String getAscii(Bitmap bitmap, boolean useBlackBackground, boolean useNormalization) {
        float heightScale = bitmap.getHeight() / targetHeight;
        float widthScale  = bitmap.getWidth() / targetWidth;
        float scale       = heightScale > widthScale ? heightScale : widthScale;

        targetHeight = (int) (bitmap.getHeight() / scale);
        targetWidth = (int) (bitmap.getWidth() / scale);

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true);
        int[]  scaledPixels = new int[targetWidth * targetHeight];
        scaledBitmap.getPixels(scaledPixels, 0, targetWidth, 0, 0, targetWidth, targetHeight);

//        Bitmap      bmpMonochrome = Bitmap.createBitmap(scaledBitmap.getWidth(), scaledBitmap.getHeight(), Bitmap.Config.ARGB_8888);
//        Canvas      canvas        = new Canvas(bmpMonochrome);
//        ColorMatrix ma            = new ColorMatrix();
//        ma.setSaturation(0);
//        Paint paint = new Paint();
//        paint.setColorFilter(new ColorMatrixColorFilter(ma));
//        canvas.drawBitmap(scaledBitmap, 0, 0, paint);

        int[] brightness   = new int[scaledPixels.length / 2];
        int   outputHeight = targetHeight / 2;

        int maxBrightness = 0;
        int minBrightness = 255;


        List<Integer> brightnessList = new ArrayList<>(scaledPixels.length / 2);
        for (int i = 0; i < targetHeight; i++) {
            if (i % 2 == 1) {
                for (int j = 0; j < targetWidth; j++) {
                    int color = scaledPixels[(i * targetWidth) + j];
//                    int red   = (color >> 16) & 0xff;
//                    int green = (color >> 8) & 0xff;
//                    int blue  = color & 0xff;

                    int red = (color >> 16 & 0xff) << 2;
                    int green = (color >> 18 & 0xff);
                    int blue = (color & 0xff) << 1;

//                    int pixelBrightness = (red + green + blue) / 3;
                    int pixelBrightness = (red + green + blue) / 7;
                    int index = ((i / 2) * targetWidth) + j;
                    brightness[index] = pixelBrightness;

                    minBrightness = pixelBrightness < minBrightness ? pixelBrightness : minBrightness;
                    maxBrightness = pixelBrightness > maxBrightness ? pixelBrightness : maxBrightness;
                    brightnessList.add(pixelBrightness);
                }
            }
        }

        Collections.sort(brightnessList);
        int minCutoff = brightnessList.get(brightness.length * 35 / 100);
        int maxCutoff = brightnessList.get(brightness.length * 65 / 100);

        String[] asciiArt = new String[outputHeight];

        if(useNormalization){
            for (int i = 0; i < outputHeight; i++) {
                char[] chars = new char[targetWidth];
                for (int j = 0; j < targetWidth; j++) {
                    int index = targetWidth * i + j;
//                    chars[j] = TextConverter.getCharForBrightness(brightness[index], useBlackBackground, minBrightness, maxBrightness);
                    chars[j] = TextConverter.getCharForBrightness(brightness[index], useBlackBackground, minCutoff, maxCutoff);
                }
                asciiArt[i] = new String(chars);
            }
        } else {
            for (int i = 0; i < outputHeight; i++) {
                char[] chars = new char[targetWidth];
                for (int j = 0; j < targetWidth; j++) {
                    int index = targetWidth * i + j;
                    chars[j] = TextConverter.getCharForBrightness(brightness[index], useBlackBackground);
                }
                asciiArt[i] = new String(chars);
            }
        }



        StringBuilder sb = new StringBuilder();
        for (String s : asciiArt) {
            sb.append(s).append('\n');
        }

        return sb.toString();
    }



}
