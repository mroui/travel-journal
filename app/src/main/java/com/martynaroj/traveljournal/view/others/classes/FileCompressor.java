package com.martynaroj.traveljournal.view.others.classes;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import id.zelory.compressor.Compressor;

public abstract class FileCompressor {

    public static byte[] compressToByte(Context context, Uri uri, int height, int width,
                                         int quality, Bitmap.CompressFormat compressFormat) {
        if (uri.getPath() != null) {
            File newFile = new File(uri.getPath());
            Bitmap compressor;
            try {
                compressor = new Compressor(context)
                        .setMaxHeight(height)
                        .setMaxWidth(width)
                        .setQuality(quality)
                        .compressToBitmap(newFile);
            } catch (IOException e) {
                return null;
            }
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            compressor.compress(compressFormat, quality, byteArrayOutputStream);

            return byteArrayOutputStream.toByteArray();
        }
        return null;
    }

}
