package com.martynaroj.traveljournal.view.fragments;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.FragmentProfileBinding;
import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;
import com.martynaroj.traveljournal.viewmodels.UserViewModel;

public class ProfileFragment extends BaseFragment implements View.OnClickListener {

    private FragmentProfileBinding binding;
    private UserViewModel userViewModel;
    private User user;

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initUserViewModel();
        setListeners();

        initUser();

        return view;
    }


    private void initUser() {
        userViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
                if (user != null) {
                    this.user = user;
                    binding.setUser(user);
                    initPreferences();
                }
        });
        getCurrentUser();
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
                    initPreferences();
                } else {
                    showSnackBar("ERROR: No such User in a database, try again later", Snackbar.LENGTH_LONG);
                }
                stopProgressBar();
            });
        } else {
            showSnackBar("ERROR: Current user is not available, try again later", Snackbar.LENGTH_LONG);
        }
    }


    private void initPreferences() {
        if (user.getPreferences() != null) {
            binding.profilePreferences.setData(user.getPreferences(), item -> {
                SpannableString spannableString = new SpannableString(item);
                spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#000000")), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                return spannableString;
            });
        }
    }


    private void initUserViewModel() {
        if (getActivity() != null)
            userViewModel = new ViewModelProvider(getActivity()).get(UserViewModel.class);
    }


    private void setListeners() {
        binding.profileSignOutButton.setOnClickListener(this);
        binding.profileNotifications.setOnClickListener(this);
        binding.profileContact.setOnClickListener(this);
        binding.profileTravels.setOnClickListener(this);
        binding.profileFriends.setOnClickListener(this);
        binding.profileSeeAllPreferences.setOnClickListener(this);
        binding.profileSettingsButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.profile_notifications:
                showSnackBar("clicked: notifications", Snackbar.LENGTH_SHORT);
                return;
            case R.id.profile_contact:
                showSnackBar("clicked: contact", Snackbar.LENGTH_SHORT);
                return;
            case R.id.profile_travels:
                showSnackBar("clicked: travels", Snackbar.LENGTH_SHORT);
                return;
            case R.id.profile_friends:
                showSnackBar("clicked: friends", Snackbar.LENGTH_SHORT);
                return;
            case R.id.profile_see_all_preferences:
                seeAllPreferences();
                return;
            case R.id.profile_settings_button:
                openSettings();
                return;
            case R.id.profile_sign_out_button:
                signOut();
        }
    }


    private void openSettings() {
        Fragment settingsFragment = ProfileSettingsFragment.newInstance();
        Bundle args = new Bundle();
        args.putSerializable(Constants.USER, user);
        settingsFragment.setArguments(args);
        getNavigationInteractions().changeFragment(this, settingsFragment, true);
    }


    private void seeAllPreferences() {
        ConstraintLayout.LayoutParams constraintLayout = (ConstraintLayout.LayoutParams) binding.profilePreferences.getLayoutParams();
        String seePreferences;
        if (binding.profilePreferences.getLayoutParams().height == ConstraintLayout.LayoutParams.WRAP_CONTENT) {
            constraintLayout.height = Constants.PREFERENCES_VIEW_HEIGHT;
            seePreferences = getResources().getString(R.string.profile_see_all_pref);
        } else {
            constraintLayout.height = ConstraintLayout.LayoutParams.WRAP_CONTENT;
            seePreferences = getResources().getString(R.string.profile_see_less_pref);
        }
        binding.profileSeeAllPreferences.setPaintFlags(binding.profileSeeAllPreferences.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        binding.profileSeeAllPreferences.setText(seePreferences);
        binding.profilePreferences.setLayoutParams(constraintLayout);
    }


    private void signOut() {
        startProgressBar();
        FirebaseAuth.getInstance().signOut();
        showSnackBar("You have been signed out successfully", Snackbar.LENGTH_SHORT);
        stopProgressBar();
        getNavigationInteractions().changeNavigationBarItem(2, LogInFragment.newInstance());
    }


    private void startProgressBar() {
        getProgressBarInteractions().startProgressBar(binding.getRoot(), binding.profileProgressbarLayout, binding.profileProgressbar);
    }


    private void stopProgressBar() {
        getProgressBarInteractions().stopProgressBar(binding.getRoot(), binding.profileProgressbarLayout, binding.profileProgressbar);
    }


    private void showSnackBar(String message, int duration) {
        getSnackBarInteractions().showSnackBar(binding.getRoot(), getActivity(), message, duration);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
