package com.martynaroj.traveljournal.view.others.classes;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Objects;

public abstract class FileUriUtils {

    static String getPath(final Context context, final Uri uri) {
        if (DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                String fullPath = getPathFromExtSD(DocumentsContract.getDocumentId(uri).split(":"));
                if (!fullPath.equals(""))
                    return fullPath;
                else
                    return null;
            } else if (isDownloadsDocument(uri)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    try (Cursor cursor = context.getContentResolver().query(uri,
                            new String[]{MediaStore.MediaColumns.DISPLAY_NAME},
                            null, null, null)) {
                        if (cursor != null && cursor.moveToFirst()) {
                            String path = Environment.getExternalStorageDirectory().toString()
                                    + "/Download/" + cursor.getString(0);
                            if (!TextUtils.isEmpty(path))
                                return path;
                        }
                    }
                    String id = DocumentsContract.getDocumentId(uri);
                    if (!TextUtils.isEmpty(id)) {
                        if (id.startsWith("raw:"))
                            return id.replaceFirst("raw:", "");
                        String[] contentUriPrefixesToTry = new String[]{
                                "content://downloads/public_downloads",
                                "content://downloads/my_downloads"
                        };
                        for (String contentUriPrefix : contentUriPrefixesToTry)
                            try {
                                return getDataColumn(context,
                                        ContentUris.withAppendedId(Uri.parse(contentUriPrefix),
                                                Long.parseLong(id)), null, null);
                            } catch (NumberFormatException e) {
                                return Objects.requireNonNull(uri.getPath()).replaceFirst("^/document/raw:", "")
                                        .replaceFirst("^raw:", "");
                            }
                    }
                } else {
                    String id = DocumentsContract.getDocumentId(uri);
                    if (id.startsWith("raw:"))
                        return id.replaceFirst("raw:", "");
                    return getDataColumn(context,
                            ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                                    Long.parseLong(id)), null, null);
                }
            } else if (isMediaDocument(uri)) {
                String[] split = DocumentsContract.getDocumentId(uri).split(":");
                String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type))
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                else if ("video".equals(type))
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                else if ("audio".equals(type))
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                return getDataColumn(context, contentUri, "_id=?", new String[]{split[1]});
            } else if (isGoogleDriveUri(uri))
                return getDriveFilePath(uri, context);
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();
            if (isGoogleDriveUri(uri))
                return getDriveFilePath(uri, context);
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N)
                return getMediaFilePathForN(uri, context);
            else
                return getDataColumn(context, uri, null, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme()))
            return uri.getPath();
        return null;
    }

    private static String getPathFromExtSD(String[] pathData) {
        final String relativePath = "/" + pathData[1];
        String fullPath;
        if ("primary".equalsIgnoreCase(pathData[0])) {
            fullPath = Environment.getExternalStorageDirectory() + relativePath;
            if (new File(fullPath).exists())
                return fullPath;
        }
        fullPath = System.getenv("SECONDARY_STORAGE") + relativePath;
        if (new File(fullPath).exists())
            return fullPath;
        fullPath = System.getenv("EXTERNAL_STORAGE") + relativePath;
        if (new File(fullPath).exists())
            return fullPath;
        return fullPath;
    }

    @SuppressLint("Recycle")
    private static String getDriveFilePath(Uri uri, Context context) {
        Cursor returnCursor = context.getContentResolver().query(uri,
                null, null, null, null);
        if (returnCursor != null) {
            returnCursor.moveToFirst();
            File file = new File(context.getCacheDir(),
                    returnCursor.getString(returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)));
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(uri);
                FileOutputStream outputStream = new FileOutputStream(file);
                int read;
                assert inputStream != null;
                final byte[] buffers = new byte[Math.min(inputStream.available(), 1024 * 1024)];
                while ((read = inputStream.read(buffers)) != -1)
                    outputStream.write(buffers, 0, read);
                inputStream.close();
                outputStream.close();
            } catch (Exception ignored) {
            }
            return file.getPath();
        } else
            return uri.getPath();
    }

    @SuppressLint("Recycle")
    private static String getMediaFilePathForN(Uri uri, Context context) {
        Cursor returnCursor = context.getContentResolver().query(uri,
                null, null, null, null);
        if (returnCursor != null) {
            returnCursor.moveToFirst();
            File file = new File(context.getFilesDir(),
                    returnCursor.getString(returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)));
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(uri);
                FileOutputStream outputStream = new FileOutputStream(file);
                int read;
                assert inputStream != null;
                final byte[] buffers = new byte[Math.min(inputStream.available(), 1024 * 1024)];
                while ((read = inputStream.read(buffers)) != -1)
                    outputStream.write(buffers, 0, read);
                inputStream.close();
                outputStream.close();
            } catch (Exception ignored) {
            }
            return file.getPath();
        } else
            return uri.getPath();
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        String[] projection = {"_data"};
        try (Cursor cursor = context.getContentResolver().query(uri, projection, selection,
                selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst())
                return cursor.getString(cursor.getColumnIndexOrThrow("_data"));
        }
        return null;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    private static boolean isGoogleDriveUri(Uri uri) {
        return "com.google.android.apps.docs.storage".equals(uri.getAuthority())
                || "com.google.android.apps.docs.storage.legacy".equals(uri.getAuthority());
    }


    public static String getFileName(Context context, Uri uri) {
        if (context != null) {
            String result = null;
            if (uri.getScheme() != null && uri.getScheme().equals("content")) {
                try (Cursor cursor = context.getContentResolver().query(
                        uri, null, null, null, null)) {
                    if (cursor != null && cursor.moveToFirst())
                        result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
            if (result == null) {
                result = uri.getPath();
                if (result != null) {
                    int cut = result.lastIndexOf('/');
                    if (cut != -1) result = result.substring(cut + 1);
                }
            }
            return result;
        }
        return null;
    }

}