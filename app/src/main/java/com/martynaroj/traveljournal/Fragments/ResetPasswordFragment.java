package com.martynaroj.traveljournal.Fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.martynaroj.traveljournal.Base.BaseFragment;
import com.martynaroj.traveljournal.Others.FormHandler;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.FragmentResetPasswordBinding;

public class ResetPasswordFragment extends BaseFragment implements View.OnClickListener {

    private FragmentResetPasswordBinding binding;

    static ResetPasswordFragment newInstance() {
        return new ResetPasswordFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentResetPasswordBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        setListeners();

        return view;
    }

    private void setListeners() {
        new FormHandler().addWatcher(binding.forgotPasswordEmailInput, binding.forgotPasswordEmailLayout);
        binding.forgotPasswordArrowButton.setOnClickListener(this);
        binding.forgotPasswordBackButton.setOnClickListener(this);
        binding.forgotPasswordSendButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.forgot_password_arrow_button:
            case R.id.forgot_password_back_button:
                if (getFragmentManager() != null && getFragmentManager().getBackStackEntryCount() > 0)
                    getFragmentManager().popBackStack();
                return;
            case R.id.forgot_password_send_button:
                if (validateEmail())
                    resetPassword();
        }
    }

    private boolean validateEmail() {
        return new FormHandler().validateInput(binding.forgotPasswordEmailInput, binding.forgotPasswordEmailLayout);
    }

    private void resetPassword() {
//        FirebaseAuth.getInstance().sendPasswordResetEmail(binding.forgotPasswordEmailInput.getText().toString())
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        if (task.isSuccessful()) {
//                            Log.d(TAG, "Email sent.");
//                            hideProgressDialog();
//                            Toast.makeText(ForgotPasswordActivity.this, getString(R.string.alert_passwordResetConfirm),
//                                    Toast.LENGTH_SHORT).show();
//                        } else {
//                            Exception e = task.getException();
//                            Log.w(TAG, "passwordResetRequest:failure " + e.getMessage(), task.getException());
//                            hideProgressDialog();
//                            Toast.makeText(ForgotPasswordActivity.this, e.getMessage(),
//                                    Toast.LENGTH_SHORT).show();
//                       }
//
//                    }
//                });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
