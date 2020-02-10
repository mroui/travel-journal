package com.martynaroj.traveljournal.view.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
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
import com.martynaroj.traveljournal.databinding.FragmentSignUpBinding;

import java.util.Objects;

public class SignUpFragment extends BaseFragment implements View.OnClickListener {

    private FragmentSignUpBinding binding;
    private AuthViewModel authViewModel;
    private GoogleClient googleClient;

    static SignUpFragment newInstance() {
        return new SignUpFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSignUpBinding.inflate(inflater, container, false);
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
        new FormHandler().addWatcher(binding.signupUsernameInput, binding.signupUsernameLayout);
        new FormHandler().addWatcher(binding.signupEmailInput, binding.signupEmailLayout);
        new FormHandler().addWatcher(binding.signupPasswordInput, binding.signupPasswordLayout);
        new FormHandler().addWatcher(binding.signupRepeatPasswordInput, binding.signupRepeatPasswordLayout);
        binding.signupArrowButton.setOnClickListener(this);
        binding.signupSignUpButton.setOnClickListener(this);
        binding.signupGoogleButton.setOnClickListener(this);
        binding.signupLogInButton.setOnClickListener(this);
        binding.signupPasswordStrengthMeter.setEditText(binding.signupPasswordInput);
    }


    private boolean validateEmail() {
        return new FormHandler().validateInput(binding.signupEmailInput, binding.signupEmailLayout);
    }


    private boolean validateUsername() {
        FormHandler formHandler = new FormHandler();
        TextInputEditText input = binding.signupUsernameInput;
        TextInputLayout layout = binding.signupUsernameLayout;
        int minLength = 4;

        return formHandler.validateInput(input, layout)
                && formHandler.validateLength(input, layout, minLength);
    }


    private boolean validatePasswords() {
        FormHandler formHandler = new FormHandler();
        TextInputEditText passInput = binding.signupPasswordInput;
        TextInputEditText repeatInput = binding.signupRepeatPasswordInput;
        TextInputLayout passLayout = binding.signupPasswordLayout;
        TextInputLayout repeatLayout = binding.signupRepeatPasswordLayout;
        int minLength = 8;

        return formHandler.validateInput(passInput, passLayout)
                && formHandler.validateInput(repeatInput, repeatLayout)
                && formHandler.validateLength(passInput, passLayout, minLength)
                && formHandler.validateInputsEquality(passInput, repeatInput, repeatLayout);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.signup_arrow_button:
            case R.id.signup_log_in_button:
                if (getParentFragmentManager().getBackStackEntryCount() > 0)
                    getParentFragmentManager().popBackStack();
                return;
            case R.id.signup_google_button:
                signUpWithGoogle();
                return;
            case R.id.signup_sign_up_button:
                signUpWithEmail();
        }
    }


    private void startProgressBar() {
        getProgressBarInteractions().startProgressBar(binding.getRoot(), binding.signupProgressbarLayout, binding.signupProgressbar);
    }


    private void stopProgressBar() {
        getProgressBarInteractions().startProgressBar(binding.getRoot(), binding.signupProgressbarLayout, binding.signupProgressbar);
    }


    private void showSnackBar(String message, int duration) {
        Snackbar snackbar = Snackbar.make(binding.getRoot(), message, duration);
        snackbar.setAnchorView(Objects.requireNonNull(getActivity()).findViewById(R.id.bottom_navigation_view));
        TextView textView = snackbar.getView().findViewById(R.id.snackbar_text);
        textView.setMaxLines(3);
        snackbar.show();
    }


    private void signUpWithEmail() {
        if (validateUsername() && validateEmail() && validatePasswords()) {
            startProgressBar();

            String email = Objects.requireNonNull(binding.signupEmailInput.getText()).toString();
            String username = Objects.requireNonNull(binding.signupUsernameInput.getText()).toString();
            String password = Objects.requireNonNull(binding.signupPasswordInput.getText()).toString();
            signUpWithEmailAuthCredential(email, password, username);
        }
    }


    private void signUpWithEmailAuthCredential(String email, String password, String username) {
        authViewModel.signUpWithEmail(email, password, username);
        authViewModel.getUserLiveData().observe(this, user -> {
            if (user.getStatus() == Status.LOADING) {
                sendVerificationMail();
            } else {
                stopProgressBar();
                showSnackBar(user.getMessage(), Snackbar.LENGTH_LONG);
            }
        });
    }


    private void sendVerificationMail() {
        authViewModel.sendVerificationMail();
        authViewModel.getUserVerificationLiveData().observe(this, verificationUser -> {
            stopProgressBar();
            if (verificationUser.getStatus() == Status.SUCCESS) {
                showSnackBar(verificationUser.getMessage(), Snackbar.LENGTH_LONG);
                getParentFragmentManager().popBackStack();
            } else {
                showSnackBar(verificationUser.getMessage(), Snackbar.LENGTH_LONG);
            }
        });
    }


    private void signUpWithGoogle() {
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
                if (!user.isAdded()) {
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


    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

}
