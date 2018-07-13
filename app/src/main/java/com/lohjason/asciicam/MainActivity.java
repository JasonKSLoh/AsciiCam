package com.lohjason.asciicam;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    Button    button;
    TextView  tvAscii;
    ImageView ivOriginalImage;

    int imageToShow = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        button = findViewById(R.id.btn_start);
        tvAscii = findViewById(R.id.tv_ascii);
        tvAscii.setTypeface(Typeface.MONOSPACE);
        ivOriginalImage = findViewById(R.id.iv_originalimage);
        button.setOnClickListener(v -> {
            int modImageToShow = imageToShow % 3;
            if (modImageToShow == 0) {
                showBitmap();
            } else if (modImageToShow == 1){
                showAscii(false);
            } else {
                showAscii(true);
            }
            imageToShow++;
        });
    }

    private void showBitmap() {
        Bitmap bitmap       = BitmapFactory.decodeResource(getResources(), R.drawable.dog);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 64, 64, false);
        tvAscii.setVisibility(View.GONE);
        ivOriginalImage.setVisibility(View.VISIBLE);
        ivOriginalImage.setImageBitmap(scaledBitmap);
    }

    private void showAscii(boolean invertDarkness) {
        tvAscii.setVisibility(View.VISIBLE);
        ivOriginalImage.setVisibility(View.GONE);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.dog);

        int    targetHeight = 64;
        int    targetWidth  = 64;
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, false).copy(Bitmap.Config.ARGB_8888, true);
        int[]  scaledPixels = new int[targetHeight * targetWidth];
        scaledBitmap.getPixels(scaledPixels, 0, targetWidth, 0, 0, targetWidth, targetHeight);

        int[] brightness = new int[scaledPixels.length];
        for (int i = 0; i < scaledPixels.length; i++) {
            int color = scaledPixels[i];
            int red   = (color >> 16) & 0xff;
            int green = (color >> 8) & 0xff;
            int blue  = (color >> 24) & 0xff;

            brightness[i] = (red + green + blue) / 3;
            if (color != 0) {
                Log.d("+_", "Original Value: " + scaledPixels[i]);
                Log.d("+_", "Red: " + red + " Blue: " + blue + "Green: " + green);
            }
            if (brightness[i] > 0) {
                Log.d("++_", "Brightness of " + brightness[i] + " was found");
            }
        }

        String[] asciiArt = new String[targetHeight];
        if(invertDarkness){
            for (int i = 0; i < targetHeight; i++) {
                char[] chars = new char[targetWidth];
                for (int j = 0; j < targetWidth; j++) {
                    chars[j] = TextConverter.getCharForBrightnessInverse(brightness[targetWidth * i + j]);
                }
                asciiArt[i] = new String(chars);
            }
        } else {
            for (int i = 0; i < targetHeight; i++) {
                char[] chars = new char[targetWidth];
                for (int j = 0; j < targetWidth; j++) {
                    chars[j] = TextConverter.getCharForBrightness(brightness[targetWidth * i + j]);
                }
                asciiArt[i] = new String(chars);
            }
        }


        StringBuilder sb = new StringBuilder();
        for (String s : asciiArt) {
            sb.append(s).append('\n');
        }

        Log.d("+_", sb.toString());
        tvAscii.setText(sb.toString());
    }
}
