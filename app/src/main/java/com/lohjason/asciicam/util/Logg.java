package com.lohjason.asciicam.util;

import android.util.Log;

import com.lohjason.asciicam.BuildConfig;

/**
 * Logg
 * Created by Jason on 14/7/2018.
 */
public class Logg {

    private static boolean isDebug = BuildConfig.DEBUG;

    public static void d(String tag, String message){
        d(tag, message, null);
    }
    public static void d(String tag, String message, Throwable t){
        if(isDebug){
            Log.d(tag, message, t);
        }
    }
    public static void e(String tag, String message){
        e(tag, message, null);
    }
    public static void e(String tag, String message, Throwable t){
        if(isDebug){
            Log.e(tag, message, t);
        }
    }

}
