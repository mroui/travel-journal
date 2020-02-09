package com.martynaroj.traveljournal.view.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.martynaroj.traveljournal.databinding.FragmentProfileSettingsBinding;
import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.view.adapters.HashtagAdapter;
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ProfileSettingsFragment extends BaseFragment {

    private FragmentProfileSettingsBinding binding;
    private User user;

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

        initUser();
        initPrivacySelectItems();
        initUserData();

        return view;
    }

    private void initUser() {
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            user = (User) bundle.getSerializable(Constants.USER);
            binding.setUser(user);
        } else user = new User();
    }


    private void initPrivacySelectItems() {
        binding.profileSettingsPrivacyEmailSelect.setItems(Constants.PUBLIC, Constants.FRIENDS, Constants.ONLY_ME);
        binding.profileSettingsPrivacyLocationSelect.setItems(Constants.PUBLIC, Constants.FRIENDS, Constants.ONLY_ME);
        binding.profileSettingsPrivacyPreferencesSelect.setItems(Constants.PUBLIC, Constants.FRIENDS, Constants.ONLY_ME);
    }

    private void initUserData() {
        int index = Objects.requireNonNull(user.getPrivacy().get(Constants.EMAIL));
        binding.profileSettingsPrivacyEmailSelect.setSelectedIndex(index);

        index = Objects.requireNonNull(user.getPrivacy().get(Constants.LOCATION));
        binding.profileSettingsPrivacyLocationSelect.setSelectedIndex(index);

        index = Objects.requireNonNull(user.getPrivacy().get(Constants.PREFERENCES));
        binding.profileSettingsPrivacyPreferencesSelect.setSelectedIndex(index);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

}
