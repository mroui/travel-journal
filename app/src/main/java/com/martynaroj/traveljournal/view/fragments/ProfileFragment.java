package com.martynaroj.traveljournal.view.fragments;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.FragmentProfileBinding;
import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.view.others.enums.Status;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;
import com.martynaroj.traveljournal.viewmodels.AuthViewModel;

import java.util.Objects;

public class ProfileFragment extends BaseFragment implements View.OnClickListener {

    private FragmentProfileBinding binding;
    private AuthViewModel authViewModel;
    private User user;

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initAuthViewModel();
        setListeners();
        initUser();

//        List<String> names = new ArrayList<>(Arrays.asList("andfgna", "olaasdgfd", "ansdaasdna", "ola","anggna", "ola","asdfsdfnna", "ola", "dsfgsdfsdfs", "asfddsgdfg"));
//        binding.profilePreferences.setData(names, item -> {
//            SpannableString spannableString = new SpannableString("#" + item);
//            spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#000000")), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//            return spannableString;
//        });

        return view;
    }


    private void initUser() {
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            user = (User) bundle.getSerializable(Constants.USER);
            binding.setUser(user);
        } else {
            getCurrentUser();
        }
    }


    private void getCurrentUser() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            startProgressBar();
            authViewModel.getUserFromDatabase(firebaseAuth.getCurrentUser().getUid());
            authViewModel.getUserLiveData().observe(getViewLifecycleOwner(), userData -> {
                if (userData.getStatus() == Status.SUCCESS) {
                    user = userData.getData();
                    binding.setUser(user);
                } else {
                    showSnackBar(userData.getMessage(), Snackbar.LENGTH_LONG);
                }
                stopProgressBar();
            });
        } else {
            showSnackBar("ERROR: Current user is not available, try again later", Snackbar.LENGTH_LONG);
        }
    }


    private void initAuthViewModel() {
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
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
        getNavigationInteractions().changeFragment(this, ProfileSettingsFragment.newInstance(), true);
    }


    private void seeAllPreferences() {
        ConstraintLayout.LayoutParams constraintLayout = (ConstraintLayout.LayoutParams) binding.profilePreferences.getLayoutParams();
        String seePreferences;
        if (binding.profilePreferences.getLayoutParams().height == ConstraintLayout.LayoutParams.WRAP_CONTENT) {
            constraintLayout.height = Constants.HASHTAG_HEIGHT;
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
        binding.profileProgressbarLayout.setVisibility(View.VISIBLE);
        binding.profileProgressbar.start();
        enableDisableViewGroup((ViewGroup) binding.getRoot(), false);
    }


    private void stopProgressBar() {
        binding.profileProgressbarLayout.setVisibility(View.INVISIBLE);
        binding.profileProgressbar.stop();
        enableDisableViewGroup((ViewGroup) binding.getRoot(), true);
    }


    private void showSnackBar(String message, int duration) {
        Snackbar snackbar = Snackbar.make(binding.getRoot(), message, duration);
        snackbar.setAnchorView(Objects.requireNonNull(getActivity()).findViewById(R.id.bottom_navigation_view));
        TextView textView = snackbar.getView().findViewById(R.id.snackbar_text);
        textView.setMaxLines(3);
        snackbar.show();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
