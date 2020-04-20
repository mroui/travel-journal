package com.martynaroj.traveljournal.view.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.FragmentResetPasswordBinding;
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.view.others.classes.FormHandler;
import com.martynaroj.traveljournal.view.others.enums.Status;
import com.martynaroj.traveljournal.viewmodels.AuthViewModel;

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


    //INIT DATA-------------------------------------------------------------------------------------


    private void initAuthViewModel() {
        if (getActivity() != null)
            authViewModel = new ViewModelProvider(getActivity()).get(AuthViewModel.class);
    }


    //LISTENERS-------------------------------------------------------------------------------------


    private void setListeners() {
        new FormHandler(getContext()).addWatcher(binding.forgotPasswordEmailInput, binding.forgotPasswordEmailLayout);
        binding.forgotPasswordArrowButton.setOnClickListener(this);
        binding.forgotPasswordBackButton.setOnClickListener(this);
        binding.forgotPasswordSendButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.forgot_password_arrow_button:
            case R.id.forgot_password_back_button:
                hideKeyboard();
                back();
                break;
            case R.id.forgot_password_send_button:
                if (validateEmail())
                    sendResetPasswordMail();
                break;
        }
    }


    //VALIDATION------------------------------------------------------------------------------------


    private boolean validateEmail() {
        return new FormHandler(getContext()).validateInput(binding.forgotPasswordEmailInput,
                binding.forgotPasswordEmailLayout);
    }


    //RESET PASS------------------------------------------------------------------------------------


    private void sendResetPasswordMail() {
        if (validateEmail()) {
            startProgressBar();
            String email = Objects.requireNonNull(binding.forgotPasswordEmailInput.getText()).toString();
            authViewModel.sendPasswordResetEmail(email);
            authViewModel.getUserForgotPasswordLiveData().observe(this, user -> {
                stopProgressBar();
                if (user.getStatus() == Status.SUCCESS) {
                    showSnackBar(user.getMessage(), Snackbar.LENGTH_SHORT);
                    back();
                } else {
                    showSnackBar(user.getMessage(), Snackbar.LENGTH_LONG);
                }
            });
        }
    }


    //OTHERS----------------------------------------------------------------------------------------


    private void back() {
        if (getParentFragmentManager().getBackStackEntryCount() > 0)
            getParentFragmentManager().popBackStack();
    }


    private void startProgressBar() {
        getProgressBarInteractions().startProgressBar(binding.getRoot(),
                binding.forgotPasswordProgressbarLayout, binding.forgotPasswordProgressbar);
    }


    private void stopProgressBar() {
        getProgressBarInteractions().stopProgressBar(binding.getRoot(),
                binding.forgotPasswordProgressbarLayout, binding.forgotPasswordProgressbar);
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
