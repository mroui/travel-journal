package com.martynaroj.traveljournal.Fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.martynaroj.traveljournal.Base.BaseFragment;
import com.martynaroj.traveljournal.Others.FormHandler;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.FragmentSignUpBinding;

public class SignUpFragment extends BaseFragment implements View.OnClickListener {

    private FragmentSignUpBinding binding;

    static SignUpFragment newInstance() {
        return new SignUpFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSignUpBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        setListeners();

        return view;
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
    }


    private boolean validateEmail() {
        return new FormHandler().validateInput(binding.signupEmailInput, binding.signupEmailLayout);
    }


    private boolean validateUsername() {
        return new FormHandler().validateInput(binding.signupUsernameInput, binding.signupUsernameLayout);
    }


    private boolean validatePasswords() {
        return new FormHandler().validateInput(binding.signupPasswordInput, binding.signupPasswordLayout)
            && new FormHandler().validateInput(binding.signupRepeatPasswordInput, binding.signupRepeatPasswordLayout)
            && new FormHandler().validateInputsEquality(binding.signupPasswordInput, binding.signupRepeatPasswordInput, binding.signupRepeatPasswordLayout);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.signup_arrow_button:
            case R.id.signup_log_in_button:
                if (getFragmentManager() != null && getFragmentManager().getBackStackEntryCount() > 0)
                    getFragmentManager().popBackStack();
                return;
            case R.id.signup_google_button:
                Toast.makeText(getContext(), "Google button", Toast.LENGTH_SHORT).show();
                return;
            case R.id.signup_sign_up_button:
                signUp();
        }
    }


    private void signUp() {
        if (validateUsername() && validateEmail() && validatePasswords())
            Toast.makeText(getContext(), "SignUp successed", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getContext(), "SignUp failed", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

}
