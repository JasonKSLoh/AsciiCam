package com.lohjason.asciicam.Util;

import android.graphics.Bitmap;

/**
 * BitmapHolder
 * Created by Jason on 16/7/2018.
 */
public class BitmapHolder {
    private Bitmap bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
