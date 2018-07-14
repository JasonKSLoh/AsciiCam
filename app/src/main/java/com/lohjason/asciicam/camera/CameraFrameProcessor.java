package com.lohjason.asciicam.camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;

import com.lohjason.asciicam.AsciiConverter;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * CameraFrameProcessor
 * Created by Jason on 14/7/2018.
 */
public class CameraFrameProcessor implements FrameProcessor {
    private AtomicBoolean isActive = new AtomicBoolean(false);
    private AsciiCallbackListener listener;

    public CameraFrameProcessor(AsciiCallbackListener listener) {
        this.listener = listener;
    }

    @Override
    public void start() {
        isActive.set(true);
    }

    @Override
    public void stop() {
        isActive.set(false);
    }

    @Override
    public void processFrame(byte[] frameBytes, int width, int height, int rotation) {
//        byte[] inputBytes = rotateArray(frameBytes, width, height, rotation);
        YuvImage              yuvImage = new YuvImage(frameBytes, ImageFormat.NV21, width, height, null);
        ByteArrayOutputStream os       = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, width, height), 100, os);
        byte[] jpegByteArray = os.toByteArray();
        Bitmap bitmap        = BitmapFactory.decodeByteArray(jpegByteArray, 0, jpegByteArray.length);

        AsciiConverter asciiConverter = new AsciiConverter(64, 64);
        String         asciiArt       = asciiConverter.getAscii(bitmap, false, true);
        listener.newAsciiImage(asciiArt);
    }



    public interface AsciiCallbackListener {
        void newAsciiImage(String ascii);
    }
}
