package com.lohjason.asciicam.conversion;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Typeface;
import android.util.Log;

import com.lohjason.asciicam.util.CameraConsts;
import com.lohjason.asciicam.util.Logg;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ThresholdingBitmapProcessor
 * Created by jason on 18/7/18.
 */
public class ThresholdingBitmapProcessor implements BitmapProcessor {
    private static final String LOG_TAG = "+_TrsBmpPrc";


    private int     numFilteringThreads = 4;
    private BitmapProcessedListener listener;
    private final int outputWidth;

    private ExecutorService mainExecutor   = Executors.newFixedThreadPool(1);
    private ExecutorService filterExecutor = Executors.newFixedThreadPool(numFilteringThreads);
    private AtomicBoolean   isProcessing   = new AtomicBoolean(false);
    private AtomicBoolean   isDestroyed    = new AtomicBoolean(false);

    private Bitmap outBitmap;
    private Canvas canvas;

    private boolean flipHorizontal      = false;


    public ThresholdingBitmapProcessor(int outputWidth) {
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

        final int threshold = Math.round(normalizationLevel * 255f / CameraConsts.MAX_NORMALIZATION);
        Log.d("+_", "Threshold was: " + threshold);

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

        List<Callable<Void>> callables      = new ArrayList<>();
        for (int n = 0; n < numFilteringThreads; n++) {
            final int threadIndex = n;
            callables.add(() -> {
                for (int i = threadIndex; i < originalHeight; i += numFilteringThreads) {
                    for (int j = 0; j < originalWidth; j++) {
                        int index     = i * originalWidth + j;
                        int argb      = scaledBitmapBytes[index];
                        int greyscale = getGreyscaleFromArgb(argb);
                        greyscale = greyscale > threshold ? 255 : 0;
                        char charToDraw = AsciiConverter.getCharForBrightness(greyscale,
                                                                              invert);

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

        return outBitmap;
    }

    private int getGreyscaleFromArgb(int argb) {
        int r = argb >> 16 & 0xff;
        int g = argb >> 8 & 0xff;
        int b = argb & 0xff;

        return ((r << 2) + (g << 1) + b) / 7;
    }
}
