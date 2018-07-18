package com.lohjason.asciicam.camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;

import com.google.android.gms.common.images.Size;
import com.lohjason.asciicam.conversion.BitmapProcessor;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * CameraFrameProcessor
 * Created by Jason on 14/7/2018.
 */
public class CameraFrameProcessor implements FixedSizeFrameProcessor,
                                             BitmapProcessor.BitmapProcessedListener {
    private AtomicBoolean isActive = new AtomicBoolean(false);
    private AsciiCallbackListener listener;
    private int targetWidth;
    private boolean invertDarkness = false;
    private int normalizationLevel = 0;
    private int color = 0xFF000000;

    private Size                  frameSize;
    private ByteArrayOutputStream byteArrayOutputStream;
    private BitmapProcessor       bitmapProcessor;
    private Bitmap                bitmap;

    public CameraFrameProcessor(BitmapProcessor bitmapProcessor,
                                int targetWidth,
                                AsciiCallbackListener listener) {
        this.listener = listener;
        this.bitmapProcessor = bitmapProcessor;
        this.targetWidth = targetWidth;
    }

    @Override
    public void setFrameSize(Size frameSize){
        this.frameSize = frameSize;
        int numPixels = frameSize.getWidth() * frameSize.getHeight();
        int arraySize = (int)(numPixels * 1.5 / 4);
        byteArrayOutputStream = new ByteArrayOutputStream(arraySize);
        bitmap = Bitmap.createBitmap(frameSize.getWidth(), frameSize.getHeight(), Bitmap.Config.ARGB_8888);
    }

    public void setNormalizationLevel(int normalizationLevel){
        this.normalizationLevel = normalizationLevel;
    }

    public void setInvertDarkness(boolean invertDarkness){
        this.invertDarkness = invertDarkness;
    }

    public void setColor(int color){
        this.color = color;
    }

    @Override
    public void processFrame(byte[] frameBytes, int width, int height, int rotation) {
        if(isActive.get()){
            return;
        }
        isActive.set(true);

        YuvImage              yuvImage = new YuvImage(frameBytes, ImageFormat.NV21, width, height, null);
        yuvImage.compressToJpeg(new Rect(0, 0, width, height), 100, byteArrayOutputStream);
        byte[] jpegByteArray = byteArrayOutputStream.toByteArray();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inBitmap = bitmap;
        bitmap = BitmapFactory.decodeByteArray(jpegByteArray, 0, jpegByteArray.length);
        byteArrayOutputStream.reset();

        bitmapProcessor.processImage(bitmap,
                                    rotation * 90,
                                     targetWidth,
                                     color,
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
