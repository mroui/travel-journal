package com.martynaroj.traveljournal.View.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.View.Base.BaseFragment;
import com.martynaroj.traveljournal.databinding.FragmentProfileBinding;

import java.util.Objects;

public class ProfileFragment extends BaseFragment implements View.OnClickListener {

    private FragmentProfileBinding binding;

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        setListeners();

        return view;
    }


    private void setListeners() {
        binding.profileSignOutButton.setOnClickListener(this);
        binding.profileNotifications.setOnClickListener(this);
        binding.profileEdit.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.profile_sign_out_button:
                signOut();
                return;
            case R.id.profile_notifications:
                showSnackBar("clicked: notifications", Snackbar.LENGTH_SHORT);
                return;
            case R.id.profile_edit:
                showSnackBar("clicked: edit", Snackbar.LENGTH_SHORT);
        }
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
