package com.martynaroj.traveljournal.Fragments;


import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.martynaroj.traveljournal.Base.BaseFragment;
import com.martynaroj.traveljournal.Others.InputTextWatcher;
import com.martynaroj.traveljournal.R;
import com.shobhitpuri.custombuttons.GoogleSignInButton;

public class ProfileFragment extends BaseFragment {

    private TextInputEditText inputEmail;
    private TextInputEditText inputPassword;
    private TextInputLayout layoutInputEmail;
    private TextInputLayout layoutInputPassword;
    private TextView buttonForgotPassword;
    private MaterialButton buttonLogIn;
    private GoogleSignInButton buttonGoogleSignIn;
    private TextView buttonSignUp;


    public ProfileFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        findViews(view);
        setListeners();

        return view;
    }


    private void setListeners() {
        inputEmail.addTextChangedListener(new InputTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                validateEmail();
            }
        });
        inputPassword.addTextChangedListener(new InputTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                validatePassword();
            }
        });
        buttonLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logIn();
            }
        });
    }


    private void logIn() {
        if (validateEmail() && validatePassword())
            Toast.makeText(getContext(), "Login successed", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getContext(), "Login failed", Toast.LENGTH_SHORT).show();
    }


    private void findViews(View view) {
        inputEmail = view.findViewById(R.id.login_email_input);
        inputPassword = view.findViewById(R.id.login_password_input);
        layoutInputEmail = view.findViewById(R.id.login_email_layout);
        layoutInputPassword = view.findViewById(R.id.login_password_layout);
        buttonForgotPassword = view.findViewById(R.id.login_forgot_password);
        buttonLogIn = view.findViewById(R.id.login_log_in_button);
        buttonGoogleSignIn = view.findViewById(R.id.login_google_button);
        buttonSignUp = view.findViewById(R.id.login_sign_up_button);
    }


    private boolean validatePassword() {

        String password = inputPassword.getText() == null ? "" : inputPassword.getText().toString();
        layoutInputPassword.setErrorEnabled(true);
        if (password.isEmpty()) {
            layoutInputPassword.setError("Field can't be empty");
            inputPassword.requestFocus();
            return false;
        } else if (password.length() < 8) {
            layoutInputPassword.setError("Password is too short");
            inputPassword.requestFocus();
            return false;
        } else
            layoutInputPassword.setErrorEnabled(false);
        return true;
    }


    private boolean validateEmail() {
        String email = inputEmail.getText() == null ? "" : inputEmail.getText().toString();
        layoutInputEmail.setErrorEnabled(true);
        if (email.isEmpty()) {
            layoutInputEmail.setError("Field can't be empty");
            inputEmail.requestFocus();
            return false;
        } else if (!isValidEmail(email)) {
            layoutInputEmail.setError("Invalid email");
            inputEmail.requestFocus();
            return false;
        } else
            layoutInputEmail.setErrorEnabled(false);
        return true;
    }


    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
