package com.martynaroj.traveljournal.View.Base;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.martynaroj.traveljournal.View.Interfaces.NavigationListener;

public class BaseFragment extends Fragment {

    private NavigationListener navigationListener;

    protected NavigationListener getNavigationInteractions() {
        return navigationListener;
    }


    @Override
    public void onDetach() {
        super.onDetach();
        navigationListener = null;
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof NavigationListener)
            navigationListener = (NavigationListener) context;
    }

}
