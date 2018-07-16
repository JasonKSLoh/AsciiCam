package com.lohjason.asciicam.Util;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

/**
 * ScreenUtils
 * Created by Jason on 16/7/2018.
 */
public class ScreenUtils {


    public static int[] getScreenDimensPx(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        assert wm != null;
        Display display = wm.getDefaultDisplay();
        Point   size    = new Point();
        display.getSize(size);

        return new int[]{size.x, size.y};
    }
}
