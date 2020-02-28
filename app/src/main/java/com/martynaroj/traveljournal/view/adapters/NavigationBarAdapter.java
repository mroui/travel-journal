package com.martynaroj.traveljournal.view.adapters;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.List;

public class NavigationBarAdapter extends FragmentStatePagerAdapter {

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
    public int getItemPosition(@NonNull Object object) {
        Fragment fragment = (Fragment) object;
        int position = fragmentsList.indexOf(fragment);
        if (position >= 0) {
            return super.getItemPosition(object);
        } else {
            return POSITION_NONE;
        }
    }


    @Override
    public int getCount() {
        return fragmentsList.size();
    }


    public void changeItem(int position, Fragment fragment) {
        fragmentsList.set(position, fragment);
    }

}