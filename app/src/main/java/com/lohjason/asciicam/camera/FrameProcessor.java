package com.lohjason.asciicam.camera;

/**
 * FrameProcessor
 * Created by Jason on 14/7/2018.
 */
public interface FrameProcessor {

    void processFrame(byte[]  frameBytes, int width, int height, int rotation);
}
