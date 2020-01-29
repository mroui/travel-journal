package com.martynaroj.traveljournal.View.Interfaces;

import androidx.fragment.app.Fragment;

public interface NavigationListener {

    void changeFragment(Fragment previous, Fragment next, Boolean addToBackStack);

    void changeNavigationBarItem(int id, Fragment fragment);
}