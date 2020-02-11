package com.martynaroj.traveljournal.view.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.FragmentProfileSettingsBinding;
import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;
import com.martynaroj.traveljournal.viewmodels.UserViewModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProfileSettingsFragment extends BaseFragment implements View.OnClickListener {

    private FragmentProfileSettingsBinding binding;
    private UserViewModel userViewModel;
    private User user;

    static ProfileSettingsFragment newInstance() {
        return new ProfileSettingsFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileSettingsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

//        List<String> fruits = new ArrayList<>(Arrays.asList("abc", "aaaa", "aaabdedede", "efloa"));
//        final HashtagAdapter adapter = new HashtagAdapter(getContext(), fruits);
//        binding.profileSettingsPersonalPreferencesInput.setAdapter(adapter);
//        binding.profileSettingsPersonalPreferencesInput.setThreshold(1);

        initPrivacySelectItems();
        setListeners();
        initUserViewModel();

        initUser();

        return view;
    }


    private void initPrivacySelectItems() {
        binding.profileSettingsPrivacyEmailSelect.setItems(Constants.PUBLIC, Constants.FRIENDS, Constants.ONLY_ME);
        binding.profileSettingsPrivacyLocationSelect.setItems(Constants.PUBLIC, Constants.FRIENDS, Constants.ONLY_ME);
        binding.profileSettingsPrivacyPreferencesSelect.setItems(Constants.PUBLIC, Constants.FRIENDS, Constants.ONLY_ME);
    }


    private void setListeners() {
        binding.profileSettingsArrowButton.setOnClickListener(this);
        binding.profileSettingsPersonalPicturePhoto.setOnClickListener(this);
        binding.profileSettingsPersonalLocationButton.setOnClickListener(this);
        binding.profileSettingsPersonalSaveButton.setOnClickListener(this);
        binding.profileSettingsAccountUsernameSaveButton.setOnClickListener(this);
        binding.profileSettingsAccountEmailSaveButton.setOnClickListener(this);
        binding.profileSettingsAccountPasswordSaveButton.setOnClickListener(this);
        binding.profileSettingsPrivacySaveButton.setOnClickListener(this);
    }


    private void initUserViewModel() {
        if (getActivity() != null)
            userViewModel = new ViewModelProvider(getActivity()).get(UserViewModel.class);
    }


    private void initUser() {
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            user = (User) bundle.getSerializable(Constants.USER);
            binding.setUser(user);
            initUserData();
        } else {
            getCurrentUser();
        }
    }


    private void getCurrentUser() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            startProgressBar();
            userViewModel.getDataSnapshotLiveData(firebaseAuth.getCurrentUser().getUid());
            userViewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
                if (user != null) {
                    this.user = user;
                    binding.setUser(user);
                    initUserData();
                } else {
                    this.user = new User();
                    showSnackBar("ERROR: No such User in a database, try again later", Snackbar.LENGTH_LONG);
                }
                stopProgressBar();
            });
        } else {
            showSnackBar("ERROR: Current user is not available, try again later", Snackbar.LENGTH_LONG);
        }
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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.profile_settings_arrow_button:
                if (getParentFragmentManager().getBackStackEntryCount() > 0)
                    getParentFragmentManager().popBackStack();
                return;
            case R.id.profile_settings_personal_picture_photo:
                showSnackBar("clicked: photo", Snackbar.LENGTH_SHORT);
                return;
            case R.id.profile_settings_personal_location_button:
                showSnackBar("clicked: find me", Snackbar.LENGTH_SHORT);
                return;
            case R.id.profile_settings_personal_save_button:
                savePersonalChanges();
                return;
            case R.id.profile_settings_account_username_save_button:
                showSnackBar("clicked: username change", Snackbar.LENGTH_SHORT);
                return;
            case R.id.profile_settings_account_email_save_button:
                showSnackBar("clicked: email change", Snackbar.LENGTH_SHORT);
                return;
            case R.id.profile_settings_account_password_save_button:
                showSnackBar("clicked: password change", Snackbar.LENGTH_SHORT);
                return;
            case R.id.profile_settings_privacy_save_button:
                savePrivacyChanges();
        }
    }


    private void savePersonalChanges() {
        Map<String, Object> changes = new HashMap<>();
        if (!user.getBio().equals(binding.profileSettingsPersonalBioInput.getText().toString())) {
            changes.put("bio", binding.profileSettingsPersonalBioInput.getText().toString());
        }
        if (!changes.isEmpty()) {
            updateUser(changes);
        }
    }


    private void savePrivacyChanges() {
        Map<String, Object> changes = new HashMap<>();
        if (user.getPrivacy().get(Constants.EMAIL) != binding.profileSettingsPrivacyEmailSelect.getSelectedIndex()) {
            changes.put(Constants.PRIVACY + "." + Constants.EMAIL, binding.profileSettingsPrivacyEmailSelect.getSelectedIndex());
        }
        if (user.getPrivacy().get(Constants.LOCATION) != binding.profileSettingsPrivacyLocationSelect.getSelectedIndex()) {
            changes.put(Constants.PRIVACY + "." + Constants.LOCATION, binding.profileSettingsPrivacyLocationSelect.getSelectedIndex());
        }
        if (user.getPrivacy().get(Constants.PREFERENCES) != binding.profileSettingsPrivacyPreferencesSelect.getSelectedIndex()) {
            changes.put(Constants.PRIVACY + "." + Constants.PREFERENCES, binding.profileSettingsPrivacyPreferencesSelect.getSelectedIndex());
        }

        if (!changes.isEmpty()) {
            updateUser(changes);
        }
    }


    private void updateUser(Map<String, Object> changes) {
        startProgressBar();
        userViewModel.updateUser(user, changes);
        userViewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                this.user = user;
                userViewModel.setUser(user);
                binding.setUser(user);
                showSnackBar("Changes saved successfully", Snackbar.LENGTH_LONG);
            } else {
                showSnackBar("ERROR: Failed to update, try again later", Snackbar.LENGTH_LONG);
            }
            stopProgressBar();
        });
    }


    private void startProgressBar() {
        getProgressBarInteractions().startProgressBar(binding.getRoot(), binding.profileSettingsProgressbarLayout, binding.profileSettingsProgressbar);
    }


    private void stopProgressBar() {
        getProgressBarInteractions().stopProgressBar(binding.getRoot(), binding.profileSettingsProgressbarLayout, binding.profileSettingsProgressbar);
    }


    private void showSnackBar(String message, int duration) {
        getSnackBarInteractions().showSnackBar(binding.getRoot(), getActivity(), message, duration);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
