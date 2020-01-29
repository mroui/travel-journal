package com.martynaroj.traveljournal.View.Fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.View.Base.BaseFragment;
import com.martynaroj.traveljournal.View.Others.FormHandler;
import com.martynaroj.traveljournal.databinding.FragmentLogInBinding;

public class LogInFragment extends BaseFragment implements View.OnClickListener {

    private FragmentLogInBinding binding;

    public static LogInFragment newInstance() {
        return new LogInFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLogInBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        setListeners();

        return view;
    }


    private void setListeners() {
        new FormHandler().addWatcher(binding.loginEmailInput, binding.loginEmailLayout);
        new FormHandler().addWatcher(binding.loginPasswordInput, binding.loginPasswordLayout);
        binding.loginForgotPasswordButton.setOnClickListener(this);
        binding.loginLogInButton.setOnClickListener(this);
        binding.loginGoogleButton.setOnClickListener(this);
        binding.loginSignUpButton.setOnClickListener(this);
    }


    private boolean validateEmail() {
        return new FormHandler().validateInput(binding.loginEmailInput, binding.loginEmailLayout);
    }


    private boolean validatePassword() {
        return new FormHandler().validateInput(binding.loginPasswordInput, binding.loginPasswordLayout);
    }


    private void logIn() {
        if (validateEmail() && validatePassword())
            Toast.makeText(getContext(), "Login successed", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getContext(), "Login failed", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_log_in_button:
                logIn();
                return;
            case R.id.login_google_button:
                Toast.makeText(getContext(), "Google button", Toast.LENGTH_SHORT).show();
                return;
            case R.id.login_forgot_password_button:
                changeFragment(ResetPasswordFragment.newInstance());
                return;
            case R.id.login_sign_up_button:
                changeFragment(SignUpFragment.newInstance());
        }
    }


    private void changeFragment(Fragment next) {
        clearInputs();
        getNavigationInteractions().changeFragment(this, next, true);
    }


    private void clearInputs() {
        new FormHandler().clearInput(binding.loginEmailInput, binding.loginEmailLayout);
        new FormHandler().clearInput(binding.loginPasswordInput, binding.loginPasswordLayout);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}