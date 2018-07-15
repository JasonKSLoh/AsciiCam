package com.lohjason.asciicam.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.lohjason.asciicam.R;
import com.lohjason.asciicam.Util.ContentProviderUtils;

/**
 * DisplayFragment
 * Created by Jason on 15/7/2018.
 */
public class DisplayFragment extends Fragment {
    public static final String FRAGMENT_TAG = "DisplayFragment";

    Button btnSave;
    Button btnCopy;
    TextView tvAsciiImage;

    String asciiImage;
    float textSize;
    Bitmap bitmap;

    public static DisplayFragment getNewInstance(String asciiImage, float textSize){
        DisplayFragment fragment = new DisplayFragment();
        fragment.asciiImage = asciiImage;
        fragment.textSize = textSize;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_display, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);;
        setupViews(view);
    }

    private void setupViews(View view){
        btnSave = view.findViewById(R.id.btn_save);
        btnCopy = view.findViewById(R.id.btn_copy);
        tvAsciiImage = view.findViewById(R.id.tv_ascii_image);

        tvAsciiImage.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        tvAsciiImage.setText(asciiImage);

        btnSave.setOnClickListener(v -> {
            onSaveImageClicked();
        });
        btnCopy.setOnClickListener(v -> {
            onCopyClicked();
        });
    }

    private void onCopyClicked() {
        ClipboardManager clipboardManager = (ClipboardManager)requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("Ascii Image", asciiImage);
        assert clipboardManager != null;
        clipboardManager.setPrimaryClip(clipData);
        Toast.makeText(requireContext(), "Copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    private void onSaveImageClicked() {
        if(bitmap == null){
            Bitmap originalBitmap = getTextViewBitmap();

            Bitmap newBitmap = Bitmap.createBitmap(originalBitmap.getWidth(), originalBitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(newBitmap);
            canvas.drawColor(0xFFFFFFFF);
            Paint alphaPaint = new Paint();
            canvas.drawBitmap(originalBitmap, 0, 0, alphaPaint);
            bitmap = newBitmap;

            ContentProviderUtils.saveBitmap(requireContext(), newBitmap);
        }
        Intent intent = ContentProviderUtils.getShareBitmapIntent(requireContext());
        requireContext().startActivity(intent);
    }

    private Bitmap getTextViewBitmap(){
        tvAsciiImage.setDrawingCacheEnabled(true);
        return Bitmap.createBitmap(tvAsciiImage.getDrawingCache());
    }
}
