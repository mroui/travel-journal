package com.martynaroj.traveljournal.view.interfaces;

import android.view.View;

import com.victor.loading.rotate.RotateLoading;

public interface ProgressBarListener {

    void startProgressBar(View root, View progressBarLayout, RotateLoading progressBar);

    void stopProgressBar(View root, View progressBarLayout, RotateLoading progressBar);

}
