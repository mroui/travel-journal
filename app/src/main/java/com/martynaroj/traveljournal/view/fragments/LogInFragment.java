package com.martynaroj.traveljournal.view.fragments;


import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.Credentials;
import com.google.android.gms.auth.api.credentials.CredentialsClient;
import com.google.android.gms.auth.api.credentials.CredentialsOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.FragmentLogInBinding;
import com.martynaroj.traveljournal.services.models.DataWrapper;
import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.services.others.GoogleClient;
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.view.others.classes.FormHandler;
import com.martynaroj.traveljournal.view.others.enums.Status;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;
import com.martynaroj.traveljournal.viewmodels.AuthViewModel;
import com.martynaroj.traveljournal.viewmodels.UserViewModel;

import java.util.Objects;

public class LogInFragment extends BaseFragment implements View.OnClickListener {

    private FragmentLogInBinding binding;
    private AuthViewModel authViewModel;
    private UserViewModel userViewModel;
    private GoogleClient googleClient;
    private CredentialsClient credentialsClient;

    public static LogInFragment newInstance() {
        return new LogInFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLogInBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initViewModels();
        setListeners();
        initGoogleClient();
        initCredentialsClient();

        return view;
    }


    //INIT DATA-------------------------------------------------------------------------------------


    private void initCredentialsClient() {
        CredentialsOptions options = new CredentialsOptions.Builder()
                .forceEnableSaveDialog()
                .build();
        if (getActivity() != null)
            credentialsClient = Credentials.getClient(getActivity(), options);
    }


    private void initGoogleClient() {
        googleClient = new GoogleClient();
        googleClient.initGoogleSignInClient(binding.getRoot().getContext());
        ((TextView)binding.loginGoogleButton.getChildAt(0)).setText(getResources().getString(R.string.google_sign_in));
    }


    private void initViewModels() {
        if (getActivity() != null) {
            authViewModel = new ViewModelProvider(getActivity()).get(AuthViewModel.class);
            userViewModel = new ViewModelProvider(getActivity()).get(UserViewModel.class);
        }
    }


    //LISTENERS-------------------------------------------------------------------------------------


    private void setListeners() {
        new FormHandler(getContext()).addWatcher(binding.loginEmailInput, binding.loginEmailLayout);
        new FormHandler(getContext()).addWatcher(binding.loginPasswordInput, binding.loginPasswordLayout);
        binding.loginForgotPasswordButton.setOnClickListener(this);
        binding.loginLogInButton.setOnClickListener(this);
        binding.loginGoogleButton.setOnClickListener(this);
        binding.loginSignUpButton.setOnClickListener(this);
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


    //VALIDATION------------------------------------------------------------------------------------


    private boolean validateEmail() {
        return new FormHandler(getContext()).validateInput(binding.loginEmailInput, binding.loginEmailLayout);
    }


    private boolean validatePassword() {
        return new FormHandler(getContext()).validateInput(binding.loginPasswordInput, binding.loginPasswordLayout);
    }


    //CREDENTIALS-----------------------------------------------------------------------------------


    private void logInWithEmail() {
        if (validateEmail() && validatePassword()) {
            startProgressBar();

            String email = Objects.requireNonNull(binding.loginEmailInput.getText()).toString();
            String password = Objects.requireNonNull(binding.loginPasswordInput.getText()).toString();

            Credential credential = new Credential.Builder(email).setPassword(password).build();
            saveCredentials(credential);

            authViewModel.logInWithEmail(email, password);
            authViewModel.getUserLiveData().observe(this, userData -> {
                if (userData.getStatus() == Status.SUCCESS) {
                    if (userData.isVerified() && !userData.isAdded())
                        addNewUser(userData);
                    else if (!userData.isVerified()) {
                        showSnackBar(getResources().getString(R.string.messages_error_no_verified), Snackbar.LENGTH_LONG);
                        resendVerificationMail();
                    } else
                        getUserData(userData);
                } else
                    showSnackBar(userData.getMessage(), Snackbar.LENGTH_LONG);
                stopProgressBar();
            });
        }
    }


    private void getUserData(DataWrapper<User> userData) {
        startProgressBar();
        userViewModel.getUserData(userData.getData().getUid());
        userViewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
            stopProgressBar();
            showSnackBar(userData.getMessage(), Snackbar.LENGTH_SHORT);
            userViewModel.setUser(user);
            getNavigationInteractions().changeNavigationBarItem(2, ProfileFragment.newInstance(user));
        });
    }


    private void saveCredentials(Credential credential) {
        if (credentialsClient != null) {
            credentialsClient.save(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful())
                    return;
                Exception e = task.getException();
                if (e instanceof ResolvableApiException) {
                    ResolvableApiException rae = (ResolvableApiException) e;
                    try {
                        rae.startResolutionForResult(getActivity(), Constants.RC_SAVE_CREDENTIALS);
                    } catch (IntentSender.SendIntentException ignored) {
                    }
                }
            });
        }
    }


    private void resendVerificationMail() {
        startProgressBar();
        authViewModel.sendVerificationMail();
        authViewModel.getUserVerificationLiveData().observe(this, verificationUser -> {
            if (verificationUser.getStatus() == Status.SUCCESS)
                showSnackBar(getResources().getString(R.string.messages_verification_sent),
                        Snackbar.LENGTH_LONG);
            else
                showSnackBar(verificationUser.getMessage(), Snackbar.LENGTH_LONG);
            stopProgressBar();
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
        String status = googleClient.getGoogleSignInAccount(requestCode, data);
        if (status.equals(Constants.SUCCESS))
            getGoogleAuthCredential(googleClient.getGoogleSignInAccount());
        else
            showSnackBar(status, Snackbar.LENGTH_LONG);
        stopProgressBar();
    }


    private void getGoogleAuthCredential(GoogleSignInAccount googleSignInAccount) {
        AuthCredential googleAuthCredential = googleClient.getGoogleAuthCredential(googleSignInAccount);
        signInWithGoogleAuthCredential(googleAuthCredential);
    }


    private void signInWithGoogleAuthCredential(AuthCredential googleAuthCredential) {
        startProgressBar();
        authViewModel.signInWithGoogle(googleAuthCredential);
        authViewModel.getUserLiveData().observe(this, userData -> {
            if (userData.getStatus() == Status.SUCCESS)
                if (!userData.isAdded())
                    addNewUser(userData);
                else
                    getUserData(userData);
            else
                showSnackBar(userData.getMessage(), Snackbar.LENGTH_LONG);
            stopProgressBar();
        });
    }


    private void addNewUser(DataWrapper<User> user) {
        startProgressBar();
        authViewModel.addUser(user);
        authViewModel.getAddedUserLiveData().observe(this, newUser -> {
            if (newUser.getStatus() == Status.SUCCESS && newUser.isAdded())
                getUserData(newUser);
            else
                showSnackBar(newUser.getMessage(), Snackbar.LENGTH_LONG);
            stopProgressBar();
        });
    }


    //OTHERS----------------------------------------------------------------------------------------


    private void changeFragment(Fragment next) {
        clearInputs();
        getNavigationInteractions().changeFragment(this, next, true);
    }


    private void clearInputs() {
        new FormHandler(getContext()).clearInput(binding.loginEmailInput, binding.loginEmailLayout);
        new FormHandler(getContext()).clearInput(binding.loginPasswordInput, binding.loginPasswordLayout);
    }


    private void startProgressBar() {
        getProgressBarInteractions().startProgressBar(binding.getRoot(),
                binding.loginProgressbarLayout, binding.loginProgressbar);
    }


    private void stopProgressBar() {
        getProgressBarInteractions().stopProgressBar(binding.getRoot(),
                binding.loginProgressbarLayout, binding.loginProgressbar);
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