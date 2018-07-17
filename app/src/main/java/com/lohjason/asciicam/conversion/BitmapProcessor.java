package com.lohjason.asciicam.conversion;

import android.graphics.Bitmap;

/**
 * BitmapProcessor
 * Created by jason on 17/7/18.
 */
public interface BitmapProcessor {

    void stopProcessing();

    void processImage(Bitmap bitmap,
                      int rotation,
                      int targetWidth,
                      int color,
                      int normalizationLevel,
                      boolean invert);

    void setFlipHorizontal(boolean flipHorizontal);

    void setListener(BitmapProcessedListener listener);

    interface BitmapProcessedListener {
        void processingFinished(Bitmap bitmap);
    }
}
