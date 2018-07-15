package com.lohjason.asciicam.Util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * SharedPrefsUtils
 * Created by Jason on 15/7/2018.
 */
public class SharedPrefsUtils {
    private static final String KEY_INITIALIZED = "key_initialized";

    public static float getFps(Context context){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getFloat(CameraConsts.KEY_FPS, CameraConsts.DEFAULT_FPS);
    }
    public static int getImageWidth(Context context){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getInt(CameraConsts.KEY_IMG_WIDTH, CameraConsts.DEFAULT_IMAGE_WIDTH);
    }

    public static boolean getNormalization(Context context){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(CameraConsts.KEY_NORMALIZATION, true);
    }
    public static boolean getInvert(Context context){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(CameraConsts.KEY_INVERT, CameraConsts.DEFAULT_INVERT);
    }
    public static boolean getUseFrontCamera(Context context){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(CameraConsts.KEY_USE_FRONT_CAMERA, CameraConsts.DEFAULT_USE_FRONT_CAMERA);
    }

    public static void setFps(Context context, float fps){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(CameraConsts.KEY_FPS, fps);
        editor.apply();
    }
    public static void setImageWidth(Context context, int imageWidth){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(CameraConsts.KEY_IMG_WIDTH, imageWidth);
        editor.apply();
    }
    public static void setNormalization(Context context, boolean normalization){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(CameraConsts.KEY_NORMALIZATION, normalization);
        editor.apply();
    }
    public static void setInvert(Context context, boolean invert){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(CameraConsts.KEY_INVERT, invert);
        editor.apply();
    }
    public static void setUseFrontCamera(Context context, boolean useFrontCamera){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(CameraConsts.KEY_USE_FRONT_CAMERA, useFrontCamera);
        editor.apply();
    }

    @SuppressLint("ApplySharedPref")
    public static void setHasInitialized(Context context){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_INITIALIZED, true);
        editor.commit();
    }
    public static boolean getHasInitialized(Context context){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(KEY_INITIALIZED, false);
    }

}
