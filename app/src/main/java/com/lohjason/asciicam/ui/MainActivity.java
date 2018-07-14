package com.lohjason.asciicam.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.lohjason.asciicam.R;
import com.lohjason.asciicam.Util.CameraConsts;
import com.lohjason.asciicam.Util.Logg;
import com.lohjason.asciicam.Util.WindowUtils;
import com.lohjason.asciicam.camera.CameraFrameProcessor;
import com.lohjason.asciicam.camera.CameraSource;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements CameraFrameProcessor.AsciiCallbackListener {

    FloatingActionButton button;
    TextView             tvAscii;
    CameraSource         cameraSource;
    ConstraintLayout     layoutMain;

    private int selectedWidth = 128;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.btn_capture_ascii);
        tvAscii = findViewById(R.id.tv_ascii);
        tvAscii.setTypeface(Typeface.MONOSPACE);
        layoutMain = findViewById(R.id.layout_main);

        button.setOnClickListener(v -> {
            captureAsciiImage();
        });
    }

    private void captureAsciiImage() {
        if (cameraSource != null) {
            cameraSource.stop();
            cameraSource.release();
            cameraSource = null;
        }

        Intent intent = new Intent();
        intent.putExtra(CameraConsts.KEY_ASCII_IMAGE, tvAscii.getText().toString());
        intent.putExtra(CameraConsts.KEY_ASCII_TEXT_SIZE, tvAscii.getTextSize());

        Logg.d("+_", "Saved Ascii Image:\n" + tvAscii.getText().toString());
        Logg.d("+_", "Text size was: " + tvAscii.getTextSize());

        setResult(RESULT_OK, intent);
        finish();
    }


    private void initCamera() {
        Intent intent = getIntent();
        if (intent == null) {
            Toast.makeText(getApplicationContext(), "Error: Could not get camera options", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        float   fps              = intent.getFloatExtra(CameraConsts.KEY_FPS, 24);
        int     imgWidth         = intent.getIntExtra(CameraConsts.KEY_IMG_WIDTH, 128);
        boolean useFrontCamera   = intent.getBooleanExtra(CameraConsts.KEY_USE_FRONT_CAMERA, false);
        boolean useNormalization = intent.getBooleanExtra(CameraConsts.KEY_NORMALIZATION, true);
        boolean invert           = intent.getBooleanExtra(CameraConsts.KEY_INVERT, false);
        selectedWidth = imgWidth;


        @SuppressWarnings("SuspiciousNameCombination")
        CameraFrameProcessor processor = new CameraFrameProcessor(MainActivity.this, imgWidth, imgWidth);
        processor.setInvertDarkness(invert);
        processor.setUseNormalization(useNormalization);
        int facing = useFrontCamera ? CameraSource.CAMERA_FACING_FRONT : CameraSource.CAMERA_FACING_BACK;

        int requestedPreviewWidth  = imgWidth > 256 ? 640 : 320;
        int requestedPreviewHeight = imgWidth > 256 ? 480 : 240;

        cameraSource = new CameraSource.Builder(MainActivity.this)
                .setFacing(facing)
                .setRequestedFps(fps)
                .setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)
                .setRequestedPreviewSize(requestedPreviewWidth, requestedPreviewHeight)
                .build(processor);
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
        startCamera();
    }

    @SuppressLint("MissingPermission")
    private void startCamera() {
        initCamera();
        try {
            cameraSource.start();
            float rotation = cameraSource.getRotation();
            Log.d("+_", "Got rotation: " + rotation);

//            float textSize = WindowUtils.getTextSizeForAsciiArt(this, 128);
//            tvAscii.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize);

//            WindowUtils.setIdealTextSize2(this, tvAscii, selectedWidth);

            WindowUtils.setIdealTextSize(this, tvAscii, selectedWidth);
//            float textSize2 = WindowUtils.getTextSizeForAsciiArt2(this, tvAscii);
//            tvAscii.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize2);

//            WindowUtils.setIdealTextSize(this, tvAscii, 128);

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
        runOnUiThread(() -> {
            tvAscii.setText(ascii);
        });
    }
}
