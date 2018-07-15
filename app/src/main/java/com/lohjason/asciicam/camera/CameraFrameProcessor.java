package com.lohjason.asciicam.camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;

import com.lohjason.asciicam.converters.AsciiConverter;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * CameraFrameProcessor
 * Created by Jason on 14/7/2018.
 */
public class CameraFrameProcessor implements FrameProcessor {
    private AtomicBoolean isActive = new AtomicBoolean(false);
    private AsciiCallbackListener listener;
    private int targetWidth = 64;
    private int targetHeight = 64;
    private boolean useNormalization = true;
    private boolean invertDarkness = false;
    private int normalizationLevel = 0;

    public CameraFrameProcessor(AsciiCallbackListener listener, int targetWidth, int targetHeight) {
        this.listener = listener;
        if(targetHeight > 0){
            this.targetHeight = targetHeight;
        }
        if(targetWidth > 0){
            this.targetWidth = targetWidth;
        }
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

        Matrix matrix = new Matrix();
        matrix.setRotate(rotation * 90);
        if(rotation % 3 == 0){
            matrix.postScale(-1, 1, (float)width / 2, (float)height / 2);
        }
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);


        AsciiConverter asciiConverter = new AsciiConverter(targetHeight, targetWidth);
        String         asciiArt       = asciiConverter.getAscii(bitmap, invertDarkness, normalizationLevel);
        listener.newAsciiImage(asciiArt);
        isActive.set(false);
    }



    public interface AsciiCallbackListener {
        void newAsciiImage(String ascii);
    }
}
