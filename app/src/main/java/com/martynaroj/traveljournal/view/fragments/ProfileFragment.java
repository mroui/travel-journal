package com.martynaroj.traveljournal.view.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.FragmentProfileBinding;
import com.martynaroj.traveljournal.services.models.Address;
import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;
import com.martynaroj.traveljournal.viewmodels.AddressViewModel;
import com.martynaroj.traveljournal.viewmodels.UserViewModel;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ProfileFragment extends BaseFragment implements View.OnClickListener {

    private FragmentProfileBinding binding;
    private UserViewModel userViewModel;
    private User user;
    private User loggedUser;

    private AddressViewModel addressViewModel;
    private Geocoder geocoder;

    public static ProfileFragment newInstance(User user) {
        return new ProfileFragment(user);
    }


    public ProfileFragment() {
        super();
    }


    private ProfileFragment(User user) {
        super();
        this.user = user;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initViewModels();
        initGeocoder();
        setListeners();

        initUser();
        observeUserChanges();
        getLoggedUser();

        return view;
    }

    private void initUser() {
        if (user != null) {
            binding.setUser(user);
            initPreferences();
            initLocalization();
        } else if (getParentFragmentManager().getBackStackEntryCount() > 0) {
            getParentFragmentManager().popBackStack();
        }
    }


    private void initGeocoder() {
        geocoder = new Geocoder(getActivity(), Locale.getDefault());
    }


    private void observeUserChanges() {
        userViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                this.user = user;
                binding.setUser(user);
                initPreferences();
                initLocalization();
            }
        });
    }


    private void getLoggedUser() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            startProgressBar();
            userViewModel.getUserData(firebaseAuth.getCurrentUser().getUid());
            userViewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
                if (user != null) {
                    binding.setLoggedUser(user);
                    this.loggedUser = user;
                    checkProfile();
                } else {
                    showSnackBar(getResources().getString(R.string.messages_error_current_user_not_available), Snackbar.LENGTH_LONG);
                }
                stopProgressBar();
            });
        }
    }


    private void checkProfile() {
        if (user != null && user.getUid().equals(loggedUser.getUid())) {
            openDialogIfNotUpdatedAccount();
        }
    }


    private void openDialogIfNotUpdatedAccount() {
        if (user.getLocation() == null && user.getPreferences() == null && user.getBio() == null
        && user.getPhoto() == null)
            openUpdateProfileDialog();
    }


    private void openUpdateProfileDialog() {
        if (getContext() != null) {
            TextView title = new TextView(getActivity());
            title.setText(getString(R.string.dialog_button_update_account_title));
            title.setPadding(0, 32, 0, 0);
            title.setGravity(Gravity.CENTER);
            title.setTextColor(getResources().getColor(R.color.main_blue));
            title.setTextSize(18);

            final AlertDialog dialog = new MaterialAlertDialogBuilder(getContext())
                    .setCustomTitle(title)
                    .setMessage(getString(R.string.dialog_button_update_account_desc))
                    .setPositiveButton(getString(R.string.dialog_button_now), (dialogInterface, i) -> {
                        dialogInterface.cancel();
                        openSettings();
                    })
                    .setNegativeButton(getString(R.string.dialog_button_later), null)
                    .show();

            TextView messageText = dialog.findViewById(android.R.id.message);
            Objects.requireNonNull(messageText).setGravity(Gravity.CENTER);
        }
    }


    private void initLocalization() {
        if (user != null) {
            if (user.getLocation() != null && !user.getLocation().equals("")) {
                startProgressBar();
                addressViewModel.getAddress(user.getLocation());
                addressViewModel.getAddressData().observe(getViewLifecycleOwner(), address -> {
                    if (address != null) {
                        getLocationCity(address);
                    }
                    stopProgressBar();
                });
            } else stopProgressBar();
        }
    }


    private void getLocationCity(Address address) {
        try {
            List<android.location.Address> addresses = geocoder.getFromLocation(address.getLatitude(), address.getLongitude(), 1);
            if (addresses != null && addresses.size() > 0) {
                binding.setLocation(addresses.get(0).getLocality() + ", " + addresses.get(0).getCountryName());
            }
        } catch (IOException ignored) {}
    }


    private void initPreferences() {
        if (user != null && user.getPreferences() != null) {
            binding.profilePreferences.setData(user.getPreferences(), item -> {
                SpannableString spannableString = new SpannableString(item);
                spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#000000")), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                return spannableString;
            });
        }
    }


    private void initViewModels() {
        if (getActivity() != null) {
            userViewModel = new ViewModelProvider(getActivity()).get(UserViewModel.class);
            addressViewModel = new ViewModelProvider(getActivity()).get(AddressViewModel.class);
        }
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
                getContactInfo();
                return;
            case R.id.profile_travels:
                openTravels();
                return;
            case R.id.profile_friends:
                getFriendsInfo();
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


    private void getFriendsInfo() {
        getNavigationInteractions().changeFragment(getParentFragment(), FriendsListFragment.newInstance(), true);
    }


    private void getContactInfo() {
        if (user != null) {
            if (loggedUser != null && user.isUserProfile(loggedUser)) {
                if (getContext() != null) {
                    new MaterialAlertDialogBuilder(getContext())
                            .setTitle(getResources().getString(R.string.dialog_button_my_email_title))
                            .setMessage(user.getEmail())
                            .setPositiveButton(getString(R.string.dialog_button_ok), null)
                            .show();
                }
            } else {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("plain/text");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{user.getEmail()});
                startActivity(Intent.createChooser(intent, ""));
            }
        }
    }


    private void openTravels() {
        getNavigationInteractions().changeFragment(getParentFragment(), TravelsListFragment.newInstance(), true);
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
        showSnackBar(getResources().getString(R.string.messages_signing_out_success), Snackbar.LENGTH_SHORT);
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
