package com.lohjason.asciicam;

import android.Manifest;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.TextView;

import com.lohjason.asciicam.Util.WindowUtils;
import com.lohjason.asciicam.camera.CameraFrameProcessor;
import com.lohjason.asciicam.camera.CameraSource;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements CameraFrameProcessor.AsciiCallbackListener{

    Button       button;
    TextView     tvAscii;
    CameraSource cameraSource;

    int imageToShow = 0;
    private final int TARGET_HEIGHT = 32;
    private final int TARGET_WIDTH  = 32;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.btn_start);
        tvAscii = findViewById(R.id.tv_ascii);
        tvAscii.setTypeface(Typeface.MONOSPACE);

        AsciiConverter asciiConverter = new AsciiConverter(TARGET_HEIGHT, TARGET_WIDTH);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.cat);

        button.setOnClickListener(v -> {
            startCamera();
        });

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 123);



    }

    private void initCamera() {
        cameraSource = new CameraSource.Builder(MainActivity.this)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedFps(24f)
                .setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)
                .setRequestedPreviewSize(320, 240)
                .build(new CameraFrameProcessor(MainActivity.this));
    }


    @Override
    protected void onPause() {
        if (cameraSource != null) {
            cameraSource.stop();
            cameraSource.release();
            cameraSource = null;
        }
        super.onPause();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onResume() {
        super.onResume();
//        initCamera();
//        try {
//            cameraSource.start();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        Logg.d("+_", "Started Cam");
    }

    @SuppressLint("MissingPermission")
    private void startCamera() {
        if (cameraSource == null) {

        }
        initCamera();
        try {
            cameraSource.start();
            float rotation = cameraSource.getRotation();
            Log.d("+_", "Got rotation: " + rotation);

            tvAscii.setRotation(rotation);
            float textSize = WindowUtils.getTextSizeForAsciiArt(this, 64);
            tvAscii.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize);


        } catch (Exception e) {
            e.printStackTrace();
        }

        if (this.cameraSource != null) {
            try {
                cameraSource.start(null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void newAsciiImage(String ascii) {
        runOnUiThread( () -> {
            tvAscii.setText(ascii);
        });
    }
}
