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
import android.text.Html;
import android.text.Spanned;
import android.text.util.Linkify;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

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

    Spinner      spinnerFps;
    Spinner      spinnerImageWidth;
    Switch       switchNormalization;
    Switch       switchInvert;
    Switch       switchFrontCamera;
    Button       btnStartCamera;
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
            String spinnerText = String.format("%.0f", fps);
            fpsValues.add(spinnerText);
        }
        ArrayAdapter<String> spinnerAdapterFps = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fpsValues);
        spinnerAdapterFps.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFps.setAdapter(spinnerAdapterFps);

        List<String> imageWidthValues = new ArrayList<>();
        for (int imgWidth : CameraConsts.IMAGE_WIDTHS) {
            String spinnerText = Integer.toString(imgWidth);
            imageWidthValues.add(spinnerText);
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
        if (normalize) {
            intent.putExtra(CameraConsts.KEY_NORMALIZATION, CameraConsts.MAX_NORMALIZATION);
        } else {
            intent.putExtra(CameraConsts.KEY_NORMALIZATION, CameraConsts.DEFAULT_NORMALIZATION);
        }
        intent.putExtra(CameraConsts.KEY_INVERT, invert);

        startActivityForResult(intent, CameraConsts.REQUEST_CODE_PREVIEW);
    }

    private void savePreferences() {
        SharedPrefsUtils.setFps(this, CameraConsts.FPS_VALUES[spinnerFps.getSelectedItemPosition()]);
        SharedPrefsUtils.setImageWidth(this, CameraConsts.IMAGE_WIDTHS[spinnerImageWidth.getSelectedItemPosition()]);
        SharedPrefsUtils.setNormalization(this, switchNormalization.isChecked());
        SharedPrefsUtils.setInvert(this, switchInvert.isChecked());
        SharedPrefsUtils.setUseFrontCamera(this, switchFrontCamera.isChecked());
    }


    private void showDisplayFragment(String asciiImage, float textSize) {
        if (isDisplayingFragment) {
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

    private void dismissDisplayFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment        displayFragment = fragmentManager.findFragmentByTag(DisplayFragment.FRAGMENT_TAG);
        if (displayFragment != null) {
            fragmentManager.beginTransaction()
                    .remove(displayFragment)
                    .commitAllowingStateLoss();
        }
        containerMain.setVisibility(View.GONE);
        btnStartCamera.setVisibility(View.VISIBLE);
        isDisplayingFragment = false;
    }

    @Override
    public void onBackPressed() {
        if (isDisplayingFragment) {
            dismissDisplayFragment();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_about) {
            showAboutDialog();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void showAboutDialog() {
        String rawMessage = "Take your camera feed and turn it into an ascii image<br>" +
                            "<h4>Usage</h4>Select what settings you want to use then start by pressing the <b>\"Start\"</b> button.<br>" +
                            "Change the contrast by moving the slider bar. We recommend setting it to 50% or higher" +
                            "<h4>Settings</h4>" +
                            "<b>FPS:</b> Choose the capture rate. If your phone cannot process fast enough the frame will be skipped.<br>" +
                            "<b>Ascii Image Width:</b> How many characters make up the image width. You may see lag when using a higher number<br>" +
                            "<b>High Contrast:</b> Sets the contrast to max upon starting the camera.<br>" +
                            "<b>Invert Brightness:</b> Dark turns to light and light turns to dark.<br>" +
                            "<h4>Website</h4>Visit our site at http://asciicam.lohjason.com" +
                            "<h4>Privacy</h4>We do not collect, log, or store any of your information<br>We do not serve or display any ads<Br>" +
                            "For a fully detailed privacy policy, check our privacy page at http://privacy.lohjason.com";
        Spanned message = Html.fromHtml(rawMessage);
        AlertDialog aboutDialog = new AlertDialog.Builder(this)
                .setTitle("About this app")
                .setMessage(message)
                .setNegativeButton("Ok", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
        TextView textView = aboutDialog.findViewById(android.R.id.message);
        if (textView != null) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            Linkify.addLinks(textView, Linkify.ALL);
        }
    }
}
