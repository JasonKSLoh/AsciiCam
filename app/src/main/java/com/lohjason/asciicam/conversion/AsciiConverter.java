package com.lohjason.asciicam.conversion;

/**
 * AsciiConverter
 * Created by jason on 13/7/18.
 */
public class AsciiConverter {

    private static final String DISPLAY_CHARS         = "$@B%8&WM#*oahkbdqwmZO0QLCJUYXzcvunxrjft/|)1}]?-_+~>i!lI;:,\"^`'. ";
    private static final char[] DISPLAY_CHAR_ARRAY    = DISPLAY_CHARS.toCharArray();
    private static final int    NUM_BRIGHTNESS_LEVELS = DISPLAY_CHAR_ARRAY.length;
    private static final float  SCALE_FACTOR          = 256 / NUM_BRIGHTNESS_LEVELS;

    public static char getCharForBrightness(int brightness, boolean invertDarkness) {
        float brightnessFloat;
        if (!invertDarkness) {
            brightnessFloat = (float) brightness;
        } else {
            brightnessFloat = 255f - brightness;
        }
        int index = (int) (brightnessFloat / SCALE_FACTOR);
        if (index >= NUM_BRIGHTNESS_LEVELS) {
            index = NUM_BRIGHTNESS_LEVELS - 1;
        }
        return DISPLAY_CHAR_ARRAY[index];
    }


    public static char getCharForBrightness(int brightness, boolean invertDarkness, int minBrightness, int maxBrightness) {
        float   range         = maxBrightness - minBrightness;
        float   relativeValue = brightness - minBrightness;
        range = range == 0 ? 1 : range;
        float normalizedBrightness = (255f * relativeValue) / (range);

        float brightnessFloat;
        if (!invertDarkness) {
            brightnessFloat = normalizedBrightness;
        } else {
            brightnessFloat = 255f - normalizedBrightness;
        }
        int index = (int) (brightnessFloat / SCALE_FACTOR);
        if (index >= NUM_BRIGHTNESS_LEVELS) {
            index = NUM_BRIGHTNESS_LEVELS - 1;
        }
        if(index < 0){
            index = 0;
        }
        return DISPLAY_CHAR_ARRAY[index];
    }

}
