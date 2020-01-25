package com.martynaroj.traveljournal.Interfaces;

import androidx.fragment.app.Fragment;

public interface NavigationListener {

    void changeFragment(Fragment previous, Fragment next, Boolean addToBackStack);

}