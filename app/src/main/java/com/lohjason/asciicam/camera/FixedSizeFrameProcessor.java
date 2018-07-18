package com.lohjason.asciicam.camera;

import com.google.android.gms.common.images.Size; /**
 * FixedSizeFrameProcessor
 * Created by Jason on 14/7/2018.
 */
public interface FixedSizeFrameProcessor {

    void processFrame(byte[]  frameBytes, int width, int height, int rotation);

    void setFrameSize(Size frameSize);
}
