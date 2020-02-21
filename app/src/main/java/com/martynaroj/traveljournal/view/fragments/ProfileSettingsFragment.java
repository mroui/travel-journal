package com.martynaroj.traveljournal.view.fragments;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.FragmentProfileSettingsBinding;
import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.view.adapters.HashtagAdapter;
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.view.interfaces.IOnBackPressed;
import com.martynaroj.traveljournal.view.others.classes.FormHandler;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;
import com.martynaroj.traveljournal.viewmodels.AuthViewModel;
import com.martynaroj.traveljournal.viewmodels.StorageViewModel;
import com.martynaroj.traveljournal.viewmodels.UserViewModel;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import id.zelory.compressor.Compressor;

import static android.app.Activity.RESULT_OK;

public class ProfileSettingsFragment extends BaseFragment implements View.OnClickListener, IOnBackPressed {

    private FragmentProfileSettingsBinding binding;
    private UserViewModel userViewModel;
    private User user;
    private AuthViewModel authViewModel;

    private Uri newImageUri;
    private Bitmap compressor;
    private StorageViewModel storageViewModel;

    static ProfileSettingsFragment newInstance() {
        return new ProfileSettingsFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileSettingsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initContentView();
        setListeners();
        initViewModels();

        initUser();

        return view;
    }


    //INIT DATA-------------------------------------------------------------------------------------


    private void initContentView() {
        if (getContext() != null) {
            binding.profileSettingsPrivacyEmailSelect.setItems(Constants.PUBLIC, Constants.FRIENDS, Constants.ONLY_ME);
            binding.profileSettingsPrivacyLocationSelect.setItems(Constants.PUBLIC, Constants.FRIENDS, Constants.ONLY_ME);
            binding.profileSettingsPrivacyPreferencesSelect.setItems(Constants.PUBLIC, Constants.FRIENDS, Constants.ONLY_ME);

            List<String> preferences = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.preferences)));
            final HashtagAdapter adapter = new HashtagAdapter(getContext(), preferences);
            binding.profileSettingsPersonalPreferencesInput.setAdapter(adapter);
            binding.profileSettingsPersonalPreferencesInput.setThreshold(1);
        }
    }


    private void setListeners() {
        new FormHandler().addWatcher(binding.profileSettingsAccountPasswordCurrentInput, binding.profileSettingsAccountPasswordCurrentLayout);
        new FormHandler().addWatcher(binding.profileSettingsAccountPasswordInput, binding.profileSettingsAccountPasswordLayout);
        new FormHandler().addWatcher(binding.profileSettingsAccountPasswordConfirmInput, binding.profileSettingsAccountPasswordConfirmLayout);
        new FormHandler().addWatcher(binding.profileSettingsAccountEmailPasswordCurrentInput, binding.profileSettingsAccountEmailPasswordCurrentLayout);
        new FormHandler().addWatcher(binding.profileSettingsAccountEmailInput, binding.profileSettingsAccountEmailLayout);
        new FormHandler().addWatcher(binding.profileSettingsAccountEmailConfirmInput, binding.profileSettingsAccountEmailConfirmLayout);
        new FormHandler().addWatcher(binding.profileSettingsAccountUsernameInput, binding.profileSettingsAccountUsernameLayout);
        binding.profileSettingsAccountPasswordStrengthMeter.setEditText(binding.profileSettingsAccountPasswordInput);
        binding.profileSettingsArrowButton.setOnClickListener(this);
        binding.profileSettingsPersonalPictureSection.setOnClickListener(this);
        binding.profileSettingsPersonalLocationButton.setOnClickListener(this);
        binding.profileSettingsPersonalSaveButton.setOnClickListener(this);
        binding.profileSettingsAccountUsernameSaveButton.setOnClickListener(this);
        binding.profileSettingsAccountEmailSaveButton.setOnClickListener(this);
        binding.profileSettingsAccountPasswordSaveButton.setOnClickListener(this);
        binding.profileSettingsPrivacySaveButton.setOnClickListener(this);
        binding.profileSettingsAboutCreditsSection.setOnClickListener(this);
    }


    private void initViewModels() {
        if (getActivity() != null) {
            userViewModel = new ViewModelProvider(getActivity()).get(UserViewModel.class);
            storageViewModel = new ViewModelProvider(getActivity()).get(StorageViewModel.class);
            authViewModel = new ViewModelProvider(getActivity()).get(AuthViewModel.class);
        }
    }


    private void initUser() {
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            user = (User) bundle.getSerializable(Constants.USER);
            binding.setUser(user);
            initUserData();
        } else {
            getCurrentUser();
        }
    }


    private void getCurrentUser() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            startProgressBar();
            userViewModel.getDataSnapshotLiveData(firebaseAuth.getCurrentUser().getUid());
            userViewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
                if (user != null) {
                    this.user = user;
                    binding.setUser(user);
                    initUserData();
                } else {
                    this.user = new User();
                    showSnackBar("ERROR: No such User in a database, try again later", Snackbar.LENGTH_LONG);
                }
                stopProgressBar();
            });
        } else {
            showSnackBar("ERROR: Current user is not available, try again later", Snackbar.LENGTH_LONG);
        }
    }


    private void initUserData() {
        int index = Objects.requireNonNull(user.getPrivacy().get(Constants.EMAIL));
        binding.profileSettingsPrivacyEmailSelect.setSelectedIndex(index);

        index = Objects.requireNonNull(user.getPrivacy().get(Constants.LOCATION));
        binding.profileSettingsPrivacyLocationSelect.setSelectedIndex(index);

        index = Objects.requireNonNull(user.getPrivacy().get(Constants.PREFERENCES));
        binding.profileSettingsPrivacyPreferencesSelect.setSelectedIndex(index);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.profile_settings_arrow_button:
                if (!areAnyChanges()) {
                    hideKeyboard();
                    if (getParentFragmentManager().getBackStackEntryCount() > 0)
                        getParentFragmentManager().popBackStack();
                } else showUnsavedChangesDialog();
                return;
            case R.id.profile_settings_personal_picture_section:
                changeProfilePhoto();
                return;
            case R.id.profile_settings_personal_location_button:
                showSnackBar("clicked: find me", Snackbar.LENGTH_SHORT);
                return;
            case R.id.profile_settings_personal_save_button:
                savePersonalChanges();
                return;
            case R.id.profile_settings_account_username_save_button:
                changeUsername();
                return;
            case R.id.profile_settings_account_email_save_button:
                changeEmail();
                return;
            case R.id.profile_settings_account_password_save_button:
                changePassword();
                return;
            case R.id.profile_settings_privacy_save_button:
                savePrivacyChanges();
                return;
            case R.id.profile_settings_about_credits_section:
                showCreditsDialog();
        }
    }


    //DIALOGS---------------------------------------------------------------------------------------


    private void showUnsavedChangesDialog() {
        if (getContext() != null && getActivity() != null) {
            final AlertDialog dialog = new MaterialAlertDialogBuilder(getContext())
                    .setTitle(getString(R.string.dialog_button_unsaved_changes_title))
                    .setMessage(getString(R.string.dialog_button_unsaved_changes_desc))
                    .setPositiveButton(getString(R.string.dialog_button_yes), (dialogInterface, i) -> {
                        hideKeyboard();
                        dialogInterface.cancel();
                        if (getParentFragmentManager().getBackStackEntryCount() > 0)
                            getParentFragmentManager().popBackStack();
                    })
                    .setNegativeButton(getString(R.string.dialog_button_no), null)
                    .show();
            ((TextView) Objects.requireNonNull(dialog.findViewById(android.R.id.message))).setMovementMethod(LinkMovementMethod.getInstance());
        }
    }


    private void showCreditsDialog() {
        if (getContext() != null) {
            final AlertDialog dialog = new MaterialAlertDialogBuilder(getContext())
                    .setTitle(getResources().getString(R.string.profile_settings_about_credits_title))
                    .setMessage(Html.fromHtml(getResources().getString(R.string.profile_settings_credits_list)))
                    .setPositiveButton(getString(R.string.dialog_button_ok), null)
                    .show();
            ((TextView) Objects.requireNonNull(dialog.findViewById(android.R.id.message))).setMovementMethod(LinkMovementMethod.getInstance());
        }
    }


    //CHECKING CHANGES------------------------------------------------------------------------------


    private boolean isImageChanged() {
        return newImageUri != null;
    }


    private boolean isUsernameChanged() {
        return !user.getUsername().equals(Objects.requireNonNull(binding.profileSettingsAccountUsernameInput.getText()).toString());
    }


    private boolean isBioChanged() {
        if (binding.profileSettingsPersonalBioInput.getText() != null) {
            return (user.getBio() == null && !binding.profileSettingsPersonalBioInput.getText().toString().equals(""))
                    || (user.getBio() != null && !user.getBio().equals(binding.profileSettingsPersonalBioInput.getText().toString()));
        }
        return false;
    }


    private boolean isPreferenceChanged() {
        if (binding.profileSettingsPersonalPreferencesInput.getText() != null) {
            return (user.getPreferences() == null && !binding.profileSettingsPersonalPreferencesInput.getText().toString().equals(""))
                    || (user.getPreferences() != null && !user.getPreferences().equals(getUniquePreferences()));
        }
        return false;
    }


    private boolean isEmailChanged() {
        if (binding.profileSettingsAccountEmailPasswordCurrentInput.getText() != null
                && binding.profileSettingsAccountEmailInput.getText() != null
                && binding.profileSettingsAccountEmailConfirmInput.getText() != null) {
            return !binding.profileSettingsAccountEmailPasswordCurrentInput.getText().toString().equals("")
                    || !binding.profileSettingsAccountEmailInput.getText().toString().equals("")
                    || !binding.profileSettingsAccountEmailConfirmInput.getText().toString().equals("");
        }
        return false;
    }


    private boolean isPasswordChanged() {
        if (binding.profileSettingsAccountPasswordCurrentInput.getText() != null
                && binding.profileSettingsAccountPasswordInput.getText() != null
                && binding.profileSettingsAccountPasswordConfirmInput.getText() != null) {
            return !binding.profileSettingsAccountPasswordCurrentInput.getText().toString().equals("")
                    || !binding.profileSettingsAccountPasswordInput.getText().toString().equals("")
                    || !binding.profileSettingsAccountPasswordConfirmInput.getText().toString().equals("");
        }
        return false;
    }


    @SuppressWarnings("ConstantConditions")
    private boolean isPrivacyEmailChanged() {
        return user.getPrivacy().get(Constants.EMAIL) != binding.profileSettingsPrivacyEmailSelect.getSelectedIndex();
    }


    @SuppressWarnings("ConstantConditions")
    private boolean isPrivacyLocationChanged() {
        return user.getPrivacy().get(Constants.LOCATION) != binding.profileSettingsPrivacyLocationSelect.getSelectedIndex();
    }


    @SuppressWarnings("ConstantConditions")
    private boolean isPrivacyPreferencesChanged() {
        return user.getPrivacy().get(Constants.PREFERENCES) != binding.profileSettingsPrivacyPreferencesSelect.getSelectedIndex();
    }


    private boolean isPrivacyChanged() {
        return isPrivacyEmailChanged() || isPrivacyLocationChanged() || isPrivacyPreferencesChanged();
    }


    private boolean areAnyChanges() {
        return (isImageChanged() || isPreferenceChanged() || isBioChanged() || isUsernameChanged()
                || isEmailChanged() || isPasswordChanged() || isPrivacyChanged());
    }


    //CHANGING DATA---------------------------------------------------------------------------------


    private void changeUsername() {
        if (validateUsername()) {
            if (isUsernameChanged()) {
                startProgressBar();
                String newUsername = Objects.requireNonNull(binding.profileSettingsAccountUsernameInput.getText()).toString();
                authViewModel.changeUsername(newUsername);
                authViewModel.getChangesStatus().observe(this, status -> {
                    if (!status.contains("ERROR")) {
                        Map<String, Object> changes = new HashMap<>();
                        changes.put(Constants.USERNAME, newUsername);
                        updateUser(changes);
                    } else
                        showSnackBar(status, Snackbar.LENGTH_LONG);
                    stopProgressBar();
                });
            }
        }
    }


    private void changeEmail() {
        if(validateChangeEmail()) {
            if (!user.getEmail().equals(Objects.requireNonNull(binding.profileSettingsAccountEmailInput.getText()).toString())) {
                startProgressBar();
                String currentPassword = Objects.requireNonNull(binding.profileSettingsAccountEmailPasswordCurrentInput.getText()).toString();
                String newEmail = Objects.requireNonNull(binding.profileSettingsAccountEmailInput.getText()).toString();
                authViewModel.changeEmail(currentPassword, newEmail);
                authViewModel.getChangesStatus().observe(this, status -> {
                    if (!status.contains("ERROR")) {
                        Map<String, Object> changes = new HashMap<>();
                        changes.put(Constants.EMAIL, newEmail);
                        updateUser(changes);
                    } else
                        showSnackBar(status, Snackbar.LENGTH_LONG);
                    stopProgressBar();
                });
            } else {
                showSnackBar("ERROR: Current email is equal to new", Snackbar.LENGTH_LONG);
            }
        }
    }


    private void changePassword() {
        if(validateChangePassword()) {
            startProgressBar();
            String currentPassword = Objects.requireNonNull(binding.profileSettingsAccountPasswordCurrentInput.getText()).toString();
            String newPassword = Objects.requireNonNull(binding.profileSettingsAccountPasswordInput.getText()).toString();
            authViewModel.changePassword(currentPassword, newPassword);
            authViewModel.getChangesStatus().observe(this, status -> {
                if (!status.contains("ERROR")) {
                    clearInputs();
                }
                showSnackBar(status, Snackbar.LENGTH_LONG);
                stopProgressBar();
            });
        }
    }


    private void changeProfilePhoto() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && getActivity() != null && getContext() != null) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Constants.RC_EXTERNAL_STORAGE);
            } else {
                selectImage();
            }
        } else {
            selectImage();
        }
    }


    private void selectImage() {
        if (getActivity() != null && getContext() != null) {
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                    .setAspectRatio(1, 1)
                    .start(getContext(), this);
        }
    }


    private void savePersonalChanges() {
        Map<String, Object> changes = new HashMap<>();

        if (isBioChanged()) {
            changes.put(Constants.BIO, Objects.requireNonNull(binding.profileSettingsPersonalBioInput.getText()).toString());
        }

        if(isPreferenceChanged()) {
            changes.put(Constants.PREFERENCES, getUniquePreferences());
        }

        if (isImageChanged()) {
            savePhotoToStorage(changes);
        } else if (!changes.isEmpty()) {
            updateUser(changes);
        }
    }


    private List<String> getUniquePreferences() {
        List<String> preferences = binding.profileSettingsPersonalPreferencesInput.getChipValues();
        LinkedHashSet<String> hashSet = new LinkedHashSet<>(preferences);
        return new ArrayList<>(hashSet);
    }


    private void savePhotoToStorage(Map<String, Object> changes) {
        startProgressBar();
        if (newImageUri.getPath() != null && getContext() != null) {
            File newFile = new File(newImageUri.getPath());
            try {
                compressor = new Compressor(getContext())
                        .setMaxHeight(150)
                        .setMaxWidth(150)
                        .setQuality(100)
                        .compressToBitmap(newFile);
            } catch (IOException e) {
                showSnackBar("ERROR: " + e.getMessage(), Snackbar.LENGTH_LONG);
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            compressor.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] thumb = byteArrayOutputStream.toByteArray();

            storageViewModel.saveToStorage(thumb, user.getUid());
            storageViewModel.getStorageStatus().observe(getViewLifecycleOwner(), status -> {
                if(status.contains("ERROR")) {
                    showSnackBar(status, Snackbar.LENGTH_LONG);
                    stopProgressBar();
                } else {
                    changes.put(Constants.PHOTO, status);
                    updateUser(changes);
                }
            });
        }
    }


    private void savePrivacyChanges() {
        Map<String, Object> changes = new HashMap<>();
        if (isPrivacyEmailChanged()) {
            changes.put(Constants.PRIVACY + "." + Constants.EMAIL, binding.profileSettingsPrivacyEmailSelect.getSelectedIndex());
        }
        if (isPrivacyLocationChanged()) {
            changes.put(Constants.PRIVACY + "." + Constants.LOCATION, binding.profileSettingsPrivacyLocationSelect.getSelectedIndex());
        }
        if (isPrivacyPreferencesChanged()) {
            changes.put(Constants.PRIVACY + "." + Constants.PREFERENCES, binding.profileSettingsPrivacyPreferencesSelect.getSelectedIndex());
        }

        if (!changes.isEmpty()) {
            updateUser(changes);
        }
    }


    private void updateUser(Map<String, Object> changes) {
        startProgressBar();
        userViewModel.updateUser(user, changes);
        userViewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                this.user = user;
                userViewModel.setUser(user);
                binding.setUser(user);
                showSnackBar("Changes saved successfully", Snackbar.LENGTH_LONG);
                clearInputs();
                newImageUri = null;
            } else {
                showSnackBar("ERROR: Failed to update, try again later", Snackbar.LENGTH_LONG);
            }
            stopProgressBar();
        });
    }


    //VALIDATIONS-----------------------------------------------------------------------------------


    private boolean validateUsername() {
        FormHandler formHandler = new FormHandler();
        TextInputEditText input = binding.profileSettingsAccountUsernameInput;
        TextInputLayout layout = binding.profileSettingsAccountUsernameLayout;
        int minLength = 4;

        return formHandler.validateInput(input, layout)
                && formHandler.validateLength(input, layout, minLength);
    }


    private boolean validateChangeEmail() {
        FormHandler formHandler = new FormHandler();
        TextInputEditText currentPasswordInput = binding.profileSettingsAccountEmailPasswordCurrentInput;
        TextInputEditText newEmailInput = binding.profileSettingsAccountEmailInput;
        TextInputEditText confirmEmailInput = binding.profileSettingsAccountEmailConfirmInput;
        TextInputLayout currentPasswordLayout = binding.profileSettingsAccountEmailPasswordCurrentLayout;
        TextInputLayout newEmailLayout = binding.profileSettingsAccountEmailLayout;
        TextInputLayout confirmEmailLayout = binding.profileSettingsAccountEmailConfirmLayout;

        return formHandler.validateInput(currentPasswordInput, currentPasswordLayout)
                && formHandler.validateInput(newEmailInput, newEmailLayout)
                && formHandler.validateInput(confirmEmailInput, confirmEmailLayout)
                && formHandler.validateInputsEquality(newEmailInput, confirmEmailInput, newEmailLayout);
    }


    private boolean validateChangePassword() {
        FormHandler formHandler = new FormHandler();
        TextInputEditText currentInput = binding.profileSettingsAccountPasswordCurrentInput;
        TextInputEditText passInput = binding.profileSettingsAccountPasswordInput;
        TextInputEditText confirmInput = binding.profileSettingsAccountPasswordConfirmInput;
        TextInputLayout currentLayout = binding.profileSettingsAccountPasswordCurrentLayout;
        TextInputLayout passLayout = binding.profileSettingsAccountPasswordLayout;
        TextInputLayout confirmLayout = binding.profileSettingsAccountPasswordConfirmLayout;
        int minLength = 8;

        return formHandler.validateInput(currentInput, currentLayout)
                && formHandler.validateInput(passInput, passLayout)
                && formHandler.validateInput(confirmInput, confirmLayout)
                && formHandler.validateLength(passInput, passLayout, minLength)
                && formHandler.validateInputsEquality(passInput, confirmInput, confirmLayout);
    }


    //OTHERS----------------------------------------------------------------------------------------


    private void startProgressBar() {
        getProgressBarInteractions().startProgressBar(binding.getRoot(), binding.profileSettingsProgressbarLayout, binding.profileSettingsProgressbar);
    }


    private void stopProgressBar() {
        getProgressBarInteractions().stopProgressBar(binding.getRoot(), binding.profileSettingsProgressbarLayout, binding.profileSettingsProgressbar);
    }


    private void showSnackBar(String message, int duration) {
        getSnackBarInteractions().showSnackBar(binding.getRoot(), getActivity(), message, duration);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK && result != null) {
                newImageUri = result.getUri();
                User.loadImage(binding.profileSettingsPersonalPicturePhoto, newImageUri.toString());
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE && result != null) {
                showSnackBar(result.getError().getMessage(), Snackbar.LENGTH_LONG);
            }
        }
    }


    private void clearInputs() {
        new FormHandler().clearInput(binding.profileSettingsAccountPasswordCurrentInput, binding.profileSettingsAccountPasswordCurrentLayout);
        new FormHandler().clearInput(binding.profileSettingsAccountPasswordInput, binding.profileSettingsAccountPasswordLayout);
        new FormHandler().clearInput(binding.profileSettingsAccountPasswordConfirmInput, binding.profileSettingsAccountPasswordConfirmLayout);
        new FormHandler().clearInput(binding.profileSettingsAccountEmailPasswordCurrentInput, binding.profileSettingsAccountEmailPasswordCurrentLayout);
        new FormHandler().clearInput(binding.profileSettingsAccountEmailInput, binding.profileSettingsAccountEmailLayout);
        new FormHandler().clearInput(binding.profileSettingsAccountEmailConfirmInput, binding.profileSettingsAccountEmailConfirmLayout);
    }


    @Override
    public boolean onBackPressed() {
        if (areAnyChanges()) {
            showUnsavedChangesDialog();
            return true;
        } else {
            return false;
        }
    }


    @SuppressWarnings("ConstantConditions")
    private void hideKeyboard() {
        if (getActivity() != null) {
            ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

}
