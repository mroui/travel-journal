package com.martynaroj.traveljournal.View.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
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
import com.martynaroj.traveljournal.databinding.FragmentLogInBinding;

import java.util.Objects;

public class LogInFragment extends BaseFragment implements View.OnClickListener {

    private FragmentLogInBinding binding;
    private AuthViewModel authViewModel;
    private GoogleSignInClient googleSignInClient;

    public static LogInFragment newInstance() {
        return new LogInFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLogInBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initAuthViewModel();
        setListeners();
        initGoogleSignInClient();

        return view;
    }


    private void initAuthViewModel() {
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
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


    private void logInWithEmail() {
        if (validateEmail() && validatePassword())
            Toast.makeText(getContext(), "Login successed", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getContext(), "Login failed", Toast.LENGTH_SHORT).show();
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


    private void logInWithGoogle() {
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