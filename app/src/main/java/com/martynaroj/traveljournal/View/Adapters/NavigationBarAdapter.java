package com.martynaroj.traveljournal.View.Adapters;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;

public class NavigationBarAdapter extends FragmentPagerAdapter {

    private List<Fragment> fragmentsList;

    public NavigationBarAdapter(List<Fragment> list, FragmentManager manager) {
        super(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        fragmentsList = list;
    }


    @NonNull
    @Override
    public Fragment getItem(int position) {
        if (position >= 0 && position < fragmentsList.size())
            return fragmentsList.get(position);
        return new Fragment();
    }


    @Override
    public int getCount() {
        return fragmentsList.size();
    }

}