package com.lohjason.asciicam.Util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * ContentProviderUtils
 * Created by Jason on 15/7/2018.
 */
public class ContentProviderUtils {
    private static final String PATH          = "images";
    private static final String FILENAME      = "AsciiCamImage";
    private static final String FILEEXTENSION = ".jpg";
    private static final String AUTHORITY     = "com.lohjason.asciicam.fileprovider";

    public static boolean saveBitmap(Context context, Bitmap bitmap) {
        try {
            clearCache(context);
            File cachePath = new File(context.getCacheDir(), PATH);
            cachePath.mkdirs();
            String fileName = FILENAME
                              + new SimpleDateFormat("YYMMddHHmmSS", Locale.getDefault()).format(new Date())
                              + FILEEXTENSION;
            File imageFile = new File(cachePath, fileName);
            FileOutputStream fileOutputStream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            return true;
        } catch (Exception e) {
            Logg.d("+_", "Failed to save Bitmap");
            return  false;
        }
    }


    public static Intent getShareBitmapIntent(Context context)  {
        File   cachePath  = new File(context.getCacheDir(), PATH);
        File[] imageFiles = cachePath.listFiles();

        if(imageFiles.length == 0){
            return null;
        }

        Uri imageUri = FileProvider.getUriForFile(context, AUTHORITY, imageFiles[0]);
        if(imageUri != null){
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(imageUri, context.getContentResolver().getType(imageUri));
            intent.setType("image/png");
            intent.putExtra(Intent.EXTRA_STREAM, imageUri);
            return Intent.createChooser(intent, "Share the Ascii Image");
        }
        return null;
    }

    private static void clearCache(Context context) {
        File file = new File(context.getCacheDir(), PATH);
        if (file.isDirectory()) {
            String[] children = file.list();
            for (String childName : children) {
                new File(file, childName).delete();
            }
        }
    }

}
