package com.lohjason.asciicam.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.lohjason.asciicam.R;
import com.lohjason.asciicam.BitmapHolder;
import com.lohjason.asciicam.util.ContentProviderUtils;

/**
 * DisplayFragment
 * Created by Jason on 15/7/2018.
 */
public class DisplayFragment extends Fragment {
    public static final String FRAGMENT_TAG = "DisplayFragment";

    Button       btnSave;
    ImageView    ivAsciiImage;
    BitmapHolder holder;


    public static DisplayFragment getNewInstance(BitmapHolder holder) {
        DisplayFragment fragment = new DisplayFragment();
        fragment.holder = holder;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_display, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews(view);
    }

    private void setupViews(View view) {
        btnSave = view.findViewById(R.id.btn_save);
        ivAsciiImage = view.findViewById(R.id.iv_ascii_image);
        ivAsciiImage.setImageBitmap(holder.getBitmap());
        btnSave.setOnClickListener(v -> {
            onSaveImageClicked();
        });
    }


    private void onSaveImageClicked() {
        Bitmap originalBitmap = holder.getBitmap();
        ContentProviderUtils.saveBitmap(requireContext(), originalBitmap);
        Intent intent = ContentProviderUtils.getShareBitmapIntent(requireContext());
        requireContext().startActivity(intent);
    }

}
