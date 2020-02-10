package com.martynaroj.traveljournal.view.base;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.martynaroj.traveljournal.view.interfaces.NavigationListener;
import com.martynaroj.traveljournal.view.interfaces.ProgressBarListener;

public class BaseFragment extends Fragment {

    private NavigationListener navigationListener;
    private ProgressBarListener progressBarListener;

    protected NavigationListener getNavigationInteractions() {
        return navigationListener;
    }

    protected ProgressBarListener getProgressBarInteractions() {
        return progressBarListener;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        navigationListener = null;
        progressBarListener = null;
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof NavigationListener)
            navigationListener = (NavigationListener) context;
        progressBarListener = (ProgressBarListener) context;
    }
}
