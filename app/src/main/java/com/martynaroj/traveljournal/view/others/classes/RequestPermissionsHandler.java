package com.martynaroj.traveljournal.view.others.classes;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.martynaroj.traveljournal.view.others.interfaces.Constants;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public abstract class RequestPermissionsHandler {

    public static void requestReadStorage(Activity activity) {
        if (activity != null) {
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    Constants.RC_EXTERNAL_STORAGE_FILE
            );
        }
    }


    public static void requestWriteStorage(Activity activity) {
        if (activity != null) {
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    Constants.RC_EXTERNAL_STORAGE_FILE
            );
        }
    }


    public static void requestFineLocation(Fragment fragment) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            fragment.requestPermissions(new String[]{ACCESS_FINE_LOCATION},
                    Constants.RC_ACCESS_FINE_LOCATION);
        }
    }


    public static boolean isReadStorageGranted(Context context) {
        if (context != null)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED;
            else
                return true;
        else
            return false;
    }


    public static boolean isWriteStorageGranted(Context context) {
        if (context != null)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED;
            else
                return true;
        else
            return false;
    }


    public static boolean isFineLocationGranted(Context context) {
        if (context != null)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                return ContextCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;
            else
                return true;
        else
            return false;
    }


    public static boolean isOnResultGranted(int requestCode, @NonNull int[] grantResults) {
        return requestCode == Constants.RC_ACCESS_FINE_LOCATION && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED;
    }

}
