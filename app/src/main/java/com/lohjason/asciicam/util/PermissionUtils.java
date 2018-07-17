package com.lohjason.asciicam.util;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.lohjason.asciicam.BuildConfig;

/**
 * PermissionUtils
 * Created by Jason on 15/7/2018.
 */
public class PermissionUtils {

    public static final int REQUEST_CODE_CAMERA = 1;

    public static boolean hasCameraPermission(Context context){
        int hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA);
        return hasPermission == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestCameraPermission(AppCompatActivity appCompatActivity){
        ActivityCompat.requestPermissions(appCompatActivity, new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_CAMERA);
    }

    public static boolean canRequestCameraPermission(AppCompatActivity appCompatActivity){
        return ActivityCompat.shouldShowRequestPermissionRationale(appCompatActivity, Manifest.permission.CAMERA);
    }

    public static void openSettingsPage(AppCompatActivity appCompatActivity){
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        appCompatActivity.startActivity(intent);
    }
}
