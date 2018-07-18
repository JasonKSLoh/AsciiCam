package com.lohjason.asciicam.util;

/**
 * CameraConsts
 * Created by Jason on 15/7/2018.
 */
public class CameraConsts {

    public static final String KEY_FPS              = "key_fps";
    public static final String KEY_IMG_WIDTH        = "key_image_width";
    public static final String KEY_THRESHOLDING     = "key_thresholding";
    public static final String KEY_INVERT           = "key_invert";
    public static final String KEY_USE_FRONT_CAMERA = "key_use_front_camera";

    public static final int REQUEST_CODE_PREVIEW = 101;

    public static float[] FPS_VALUES   = new float[]{12f, 24f, 36f, 48f, 60f};
    public static int[]   IMAGE_WIDTHS = new int[]{30, 60, 120, 240, 480};

    public static final float DEFAULT_FPS = 24;
    public static final int DEFAULT_IMAGE_WIDTH = 120;
    public static final boolean DEFAULT_USE_THRESHOLDING = false;
    public static final boolean DEFAULT_USE_FRONT_CAMERA = false;
    public static final boolean DEFAULT_INVERT = false;

    public static final int MAX_NORMALIZATION = 50;
    public static final float NORMALIZATION_SCALE = 100f / MAX_NORMALIZATION;
}
