package com.lohjason.asciicam;

/**
 * TextConverter
 * Created by jason on 13/7/18.
 */
public class TextConverter {

    private static final String DISPLAY_CHARS         = "$@B%8&WM#*oahkbdqwmZO0QLCJUYXzcvunxrjft/|)1}]?-_+~>i!lI;:,\"^`'. ";
    private static final char[] DISPLAY_CHAR_ARRAY    = DISPLAY_CHARS.toCharArray();
    private static final int    NUM_BRIGHTNESS_LEVELS = DISPLAY_CHAR_ARRAY.length;
    private static final float  SCALE_FACTOR          = 256 / NUM_BRIGHTNESS_LEVELS;


    public static char getCharForBrightness(int brightness, boolean asWhiteOnBlack) {
        float brightnessFloat;
        if (!asWhiteOnBlack) {
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


    public static char getCharForBrightness(int brightness, boolean asWhiteOnBlack, int minBrightness, int maxBrightness) {
        float   range         = maxBrightness - minBrightness;
        float   relativeValue = brightness - minBrightness;
        range = range == 0 ? 1 : range;
        float normalizedBrightness = (255f * relativeValue) / (range);

        float brightnessFloat;
        if (!asWhiteOnBlack) {
            brightnessFloat = normalizedBrightness;
        } else {
            brightnessFloat = 255f - normalizedBrightness;
        }
        int index = (int) (brightnessFloat / SCALE_FACTOR);
        if (index >= NUM_BRIGHTNESS_LEVELS) {
            index = NUM_BRIGHTNESS_LEVELS - 1;
        }
        return DISPLAY_CHAR_ARRAY[index];
    }

}
