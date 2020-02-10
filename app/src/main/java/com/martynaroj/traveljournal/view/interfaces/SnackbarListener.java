package com.martynaroj.traveljournal.view.interfaces;

import android.app.Activity;
import android.view.View;

public interface SnackbarListener {

    void showSnackBar(View root, Activity activity, String message, int duration);

}
