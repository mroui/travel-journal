package com.martynaroj.traveljournal.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

public class NavigationBarAdapter extends FragmentPagerAdapter {

    private List<Fragment> fragmentsList;

    public NavigationBarAdapter(List<Fragment> list, FragmentManager manager) {
        super(manager);
        fragmentsList = list;
    }


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