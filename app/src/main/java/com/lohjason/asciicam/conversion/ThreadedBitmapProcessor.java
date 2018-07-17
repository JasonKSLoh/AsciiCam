package com.lohjason.asciicam.conversion;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Typeface;

import com.lohjason.asciicam.util.Logg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ThreadedBitmapProcessor
 * Created by jason on 16/7/18.
 */
public class ThreadedBitmapProcessor implements BitmapProcessor {
    private static final String LOG_TAG = "+_ThdBmpPrc";


    private int     numFilteringThreads = 4;
    private int     lastMinBrightness   = 255;
    private int     lastMaxBrightness   = 0;
    private BitmapProcessedListener listener;
    private final int outputWidth;

    private ExecutorService mainExecutor   = Executors.newFixedThreadPool(1);
    private ExecutorService filterExecutor = Executors.newFixedThreadPool(numFilteringThreads);
    private AtomicBoolean   isProcessing   = new AtomicBoolean(false);
    private AtomicBoolean   isDestroyed    = new AtomicBoolean(false);

    private Bitmap outBitmap;
    private Canvas canvas;

    private boolean flipHorizontal      = false;


    public ThreadedBitmapProcessor(int outputWidth) {
        this.outputWidth = outputWidth;
    }

    @Override
    public void setListener(BitmapProcessedListener listener) {
        this.listener = listener;
    }

    @Override
    public void setFlipHorizontal(boolean flipHorizontal) {
        this.flipHorizontal = flipHorizontal;
    }

    @Override
    public void stopProcessing() {
        isDestroyed.set(true);
    }

    @Override
    public void processImage(Bitmap bitmap, int rotation, int targetWidth, int color, int normalizationLevel, boolean invert) {
        if (isProcessing.get() || isDestroyed.get()) {
            return;
        }
        isProcessing.set(true);
        mainExecutor.execute(() -> {
            Bitmap rotatedBitmap   = rotateAndScaleBitmap(bitmap, rotation, targetWidth);
            Bitmap processedBitmap = generateAsciiBitmap(rotatedBitmap, color, normalizationLevel, invert);


            isProcessing.set(false);
            if (listener != null) {
                listener.processingFinished(processedBitmap);
            }
            if (isDestroyed.get()) {
                mainExecutor.shutdown();
                filterExecutor.shutdown();
            }
        });
    }


    private Bitmap rotateAndScaleBitmap(Bitmap bitmap, int rotation, int targetWidth) {
        Matrix matrix = new Matrix();
        float  scale  = (float) targetWidth / bitmap.getWidth();
        matrix.preScale(scale, scale);
        matrix.postRotate(rotation);
        if (flipHorizontal) {
            matrix.postScale(-1, 1);
        }
        return Bitmap.createBitmap(bitmap,
                                   0, 0,
                                   bitmap.getWidth(), bitmap.getHeight(),
                                   matrix, true);
    }


    private Bitmap generateAsciiBitmap(Bitmap scaledBitmap, int color, int normalizationLevel, boolean invert) {
        int originalHeight = scaledBitmap.getHeight();
        int originalWidth  = scaledBitmap.getWidth();
        int numBytes       = originalWidth * originalHeight;

        int[] scaledBitmapBytes = new int[numBytes];
        scaledBitmap.getPixels(scaledBitmapBytes,
                               0, originalWidth,
                               0, 0,
                               originalWidth, originalHeight);


        float heightRatio = (float) originalHeight / originalWidth;
        int   outHeight   = (int) (heightRatio * outputWidth);

        if (outBitmap == null) {
            outBitmap = Bitmap.createBitmap(outputWidth, outHeight, Bitmap.Config.ARGB_8888);
            canvas = new Canvas(outBitmap);
            canvas.drawColor(0xFFFFFFFF);
        }

        float heightSpacing = outHeight / originalHeight;
        float widthSpacing  = outputWidth / originalWidth;
        int   idealTextSize = (int) (heightSpacing * 1.5);

        Paint paint = new Paint();
        paint.setColor(color);
        paint.setTextSize(idealTextSize);
        paint.setTypeface(Typeface.MONOSPACE);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

        canvas.drawColor(0xFFFFFFFF);

        int[]                brightnessList = new int[scaledBitmapBytes.length];
        List<Callable<Void>> callables      = new ArrayList<>();
        for (int n = 0; n < numFilteringThreads; n++) {
            final int threadIndex = n;
            callables.add(() -> {
                for (int i = threadIndex; i < originalHeight; i += numFilteringThreads) {
                    for (int j = 0; j < originalWidth; j++) {
                        int index     = i * originalWidth + j;
                        int argb      = scaledBitmapBytes[index];
                        int greyscale = getGreyscaleFromArgb(argb);
                        brightnessList[index] = greyscale;
                        char charToDraw = AsciiConverter.getCharForBrightness(greyscale,
                                                                              invert,
                                                                              lastMinBrightness,
                                                                              lastMaxBrightness);

                        canvas.drawText(Character.toString(charToDraw),
                                        j * widthSpacing,
                                        i * heightSpacing, paint);
                    }
                }
                return null;
            });
        }
        try {
            filterExecutor.invokeAll(callables);
        } catch (InterruptedException e) {
            Logg.e(LOG_TAG, e.getMessage(), e);
        }

        Arrays.sort(brightnessList);
        if (normalizationLevel == 0) {
            lastMaxBrightness = 255;
            lastMinBrightness = 0;
        } else {
            int minIndex = normalizationLevel * brightnessList.length / 100;
            int maxIndex = brightnessList.length - (normalizationLevel * brightnessList.length / 100);
            lastMinBrightness = brightnessList[minIndex];
            lastMaxBrightness = brightnessList[maxIndex];
        }
        return outBitmap;
    }

    private int getGreyscaleFromArgb(int argb) {
        int r = argb >> 16 & 0xff;
        int g = argb >> 8 & 0xff;
        int b = argb & 0xff;

        return ((r << 2) + (g << 1) + b) / 7;
    }
}
