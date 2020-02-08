package com.martynaroj.traveljournal.view.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.martynaroj.traveljournal.databinding.FragmentProfileSettingsBinding;
import com.martynaroj.traveljournal.view.adapters.HashtagAdapter;
import com.martynaroj.traveljournal.view.base.BaseFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProfileSettingsFragment extends BaseFragment {

    private FragmentProfileSettingsBinding binding;

    static ProfileSettingsFragment newInstance() {
        return new ProfileSettingsFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileSettingsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        List<String> fruits = new ArrayList<>(Arrays.asList("abc", "aaaa", "aaabdedede", "efloa"));
        final HashtagAdapter adapter = new HashtagAdapter(getContext(), fruits);
        binding.profileSettingsPersonalPreferencesInput.setAdapter(adapter);
        binding.profileSettingsPersonalPreferencesInput.setThreshold(1);

        return view;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

}
