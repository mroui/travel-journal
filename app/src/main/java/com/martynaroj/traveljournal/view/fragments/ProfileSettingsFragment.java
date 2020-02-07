package com.martynaroj.traveljournal.view.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.martynaroj.traveljournal.databinding.FragmentProfileSettingsBinding;
import com.martynaroj.traveljournal.view.base.BaseFragment;

public class ProfileSettingsFragment extends BaseFragment {

    private FragmentProfileSettingsBinding binding;

    static ProfileSettingsFragment newInstance() {
        return new ProfileSettingsFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileSettingsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        return view;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

}
