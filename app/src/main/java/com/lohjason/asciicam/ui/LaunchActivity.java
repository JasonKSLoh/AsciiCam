package com.lohjason.asciicam.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.graphics.ColorUtils;
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
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.lohjason.asciicam.BitmapHolder;
import com.lohjason.asciicam.MainApp;
import com.lohjason.asciicam.R;
import com.lohjason.asciicam.util.CameraConsts;
import com.lohjason.asciicam.util.PermissionUtils;
import com.lohjason.asciicam.util.SharedPrefsUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
    View         viewColorPreview;
    SeekBar      seekbarColor;
    Button       btnStartCamera;
    LinearLayout containerMain;

    private boolean isDisplayingFragment = false;
    int color = 0xFF000000;

    BitmapHolder holder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        setupViews();
        holder = ((MainApp) getApplication()).getBitmapHolder();
    }

    private void setupViews() {
        bindViews();

        btnStartCamera.setOnClickListener(v -> {
            if (PermissionUtils.hasCameraPermission(this)) {
                onBtnStartCameraPressed();
            } else {
                PermissionUtils.requestCameraPermission(this);
            }
        });

        seekbarColor.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress >= 5){
                    float hue = progress * 3.6f;
                    float[] hsl = new float[]{hue, 1f, 0.5f};
                    color = ColorUtils.HSLToColor(hsl);
                } else {
                    color = 0xFF000000;
                }
                seekbarColor.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                viewColorPreview.setBackgroundColor(color);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekbarColor.getProgressDrawable().setColorFilter(0xFF000000, PorterDuff.Mode.SRC_ATOP);
        viewColorPreview.setBackgroundColor(0xFF000000);

        setupSpinners();

        restoreSavedPreferences();
    }

    private void bindViews() {
        spinnerFps = findViewById(R.id.spinner_fps);
        spinnerImageWidth = findViewById(R.id.spinner_image_width);
        switchNormalization = findViewById(R.id.switch_use_thresholding);
        switchInvert = findViewById(R.id.switch_invert);
        switchFrontCamera = findViewById(R.id.switch_front_camera);
        btnStartCamera = findViewById(R.id.btn_start_camera);
        containerMain = findViewById(R.id.container_main);
        seekbarColor = findViewById(R.id.seekbar_color);
        viewColorPreview = findViewById(R.id.view_color_preview);
    }

    private void setupSpinners() {
        List<String> fpsValues = new ArrayList<>();
        for (float fps : CameraConsts.FPS_VALUES) {
            String spinnerText = String.format(Locale.US, "%.0f", fps);
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
    }

    private void restoreSavedPreferences() {
        switchFrontCamera.setChecked(SharedPrefsUtils.getUseFrontCamera(this));
        switchNormalization.setChecked(SharedPrefsUtils.getUseThresholding(this));
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

        Intent intent = new Intent(getBaseContext(), CameraActivity.class);
        intent.putExtra(CameraConsts.KEY_FPS, fps);
        intent.putExtra(CameraConsts.KEY_IMG_WIDTH, imageWidth);
        intent.putExtra(CameraConsts.KEY_USE_FRONT_CAMERA, useFrontCamera);
        intent.putExtra(CameraConsts.KEY_THRESHOLDING, normalize);
        intent.putExtra(CameraConsts.KEY_INVERT, invert);
        intent.putExtra(CameraConsts.KEY_COLOR, color);

        startActivityForResult(intent, CameraConsts.REQUEST_CODE_PREVIEW);
    }

    private void savePreferences() {
        SharedPrefsUtils.setFps(this, CameraConsts.FPS_VALUES[spinnerFps.getSelectedItemPosition()]);
        SharedPrefsUtils.setImageWidth(this, CameraConsts.IMAGE_WIDTHS[spinnerImageWidth.getSelectedItemPosition()]);
        SharedPrefsUtils.setUseThresholding(this, switchNormalization.isChecked());
        SharedPrefsUtils.setInvert(this, switchInvert.isChecked());
        SharedPrefsUtils.setUseFrontCamera(this, switchFrontCamera.isChecked());
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
                            showCameraRationaleDialog(false);
                        } else {
                            showCameraRationaleDialog(true);
                        }
                    }
                }
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void showCameraRationaleDialog(boolean canRequestPermission) {
        String requestMessage = getString(R.string.camera_permission_message);
        if (!canRequestPermission) {
            requestMessage = requestMessage + getString(R.string.open_permission_settings_message);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.camera_permission_title)
                .setMessage(requestMessage)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    dialog.dismiss();
                    if (canRequestPermission) {
                        PermissionUtils.requestCameraPermission(this);
                    } else {
                        PermissionUtils.openSettingsPage(this);
                    }
                })
                .setNegativeButton(R.string.not_now, (dialog, which) -> dialog.dismiss())
                .setCancelable(true);
        builder.show();
    }


    private void showAboutDialog() {
        String  rawMessage = getString(R.string.about_app_message);
        Spanned message    = Html.fromHtml(rawMessage);
        AlertDialog aboutDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.about_app_title)
                .setMessage(message)
                .setNegativeButton(R.string.dismiss, (dialog, which) -> dialog.dismiss())
                .show();
        TextView textView = aboutDialog.findViewById(android.R.id.message);
        if (textView != null) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            Linkify.addLinks(textView, Linkify.ALL);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CameraConsts.REQUEST_CODE_PREVIEW: {
                if (resultCode == RESULT_OK) {
                    showDisplayFragment();
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

    private void showDisplayFragment() {
        if (isDisplayingFragment) {
            return;
        }
        containerMain.setVisibility(View.VISIBLE);
        btnStartCamera.setVisibility(View.GONE);
        FragmentManager fragmentManager = getSupportFragmentManager();
        DisplayFragment displayFragment = DisplayFragment.getNewInstance(holder);
        fragmentManager.beginTransaction()
                .replace(R.id.container_main,
                         displayFragment,
                         DisplayFragment.FRAGMENT_TAG)
                .commitAllowingStateLoss();
        isDisplayingFragment = true;
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

}
