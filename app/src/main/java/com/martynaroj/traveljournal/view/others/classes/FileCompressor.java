package com.martynaroj.traveljournal.view.others.classes;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import id.zelory.compressor.Compressor;

public abstract class FileCompressor {

    public static byte[] compressToByte(Context context, Uri uri) {
        Bitmap compressor;
        String path = FileUriUtils.getPath(context, uri);
        if (path != null) {
            try {
                compressor = new Compressor(context)
                        .setMaxHeight(100)
                        .setMaxWidth(100)
                        .setQuality(100)
                        .compressToBitmap(new File(path));
            } catch (IOException e) {
                return null;
            }
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            compressor.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        } else
            return null;
    }

}
