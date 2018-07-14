package com.lohjason.asciicam.Util;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * WindowUtils
 * Created by Jason on 14/7/2018.
 */
public class WindowUtils {

    public static float getScreenHeight(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.heightPixels / displayMetrics.density;
    }

    public static float getScreenWidth(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.widthPixels / displayMetrics.density;
    }

    public static int getScreenHeightPixels(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.heightPixels;
    }

    public static int getScreenWidthPixels(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.widthPixels;
    }

    public static float getTextSizeForAsciiArt(Context context, int numCharsInRow){
        float width = getScreenWidth(context);
        float safetyRatio = 1f;
        float textSizeToUse = (width / (numCharsInRow / 2)) * safetyRatio;
        return textSizeToUse;
    }


}
