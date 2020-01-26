package com.martynaroj.traveljournal.Fragments;


import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.martynaroj.traveljournal.Base.BaseFragment;
import com.martynaroj.traveljournal.Interfaces.Form;
import com.martynaroj.traveljournal.Others.InputTextWatcher;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.FragmentProfileBinding;

public class ProfileFragment extends BaseFragment implements View.OnClickListener, Form {

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
        binding.loginEmailInput.addTextChangedListener(new InputTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (binding.loginEmailInput.hasFocus())
                    validateEmail();
            }
        });
        binding.loginPasswordInput.addTextChangedListener(new InputTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (binding.loginPasswordInput.hasFocus())
                    validatePassword();
            }
        });
        binding.loginForgotPasswordButton.setOnClickListener(this);
        binding.loginLogInButton.setOnClickListener(this);
        binding.loginGoogleButton.setOnClickListener(this);
        binding.loginSignUpButton.setOnClickListener(this);
    }


    private void logIn() {
        if (validateEmail() && validatePassword())
            Toast.makeText(getContext(), "Login successed", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getContext(), "Login failed", Toast.LENGTH_SHORT).show();
    }


    private boolean validatePassword() {
        String password = binding.loginPasswordInput.getText() == null ? "" : binding.loginPasswordInput.getText().toString();
        binding.loginPasswordLayout.setErrorEnabled(true);
        if (password.isEmpty()) {
            binding.loginPasswordLayout.setError("Field can't be empty");
            binding.loginPasswordInput.requestFocus();
            return false;
        } else
            binding.loginPasswordLayout.setErrorEnabled(false);
        return true;
    }


    private boolean validateEmail() {
        String email = binding.loginEmailInput.getText() == null ? "" : binding.loginEmailInput.getText().toString();
        binding.loginEmailLayout.setErrorEnabled(true);
        if (email.isEmpty()) {
            binding.loginEmailLayout.setError("Field can't be empty");
            binding.loginEmailInput.requestFocus();
            return false;
        } else if (!isValidEmail(email)) {
            binding.loginEmailLayout.setError("Invalid email");
            binding.loginEmailInput.requestFocus();
            return false;
        } else
            binding.loginEmailLayout.setErrorEnabled(false);
        return true;
    }


    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
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
                Toast.makeText(getContext(), "sign up", Toast.LENGTH_SHORT).show();
        }
    }


    private void changeFragment(Fragment next) {
        clearText();
        offWatcher();
        clearFocus();
        getNavigationInteractions().changeFragment(this, next, true);
    }


    @Override
    public void clearText() {
        binding.loginEmailInput.setText("");
        binding.loginPasswordInput.setText("");
    }

    @Override
    public void offWatcher() {
        binding.loginEmailLayout.setErrorEnabled(false);
        binding.loginPasswordLayout.setErrorEnabled(false);
    }

    @Override
    public void clearFocus() {
        binding.loginEmailInput.clearFocus();
        binding.loginPasswordInput.clearFocus();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
