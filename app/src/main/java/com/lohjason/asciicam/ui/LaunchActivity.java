package com.lohjason.asciicam.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;

import com.lohjason.asciicam.R;
import com.lohjason.asciicam.Util.CameraConsts;
import com.lohjason.asciicam.Util.PermissionUtils;
import com.lohjason.asciicam.Util.SharedPrefsUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * LaunchActivity
 * Created by Jason on 15/7/2018.
 */
public class LaunchActivity extends AppCompatActivity {

    Spinner spinnerFps;
    Spinner spinnerImageWidth;
    Switch  switchNormalization;
    Switch  switchInvert;
    Switch  switchFrontCamera;
    Button  btnStartCamera;
    LinearLayout containerMain;

    private boolean isDisplayingFragment = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        setupViews();
    }

    @SuppressLint("DefaultLocale")
    private void setupViews() {
        spinnerFps = findViewById(R.id.spinner_fps);
        spinnerImageWidth = findViewById(R.id.spinner_image_width);
        switchNormalization = findViewById(R.id.switch_normalization);
        switchInvert = findViewById(R.id.switch_invert);
        switchFrontCamera = findViewById(R.id.switch_front_camera);
        btnStartCamera = findViewById(R.id.btn_start_camera);
        containerMain = findViewById(R.id.container_main);

        List<String> fpsValues = new ArrayList<>();
        for (float fps : CameraConsts.FPS_VALUES) {
            fpsValues.add(String.format("%.0f", fps));
        }
        ArrayAdapter<String> spinnerAdapterFps = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fpsValues);
        spinnerAdapterFps.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFps.setAdapter(spinnerAdapterFps);

        List<String> imageWidthValues = new ArrayList<>();
        for (int imgWidth : CameraConsts.IMAGE_WIDTHS) {
            imageWidthValues.add(Integer.toString(imgWidth));
        }

        ArrayAdapter<String> spinnerAdapterImageWidth = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, imageWidthValues);
        spinnerAdapterImageWidth.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerImageWidth.setAdapter(spinnerAdapterImageWidth);

        switchNormalization.setChecked(true);
        switchInvert.setChecked(false);

        btnStartCamera.setOnClickListener(v -> {
            if (PermissionUtils.hasCameraPermission(this)) {
                onBtnStartCameraPressed();
            } else {
                PermissionUtils.requestCameraPermission(this);
            }
        });

        restoreSavedPreferences();
    }

    private void restoreSavedPreferences() {
        switchFrontCamera.setChecked(SharedPrefsUtils.getUseFrontCamera(this));
        switchNormalization.setChecked(SharedPrefsUtils.getNormalization(this));
        switchInvert.setChecked(SharedPrefsUtils.getInvert(this));

        float preferredFps      = SharedPrefsUtils.getFps(this);
        int   preferredImgWidth = SharedPrefsUtils.getImageWidth(this);
        for (int i = 0; i < CameraConsts.FPS_VALUES.length; i++) {
            if (preferredFps == CameraConsts.FPS_VALUES[i]) {
                spinnerFps.setSelection(i);
                break;
            }
        }
        for (int i = 0; i < CameraConsts.IMAGE_WIDTHS.length; i++) {
            if (preferredImgWidth == CameraConsts.IMAGE_WIDTHS[i]) {
                spinnerImageWidth.setSelection(i);
                break;
            }
        }
    }

    private void onBtnStartCameraPressed() {
        float   fps            = CameraConsts.FPS_VALUES[spinnerFps.getSelectedItemPosition()];
        int     imageWidth     = CameraConsts.IMAGE_WIDTHS[spinnerImageWidth.getSelectedItemPosition()];
        boolean normalize      = switchNormalization.isChecked();
        boolean invert         = switchInvert.isChecked();
        boolean useFrontCamera = switchFrontCamera.isChecked();

        savePreferences();

        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        intent.putExtra(CameraConsts.KEY_FPS, fps);
        intent.putExtra(CameraConsts.KEY_IMG_WIDTH, imageWidth);
        intent.putExtra(CameraConsts.KEY_USE_FRONT_CAMERA, useFrontCamera);
        intent.putExtra(CameraConsts.KEY_NORMALIZATION, normalize);
        intent.putExtra(CameraConsts.KEY_INVERT, invert);

        startActivityForResult(intent, CameraConsts.REQUEST_CODE_PREVIEW);
    }

    private void savePreferences() {
        SharedPrefsUtils.setFps(this, CameraConsts.FPS_VALUES[spinnerFps.getSelectedItemPosition()]);
        SharedPrefsUtils.setImageWidth(this, CameraConsts.IMAGE_WIDTHS[spinnerImageWidth.getSelectedItemPosition()]);
        SharedPrefsUtils.setNormalization(this, switchNormalization.isChecked());
        SharedPrefsUtils.setInvert(this, switchInvert.isChecked());
        SharedPrefsUtils.setUseFrontCamera(this, switchInvert.isChecked());
    }


    private void showDisplayFragment(String asciiImage, float textSize) {
        if(isDisplayingFragment){
            return;
        }
        containerMain.setVisibility(View.VISIBLE);
        btnStartCamera.setVisibility(View.GONE);
        FragmentManager fragmentManager = getSupportFragmentManager();
        DisplayFragment displayFragment = DisplayFragment.getNewInstance(asciiImage, textSize);
        fragmentManager.beginTransaction()
                .replace(R.id.container_main,
                         displayFragment,
                         DisplayFragment.FRAGMENT_TAG)
                .commitAllowingStateLoss();
        isDisplayingFragment = true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PermissionUtils.REQUEST_CODE_CAMERA: {
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        onBtnStartCameraPressed();
                    } else {
                        if (!PermissionUtils.canRequestCameraPermission(this)) {
                            showCameraRationaleDialog(true);
                        } else {
                            showCameraRationaleDialog(false);
                        }
                    }
                }
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void showCameraRationaleDialog(boolean canRequestPermission) {
        String requestMessage = "The camera permission is needed for this app to work. If you do not grant this permission, it cannot convert your camera feed to ASCII images.\nDo you want to grant the Camera Permission?";
        if (!canRequestPermission) {
            requestMessage = requestMessage + "\nYou will need to grant the permission from the App's settings page";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Camera")
                .setMessage(requestMessage)
                .setPositiveButton("Yes", (dialog, which) -> {
                    dialog.dismiss();
                    if (canRequestPermission) {
                        PermissionUtils.requestCameraPermission(this);
                    } else {
                        PermissionUtils.openSettingsPage(this);
                    }
                })
                .setNegativeButton("Not Now", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setCancelable(true);
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CameraConsts.REQUEST_CODE_PREVIEW: {
                if (resultCode == RESULT_OK) {
                    String asciiImage = data.getStringExtra(CameraConsts.KEY_ASCII_IMAGE);
                    float  textSize   = data.getFloatExtra(CameraConsts.KEY_ASCII_TEXT_SIZE, -1);
                    if (asciiImage != null
                        && !asciiImage.isEmpty()
                        && textSize > 0) {
                        showDisplayFragment(asciiImage, textSize);
                    }
                }
                break;
            }
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }


    @Override
    protected void onDestroy() {
        savePreferences();
        super.onDestroy();
    }

    private void dismissDisplayFragment(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment        displayFragment = fragmentManager.findFragmentByTag(DisplayFragment.FRAGMENT_TAG);
        if(displayFragment != null){
            fragmentManager.beginTransaction()
                    .remove(displayFragment)
                    .commitAllowingStateLoss();
        }
        containerMain.setVisibility(View.GONE);
        btnStartCamera.setVisibility(View.VISIBLE);
        isDisplayingFragment  = false;
    }

    @Override
    public void onBackPressed() {
        if (isDisplayingFragment) {
            dismissDisplayFragment();
        } else {
            super.onBackPressed();
        }
    }
}
