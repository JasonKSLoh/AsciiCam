package com.lohjason.asciicam.camera;

/**
 * FrameProcessor
 * Created by Jason on 14/7/2018.
 */
public interface FrameProcessor {
    void start();

    void stop();

    void processFrame(byte[]  frameBytes, int width, int height, int rotation);
}
