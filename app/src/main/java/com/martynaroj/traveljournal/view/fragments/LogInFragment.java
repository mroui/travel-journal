package com.martynaroj.traveljournal.view.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.services.models.DataWrapper;
import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.services.others.GoogleClient;
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.view.others.classes.FormHandler;
import com.martynaroj.traveljournal.view.others.enums.Status;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;
import com.martynaroj.traveljournal.viewmodels.AuthViewModel;
import com.martynaroj.traveljournal.databinding.FragmentLogInBinding;

import java.util.Objects;

public class LogInFragment extends BaseFragment implements View.OnClickListener {

    private FragmentLogInBinding binding;
    private AuthViewModel authViewModel;
    private GoogleClient googleClient;

    public static LogInFragment newInstance() {
        return new LogInFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLogInBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initAuthViewModel();
        setListeners();
        initGoogleClient();

        return view;
    }


    private void initGoogleClient() {
        googleClient = new GoogleClient();
        googleClient.initGoogleSignInClient(binding.getRoot().getContext());
    }


    private void initAuthViewModel() {
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
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


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_log_in_button:
                logInWithEmail();
                return;
            case R.id.login_google_button:
                logInWithGoogle();
                return;
            case R.id.login_forgot_password_button:
                changeFragment(ResetPasswordFragment.newInstance());
                return;
            case R.id.login_sign_up_button:
                changeFragment(SignUpFragment.newInstance());
        }
    }


    private void logInWithEmail() {
        if (validateEmail() && validatePassword()) {
            startProgressBar();

            String email = Objects.requireNonNull(binding.loginEmailInput.getText()).toString();
            String password = Objects.requireNonNull(binding.loginPasswordInput.getText()).toString();

            authViewModel.logInWithEmail(email, password);
            authViewModel.getUserLiveData().observe(this, user -> {
                if (user.getStatus() == Status.SUCCESS) {
                    if (user.isVerified() && user.isNew()) {
                        addNewUser(user);
                    } else if (!user.isVerified()) {
                        showSnackBar("Error: Account is not verified", Snackbar.LENGTH_SHORT);
                        resendVerificationMail();
                    } else if (!user.isNew()){
                        stopProgressBar();
                        showSnackBar(user.getMessage(), Snackbar.LENGTH_SHORT);
                        getNavigationInteractions().changeNavigationBarItem(2, ProfileFragment.newInstance());
                    }
                } else {
                    stopProgressBar();
                    showSnackBar(user.getMessage(), Snackbar.LENGTH_LONG);
                }
            });
        }
    }


    private void resendVerificationMail() {
        authViewModel.sendVerificationMail();
        authViewModel.getUserVerificationLiveData().observe(this, verificationUser -> {
            stopProgressBar();
            if (verificationUser.getStatus() == Status.SUCCESS) {
                stopProgressBar();
                showSnackBar("Verification email has been sent. Check your email to verify account",
                        Snackbar.LENGTH_LONG);
            } else {
                showSnackBar(verificationUser.getMessage(), Snackbar.LENGTH_LONG);
            }
        });
    }


    private void logInWithGoogle() {
        Intent signInIntent = googleClient.getGoogleSignInClient().getSignInIntent();
        startActivityForResult(signInIntent, Constants.RC_SIGN_IN);
        startProgressBar();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String status = googleClient.getGoogleSignInAccount(requestCode, resultCode, data);
        if (status.equals(Constants.SUCCESS)) {
            getGoogleAuthCredential(googleClient.getGoogleSignInAccount());
        } else {
            showSnackBar(status, Snackbar.LENGTH_LONG);
            stopProgressBar();
        }
    }


    private void getGoogleAuthCredential(GoogleSignInAccount googleSignInAccount) {
        AuthCredential googleAuthCredential = googleClient.getGoogleAuthCredential(googleSignInAccount);
        signInWithGoogleAuthCredential(googleAuthCredential);
    }


    private void signInWithGoogleAuthCredential(AuthCredential googleAuthCredential) {
        authViewModel.signInWithGoogle(googleAuthCredential);
        authViewModel.getUserLiveData().observe(this, user -> {
            if (user.getStatus() == Status.SUCCESS) {
                if (user.isNew()) {
                    addNewUser(user);
                } else {
                    stopProgressBar();
                    showSnackBar(user.getMessage(), Snackbar.LENGTH_SHORT);
                    getNavigationInteractions().changeNavigationBarItem(2, ProfileFragment.newInstance());
                }
            } else {
                stopProgressBar();
                showSnackBar(user.getMessage(), Snackbar.LENGTH_LONG);
            }
        });
    }


    private void addNewUser(DataWrapper<User> user) {
        authViewModel.addUser(user);
        authViewModel.getAddedUserLiveData().observe(this, newUser -> {
            if (newUser.getStatus() == Status.SUCCESS && newUser.isAdded()) {
                stopProgressBar();
                showSnackBar(newUser.getMessage(), Snackbar.LENGTH_SHORT);
                getNavigationInteractions().changeNavigationBarItem(2, ProfileFragment.newInstance());
            } else {
                stopProgressBar();
                showSnackBar(newUser.getMessage(), Snackbar.LENGTH_LONG);
            }
        });
    }


    private void changeFragment(Fragment next) {
        clearInputs();
        getNavigationInteractions().changeFragment(this, next, true);
    }


    private void clearInputs() {
        new FormHandler().clearInput(binding.loginEmailInput, binding.loginEmailLayout);
        new FormHandler().clearInput(binding.loginPasswordInput, binding.loginPasswordLayout);
    }


    private void startProgressBar() {
        binding.loginProgressbarLayout.setVisibility(View.VISIBLE);
        binding.loginProgressbar.start();
        enableDisableViewGroup((ViewGroup) binding.getRoot(), false);
    }


    private void stopProgressBar() {
        binding.loginProgressbarLayout.setVisibility(View.INVISIBLE);
        binding.loginProgressbar.stop();
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