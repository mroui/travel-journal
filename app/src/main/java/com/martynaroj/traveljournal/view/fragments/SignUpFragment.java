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
import com.martynaroj.traveljournal.databinding.FragmentSignUpBinding;
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

public class SignUpFragment extends BaseFragment implements View.OnClickListener {

    private FragmentSignUpBinding binding;
    private AuthViewModel authViewModel;
    private UserViewModel userViewModel;
    private GoogleClient googleClient;

    static SignUpFragment newInstance() {
        return new SignUpFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSignUpBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initViewModels();
        setListeners();
        initGoogleClient();

        return view;
    }


    //INIT DATA-------------------------------------------------------------------------------------


    private void initGoogleClient() {
        googleClient = new GoogleClient();
        googleClient.initGoogleSignInClient(binding.getRoot().getContext());
        ((TextView)binding.signupGoogleButton.getChildAt(0)).setText(getResources().getString(R.string.google_sign_up));
    }


    private void initViewModels() {
        if (getActivity() != null) {
            authViewModel = new ViewModelProvider(getActivity()).get(AuthViewModel.class);
            userViewModel = new ViewModelProvider(getActivity()).get(UserViewModel.class);
        }
    }


    //LISTENERS-------------------------------------------------------------------------------------


    private void setListeners() {
        new FormHandler(getContext()).addWatcher(binding.signupUsernameInput, binding.signupUsernameLayout);
        new FormHandler(getContext()).addWatcher(binding.signupEmailInput, binding.signupEmailLayout);
        new FormHandler(getContext()).addWatcher(binding.signupPasswordInput, binding.signupPasswordLayout);
        new FormHandler(getContext()).addWatcher(binding.signupRepeatPasswordInput, binding.signupRepeatPasswordLayout);
        binding.signupArrowButton.setOnClickListener(this);
        binding.signupSignUpButton.setOnClickListener(this);
        binding.signupGoogleButton.setOnClickListener(this);
        binding.signupLogInButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.signup_arrow_button:
            case R.id.signup_log_in_button:
                hideKeyboard();
                back();
                break;
            case R.id.signup_google_button:
                signUpWithGoogle();
                break;
            case R.id.signup_sign_up_button:
                signUpWithEmail();
                break;
        }
    }


    //VALIDATION------------------------------------------------------------------------------------


    private boolean validateEmail() {
        return new FormHandler(getContext()).validateInput(binding.signupEmailInput, binding.signupEmailLayout);
    }


    private boolean validateUsername() {
        FormHandler formHandler = new FormHandler(getContext());
        TextInputEditText input = binding.signupUsernameInput;
        TextInputLayout layout = binding.signupUsernameLayout;
        int minLength = 4;

        return formHandler.validateInput(input, layout)
                && formHandler.validateLength(input, layout, minLength);
    }


    private boolean validatePasswords() {
        FormHandler formHandler = new FormHandler(getContext());
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


    //CREDENTIALS-----------------------------------------------------------------------------------


    private void signUpWithEmail() {
        if (validateUsername() && validateEmail() && validatePasswords()) {
            String email = Objects.requireNonNull(binding.signupEmailInput.getText()).toString();
            String username = Objects.requireNonNull(binding.signupUsernameInput.getText()).toString();
            String password = Objects.requireNonNull(binding.signupPasswordInput.getText()).toString();
            signUpWithEmailAuthCredential(email, password, username);
        }
    }


    private void signUpWithEmailAuthCredential(String email, String password, String username) {
        startProgressBar();
        authViewModel.signUpWithEmail(email, password, username);
        authViewModel.getUserLiveData().observe(this, user -> {
            if (user.getStatus() == Status.LOADING)
                sendVerificationMail();
            else
                showSnackBar(user.getMessage(), Snackbar.LENGTH_LONG);
            stopProgressBar();
        });
    }


    private void signUpWithGoogle() {
        Intent signInIntent = googleClient.getGoogleSignInClient().getSignInIntent();
        startActivityForResult(signInIntent, Constants.RC_SIGN_IN);
        startProgressBar();
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


    private void sendVerificationMail() {
        startProgressBar();
        authViewModel.sendVerificationMail();
        authViewModel.getUserVerificationLiveData().observe(this, verificationUser -> {
            stopProgressBar();
            showSnackBar(verificationUser.getMessage(), Snackbar.LENGTH_LONG);
            if (verificationUser.getStatus() == Status.SUCCESS)
                back();
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String status = googleClient.getGoogleSignInAccount(requestCode, data);
        if (status.equals(Constants.SUCCESS)) {
            getGoogleAuthCredential(googleClient.getGoogleSignInAccount());
        } else {
            showSnackBar(status, Snackbar.LENGTH_LONG);
            stopProgressBar();
        }
    }


    private void getUserData(DataWrapper<User> userData) {
        startProgressBar();
        userViewModel.getUserData(userData.getData().getUid());
        userViewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
            stopProgressBar();
            showSnackBar(userData.getMessage(), Snackbar.LENGTH_SHORT);
            getNavigationInteractions().changeNavigationBarItem(2, ProfileFragment.newInstance(user));
        });
    }


    private void addNewUser(DataWrapper<User> userData) {
        startProgressBar();
        authViewModel.addUser(userData);
        authViewModel.getAddedUserLiveData().observe(this, newUser -> {
            if (newUser.getStatus() == Status.SUCCESS && newUser.isAdded())
                getUserData(userData);
            else
                showSnackBar(newUser.getMessage(), Snackbar.LENGTH_LONG);
            stopProgressBar();
        });
    }


    //OTHERS----------------------------------------------------------------------------------------


    private void back() {
        if (getParentFragmentManager().getBackStackEntryCount() > 0)
            getParentFragmentManager().popBackStack();
    }


    private void startProgressBar() {
        getProgressBarInteractions().startProgressBar(binding.getRoot(),
                binding.signupProgressbarLayout, binding.signupProgressbar);
    }


    private void stopProgressBar() {
        getProgressBarInteractions().stopProgressBar(binding.getRoot(),
                binding.signupProgressbarLayout, binding.signupProgressbar);
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
