package com.martynaroj.traveljournal.view.base;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.martynaroj.traveljournal.view.interfaces.NavigationListener;
import com.martynaroj.traveljournal.view.interfaces.ProgressBarListener;
import com.martynaroj.traveljournal.view.interfaces.SnackbarListener;

public class BaseFragment extends Fragment {

    private NavigationListener navigationListener;
    private ProgressBarListener progressBarListener;
    private SnackbarListener snackbarListener;


    protected NavigationListener getNavigationInteractions() {
        return navigationListener;
    }


    protected ProgressBarListener getProgressBarInteractions() {
        return progressBarListener;
    }


    protected SnackbarListener getSnackBarInteractions() {
        return snackbarListener;
    }


    @SuppressWarnings("ConstantConditions")
    protected void hideKeyboard() {
        if (getActivity() != null) {
            ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        navigationListener = null;
        progressBarListener = null;
        snackbarListener = null;
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof NavigationListener) {
            navigationListener = (NavigationListener) context;
            progressBarListener = (ProgressBarListener) context;
            snackbarListener = (SnackbarListener) context;
        }
    }

}
