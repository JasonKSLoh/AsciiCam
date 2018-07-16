package com.lohjason.asciicam.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.lohjason.asciicam.MainApp;
import com.lohjason.asciicam.R;
import com.lohjason.asciicam.Util.BitmapHolder;
import com.lohjason.asciicam.Util.CameraConsts;
import com.lohjason.asciicam.Util.ScreenUtils;
import com.lohjason.asciicam.camera.CameraFrameProcessor;
import com.lohjason.asciicam.camera.CameraSource;
import com.lohjason.asciicam.converters.ImageProcessor;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements CameraFrameProcessor.AsciiCallbackListener {

    ImageButton          button;
    ImageView ivAscii;
    SeekBar              seekbarNormalization;
    CameraSource         cameraSource;
    CameraFrameProcessor cameraFrameProcessor;
    ImageProcessor       imageProcessor;
    ConstraintLayout     layoutMain;
    int normalizationLevel = CameraConsts.DEFAULT_NORMALIZATION;
    BitmapHolder holder;

    private int selectedWidth = 120;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_main);
        setupViews();
        holder = ((MainApp)getApplication()).getBitmapHolder();
    }

    private void setupViews() {
        button = findViewById(R.id.btn_capture_ascii);
        ivAscii = findViewById(R.id.iv_ascii);
        ivAscii.setDrawingCacheEnabled(true);
        layoutMain = findViewById(R.id.layout_main);
        seekbarNormalization = findViewById(R.id.seekbar_normalization);

        button.setOnClickListener(v -> {
            captureAsciiImage();
        });

        seekbarNormalization.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                normalizationLevel = (int) (progress / CameraConsts.NORMALIZATION_SCALE);
                cameraFrameProcessor.setNormalizationLevel(normalizationLevel);
                Log.d("+_", "Normalization set to: " + normalizationLevel);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void captureAsciiImage() {
        if (cameraSource != null) {
            cameraSource.stop();
            cameraSource.release();
            cameraSource = null;
        }


        Intent intent = new Intent();
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
        float   fps            = intent.getFloatExtra(CameraConsts.KEY_FPS, 24);
        int     imgWidth       = intent.getIntExtra(CameraConsts.KEY_IMG_WIDTH, 128);
        boolean useFrontCamera = intent.getBooleanExtra(CameraConsts.KEY_USE_FRONT_CAMERA, false);
        normalizationLevel = intent.getIntExtra(CameraConsts.KEY_NORMALIZATION, CameraConsts.DEFAULT_NORMALIZATION);
        boolean invert = intent.getBooleanExtra(CameraConsts.KEY_INVERT, false);
        selectedWidth = imgWidth;


        int[] screenSize = ScreenUtils.getScreenDimensPx(this);
        imageProcessor = new ImageProcessor(screenSize[0]);
        cameraFrameProcessor = new CameraFrameProcessor(imageProcessor, imgWidth, this);
        imageProcessor.setListener(cameraFrameProcessor);
        imageProcessor.setFlipHorizontal(useFrontCamera);
        cameraFrameProcessor.setInvertDarkness(invert);
        seekbarNormalization.setProgress((int) (normalizationLevel * CameraConsts.NORMALIZATION_SCALE));
        int facing = useFrontCamera ? CameraSource.CAMERA_FACING_FRONT : CameraSource.CAMERA_FACING_BACK;

        int requestedPreviewWidth  = imgWidth > 240 ? 640 : 320;
        int requestedPreviewHeight = imgWidth > 240 ? 480 : 240;

        cameraSource = new CameraSource.Builder(MainActivity.this)
                .setFacing(facing)
                .setRequestedFps(fps)
                .setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)
                .setRequestedPreviewSize(requestedPreviewWidth, requestedPreviewHeight)
                .build(cameraFrameProcessor);
    }


    @Override
    protected void onPause() {
        if (cameraSource != null) {
            cameraSource.stop();
            cameraSource.release();
            cameraSource = null;
        }
        if(imageProcessor != null){
            imageProcessor.destroy();
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
    public void newAsciiImage(Bitmap bitmap) {
        runOnUiThread(() -> {
            ivAscii.setImageBitmap(bitmap);
            holder.setBitmap(bitmap);
        });
    }
}
