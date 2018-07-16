package com.lohjason.asciicam;

import android.app.Application;

import com.lohjason.asciicam.Util.BitmapHolder;
import com.lohjason.asciicam.Util.CameraConsts;
import com.lohjason.asciicam.Util.SharedPrefsUtils;

/**
 * MainApp
 * Created by Jason on 15/7/2018.
 */
public class MainApp extends Application {

    private BitmapHolder bitmapHolder;

    @Override
    public void onCreate() {
        super.onCreate();

        initializePreferences();
        bitmapHolder = new BitmapHolder();
    }

    public BitmapHolder getBitmapHolder() {
        return bitmapHolder;
    }


    private void initializePreferences(){
        if(!SharedPrefsUtils.getHasInitialized(this)){
            SharedPrefsUtils.setFps(this, CameraConsts.DEFAULT_FPS);
            SharedPrefsUtils.setImageWidth(this, CameraConsts.DEFAULT_IMAGE_WIDTH);
            SharedPrefsUtils.setUseFrontCamera(this, false);
            SharedPrefsUtils.setInvert(this, false);
            SharedPrefsUtils.setNormalization(this, true);
            SharedPrefsUtils.setHasInitialized(this);
        }
    }
}
