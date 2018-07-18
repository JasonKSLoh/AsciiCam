package com.lohjason.asciicam.camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;

import com.lohjason.asciicam.conversion.BitmapProcessor;
import com.lohjason.asciicam.conversion.ContrastingBitmapProcessor;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * CameraFrameProcessor
 * Created by Jason on 14/7/2018.
 */
public class CameraFrameProcessor implements FrameProcessor, ContrastingBitmapProcessor.BitmapProcessedListener {
    private AtomicBoolean isActive = new AtomicBoolean(false);
    private AsciiCallbackListener listener;
    private int targetWidth;
    private boolean invertDarkness = false;
    private int normalizationLevel = 0;

    private BitmapProcessor bitmapProcessor;

    public CameraFrameProcessor(BitmapProcessor bitmapProcessor, int targetWidth, AsciiCallbackListener listener) {
        this.listener = listener;
        this.bitmapProcessor = bitmapProcessor;
        this.targetWidth = targetWidth;
    }

    public void setNormalizationLevel(int normalizationLevel){
        this.normalizationLevel = normalizationLevel;
    }

    public void setInvertDarkness(boolean invertDarkness){
        this.invertDarkness = invertDarkness;
    }

    @Override
    public void processFrame(byte[] frameBytes, int width, int height, int rotation) {
        if(isActive.get()){
            return;
        }
        isActive.set(true);

        YuvImage              yuvImage = new YuvImage(frameBytes, ImageFormat.NV21, width, height, null);
        ByteArrayOutputStream os       = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, width, height), 100, os);
        byte[] jpegByteArray = os.toByteArray();
        Bitmap bitmap        = BitmapFactory.decodeByteArray(jpegByteArray, 0, jpegByteArray.length);

        bitmapProcessor.processImage(bitmap,
                                    rotation * 90,
                                     targetWidth,
                                     0xFF000000,
                                     normalizationLevel,
                                     invertDarkness);
    }

    @Override
    public void processingFinished(Bitmap bitmap) {
        listener.newAsciiImage(bitmap);
        isActive.set(false);
    }


    public interface AsciiCallbackListener {
        void newAsciiImage(Bitmap asciiBitmap);
    }
}
