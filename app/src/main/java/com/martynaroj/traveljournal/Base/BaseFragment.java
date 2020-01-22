package com.martynaroj.traveljournal.Base;

import android.content.Context;
import android.support.v4.app.Fragment;

import com.martynaroj.traveljournal.Interfaces.NavigationListener;

public class BaseFragment extends Fragment {

    private NavigationListener navigationListener;

    public NavigationListener getNavigationInteractions() {
        return navigationListener;
    }


    @Override
    public void onDetach() {
        super.onDetach();
        navigationListener = null;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof NavigationListener)
            navigationListener = (NavigationListener) context;
    }

}
