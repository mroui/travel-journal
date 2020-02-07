package com.martynaroj.traveljournal.view.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.view.others.classes.FormHandler;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.view.others.enums.Status;
import com.martynaroj.traveljournal.viewmodels.AuthViewModel;
import com.martynaroj.traveljournal.databinding.FragmentResetPasswordBinding;

import java.util.Objects;

public class ResetPasswordFragment extends BaseFragment implements View.OnClickListener {

    private FragmentResetPasswordBinding binding;
    private AuthViewModel authViewModel;

    static ResetPasswordFragment newInstance() {
        return new ResetPasswordFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentResetPasswordBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initAuthViewModel();
        setListeners();

        return view;
    }


    private void initAuthViewModel() {
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
    }


    private void setListeners() {
        new FormHandler().addWatcher(binding.forgotPasswordEmailInput, binding.forgotPasswordEmailLayout);
        binding.forgotPasswordArrowButton.setOnClickListener(this);
        binding.forgotPasswordBackButton.setOnClickListener(this);
        binding.forgotPasswordSendButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.forgot_password_arrow_button:
            case R.id.forgot_password_back_button:
                if (getParentFragmentManager().getBackStackEntryCount() > 0)
                    getParentFragmentManager().popBackStack();
                return;
            case R.id.forgot_password_send_button:
                if (validateEmail())
                    sendResetPasswordMail();
        }
    }


    private void startProgressBar() {
        binding.forgotPasswordProgressbarLayout.setVisibility(View.VISIBLE);
        binding.forgotPasswordProgressbar.start();
        enableDisableViewGroup((ViewGroup) binding.getRoot(), false);
    }


    private void stopProgressBar() {
        binding.forgotPasswordProgressbarLayout.setVisibility(View.INVISIBLE);
        binding.forgotPasswordProgressbar.stop();
        enableDisableViewGroup((ViewGroup) binding.getRoot(), true);
    }


    private void showSnackBar(String message, int duration) {
        Snackbar snackbar = Snackbar.make(binding.getRoot(), message, duration);
        snackbar.setAnchorView(Objects.requireNonNull(getActivity()).findViewById(R.id.bottom_navigation_view));
        TextView textView = snackbar.getView().findViewById(R.id.snackbar_text);
        textView.setMaxLines(3);
        snackbar.show();
    }


    private boolean validateEmail() {
        return new FormHandler().validateInput(binding.forgotPasswordEmailInput, binding.forgotPasswordEmailLayout);
    }


    private void sendResetPasswordMail() {
        if (validateEmail()) {
            startProgressBar();
            String email = Objects.requireNonNull(binding.forgotPasswordEmailInput.getText()).toString();

            authViewModel.sendPasswordResetEmail(email);
            authViewModel.getUserForgotPasswordLiveData().observe(this, user -> {
                stopProgressBar();
                if (user.getStatus() == Status.SUCCESS) {
                    showSnackBar(user.getMessage(), Snackbar.LENGTH_LONG);
                    getParentFragmentManager().popBackStack();
                } else {
                    showSnackBar(user.getMessage(), Snackbar.LENGTH_LONG);
                }
            });
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
