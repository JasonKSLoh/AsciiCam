package com.lohjason.asciicam.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lohjason.asciicam.BitmapHolder;
import com.lohjason.asciicam.MainApp;
import com.lohjason.asciicam.R;
import com.lohjason.asciicam.camera.CameraFrameProcessor;
import com.lohjason.asciicam.camera.CameraSource;
import com.lohjason.asciicam.conversion.BitmapProcessor;
import com.lohjason.asciicam.conversion.ContrastingBitmapProcessor;
import com.lohjason.asciicam.conversion.ThresholdingBitmapProcessor;
import com.lohjason.asciicam.util.CameraConsts;
import com.lohjason.asciicam.util.Logg;
import com.lohjason.asciicam.util.ScreenUtils;

import java.io.IOException;

public class CameraActivity extends AppCompatActivity implements CameraFrameProcessor.AsciiCallbackListener {

    private static final String LOG_TAG = "+_CamAtv";

    ImageButton          button;
    ImageView            ivAscii;
    TextView             tvThresholdOrContrast;
    SeekBar              seekbarNormalization;
    CameraSource         cameraSource;
    CameraFrameProcessor cameraFrameProcessor;
    BitmapProcessor      bitmapProcessor;
    ConstraintLayout     layoutMain;
    int normalizationLevel = 255 / 2;
    BitmapHolder holder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_camera);
        setupViews();
        holder = ((MainApp) getApplication()).getBitmapHolder();
    }

    private void setupViews() {
        button = findViewById(R.id.btn_capture_ascii);
        ivAscii = findViewById(R.id.iv_ascii);
        layoutMain = findViewById(R.id.layout_main);
        tvThresholdOrContrast = findViewById(R.id.tv_threshold_or_contrast);
        seekbarNormalization = findViewById(R.id.seekbar_normalization);

        button.setOnClickListener(v -> captureAsciiImage());

        seekbarNormalization.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                normalizationLevel = (int) (progress / CameraConsts.NORMALIZATION_SCALE);
                cameraFrameProcessor.setNormalizationLevel(normalizationLevel);
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

        if (bitmapProcessor != null) {
            bitmapProcessor.stopProcessing();
        }


        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }


    @SuppressWarnings("deprecation")
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
        boolean invert         = intent.getBooleanExtra(CameraConsts.KEY_INVERT, false);

        boolean useThresholding = intent.getBooleanExtra(CameraConsts.KEY_THRESHOLDING, false);

        int[] screenSize = ScreenUtils.getScreenDimensPx(this);

        if (!useThresholding) {
            bitmapProcessor = new ContrastingBitmapProcessor(screenSize[0]);
            tvThresholdOrContrast.setText(R.string.contrast);
        } else {
            bitmapProcessor = new ThresholdingBitmapProcessor(screenSize[0]);
            tvThresholdOrContrast.setText(R.string.threshold);
        }
        cameraFrameProcessor = new CameraFrameProcessor(bitmapProcessor, imgWidth, this);
        cameraFrameProcessor.setInvertDarkness(invert);
        bitmapProcessor.setListener(cameraFrameProcessor);
        bitmapProcessor.setFlipHorizontal(useFrontCamera);
        seekbarNormalization.setProgress(50);

        int facing                 = useFrontCamera ? CameraSource.CAMERA_FACING_FRONT : CameraSource.CAMERA_FACING_BACK;
        int requestedPreviewWidth  = imgWidth > 240 ? 640 : 320;
        int requestedPreviewHeight = imgWidth > 240 ? 480 : 240;

        cameraSource = new CameraSource.Builder(CameraActivity.this)
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
        if (bitmapProcessor != null) {
            bitmapProcessor.stopProcessing();
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
            Logg.e(LOG_TAG, e.getMessage(), e);
        }

        if (this.cameraSource != null) {
            try {
                cameraSource.start(null);
            } catch (IOException e) {
                Logg.e(LOG_TAG, e.getMessage(), e);
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
