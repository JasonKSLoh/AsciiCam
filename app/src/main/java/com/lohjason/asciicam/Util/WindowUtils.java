package com.lohjason.asciicam.Util;

import android.content.Context;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.widget.TextView;

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
        float safetyRatio = 0.9f;
        float textSizeToUse = (width / (numCharsInRow / 2)) * safetyRatio;
        return textSizeToUse;
    }

    public static void setIdealTextSize2(Context context, TextView textView, int numCharsInRow){
        float textSize = getTextSizeForAsciiArt(context, numCharsInRow);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize );

        while (getNumCharsThatFitInScreenWidth(context, textView) < numCharsInRow){
            textSize -= 0.1f;
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize);
        }
        Log.d("+_", "Found ideal text size: " + textSize);
    }

    public static void setIdealTextSize(Context context, TextView textView, int numCharsInRow){
        TextPaint paint       = textView.getPaint();
        float width = getScreenWidth(context);
        float textSizeToUse = (width / (numCharsInRow) * 4);
        paint.setTextSize(textSizeToUse);

        while (getNumCharsThatFitInScreenWidth(context, textView) < numCharsInRow){
            textSizeToUse -= 1f;
            textView.getPaint().setTextSize(textSizeToUse);
        }
        Log.d("+_", "Found ideal text size: " + textSizeToUse);
    }

    private static int getNumCharsThatFitInScreenWidth(Context context, TextView textView){
        TextPaint paint       = textView.getPaint();
        float       wordwidth   = paint.measureText("a",0,1);
        float       screenwidth = context.getResources().getDisplayMetrics().widthPixels;
        float numCharsThatFit = screenwidth / wordwidth;
        Logg.d("+_", "Num chars that fit: " + numCharsThatFit);
        return (int)numCharsThatFit;
    }


}
