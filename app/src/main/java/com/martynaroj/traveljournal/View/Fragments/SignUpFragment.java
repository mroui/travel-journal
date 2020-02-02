package com.martynaroj.traveljournal.View.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.Services.Models.DataWrapper;
import com.martynaroj.traveljournal.Services.Models.User;
import com.martynaroj.traveljournal.View.Base.BaseFragment;
import com.martynaroj.traveljournal.View.Others.Classes.FormHandler;
import com.martynaroj.traveljournal.View.Others.Enums.Status;
import com.martynaroj.traveljournal.View.Others.Interfaces.Constants;
import com.martynaroj.traveljournal.ViewModels.AuthViewModel;
import com.martynaroj.traveljournal.databinding.FragmentSignUpBinding;

import java.util.Objects;

public class SignUpFragment extends BaseFragment implements View.OnClickListener {

    private FragmentSignUpBinding binding;
    private AuthViewModel authViewModel;
    private GoogleSignInClient googleSignInClient;

    static SignUpFragment newInstance() {
        return new SignUpFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSignUpBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initAuthViewModel();
        setListeners();
        initGoogleSignInClient();

        return view;
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


    @SuppressWarnings("ConstantConditions")
    private void initGoogleSignInClient() {
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(getContext(), googleSignInOptions);
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
        binding.signupProgressbarLayout.setVisibility(View.VISIBLE);
        binding.signupProgressbar.start();
        enableDisableViewGroup((ViewGroup) binding.getRoot(), false);
    }


    private void stopProgressBar() {
        binding.signupProgressbarLayout.setVisibility(View.INVISIBLE);
        binding.signupProgressbar.stop();
        enableDisableViewGroup((ViewGroup) binding.getRoot(), true);
    }


    private void showSnackBar(String message, int duration) {
        Snackbar snackbar = Snackbar.make(binding.getRoot(), message, duration);
        snackbar.setAnchorView(Objects.requireNonNull(getActivity()).findViewById(R.id.bottom_navigation_view));
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
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, Constants.RC_SIGN_IN);
        startProgressBar();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount googleSignInAccount = task.getResult(ApiException.class);
                if (googleSignInAccount != null)
                    getGoogleAuthCredential(googleSignInAccount);
            } catch (ApiException e) {
                String statusCode = CommonStatusCodes.getStatusCodeString(e.getStatusCode());
                String message = "Error: Internal API error";
                if (statusCode.equals("NETWORK_ERROR"))
                    message = "Error: Please check your network connection";
                else if (statusCode.equals("TIMEOUT"))
                    message = "Error: Timed out while awaiting the result";

                showSnackBar(message, Snackbar.LENGTH_LONG);
                stopProgressBar();
            }
        } else {
            showSnackBar("Error: Activity request error", Snackbar.LENGTH_LONG);
            stopProgressBar();
        }
    }


    private void getGoogleAuthCredential(GoogleSignInAccount googleSignInAccount) {
        String googleTokenId = googleSignInAccount.getIdToken();
        AuthCredential googleAuthCredential = GoogleAuthProvider.getCredential(googleTokenId, null);
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


    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

}
