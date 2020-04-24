package com.martynaroj.traveljournal.view.interfaces;

import android.view.View;
import android.widget.ProgressBar;

public interface ProgressBarListener {

    void startProgressBar(View root, View progressBarLayout, ProgressBar progressBar);

    void stopProgressBar(View root, View progressBarLayout, ProgressBar progressBar);

}
