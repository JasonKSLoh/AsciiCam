package com.lohjason.asciicam;

/**
 * TextConverter
 * Created by jason on 13/7/18.
 */
public class TextConverter {

    private static final String DISPLAY_CHARS         = "$@B%8&WM#*oahkbdqwmZO0QLCJUYXzcvunxrjft/|)1}]?-_+~>i!lI;:,\"^`'. ";
    private static final char[] DISPLAY_CHAR_ARRAY = DISPLAY_CHARS.toCharArray();
    private static final int    NUM_BRIGHTNESS_LEVELS = DISPLAY_CHAR_ARRAY.length;
    private static final float SCALE_FACTOR = 255f / NUM_BRIGHTNESS_LEVELS;

    public static char getCharForBrightnessInverse(int brightness){
        float brightnessFloat = (float)brightness;
        int index = Math.round(brightnessFloat / SCALE_FACTOR);
        if(index >= NUM_BRIGHTNESS_LEVELS){
            index = NUM_BRIGHTNESS_LEVELS - 1;
        }
        return DISPLAY_CHAR_ARRAY[index];
    }


    public static char getCharForBrightness(int brightness){
        float brightnessFloat = 255f - (float)brightness;
        int index = Math.round(brightnessFloat / SCALE_FACTOR);
        if(index >= NUM_BRIGHTNESS_LEVELS){
            index = NUM_BRIGHTNESS_LEVELS - 1;
        }
        return DISPLAY_CHAR_ARRAY[index];
    }

}
