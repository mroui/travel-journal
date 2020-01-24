package com.martynaroj.traveljournal.Interfaces;

import androidx.fragment.app.Fragment;

public interface NavigationListener {

    void changeFragment(Fragment fragment, Boolean addToBackStack);

}