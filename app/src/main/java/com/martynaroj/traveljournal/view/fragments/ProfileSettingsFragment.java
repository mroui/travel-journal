package com.martynaroj.traveljournal.view.fragments;


import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.DialogCustomBinding;
import com.martynaroj.traveljournal.databinding.FragmentProfileSettingsBinding;
import com.martynaroj.traveljournal.services.models.Address;
import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.services.others.GooglePlaces;
import com.martynaroj.traveljournal.view.adapters.HashtagAdapter;
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.view.interfaces.IOnBackPressed;
import com.martynaroj.traveljournal.view.others.classes.DialogHandler;
import com.martynaroj.traveljournal.view.others.classes.FileCompressor;
import com.martynaroj.traveljournal.view.others.classes.FormHandler;
import com.martynaroj.traveljournal.view.others.classes.RequestPermissionsHandler;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;
import com.martynaroj.traveljournal.viewmodels.AddressViewModel;
import com.martynaroj.traveljournal.viewmodels.AuthViewModel;
import com.martynaroj.traveljournal.viewmodels.StorageViewModel;
import com.martynaroj.traveljournal.viewmodels.UserViewModel;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class ProfileSettingsFragment extends BaseFragment implements View.OnClickListener, IOnBackPressed {

    private FragmentProfileSettingsBinding binding;
    private UserViewModel userViewModel;
    private AuthViewModel authViewModel;
    private User user;

    private Uri newImageUri;
    private StorageViewModel storageViewModel;

    private Address newLocation;
    private Address currentLocation;
    private AutocompleteSupportFragment autocompleteFragment;
    private FindCurrentPlaceRequest request;
    private PlacesClient placesClient;
    private AddressViewModel addressViewModel;


    public static ProfileSettingsFragment newInstance(User user) {
        ProfileSettingsFragment fragment = new ProfileSettingsFragment();
        Bundle args = new Bundle();
        args.putSerializable(Constants.BUNDLE_USER, user);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user = (User) getArguments().getSerializable(Constants.BUNDLE_USER);
        }
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileSettingsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initViewModels();
        initContentData();
        initGooglePlaces();
        initUser();

        setListeners();

        return view;
    }


    //INIT DATA-------------------------------------------------------------------------------------


    private void initContentData() {
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


    private void initGooglePlaces() {
        if (getContext() != null) {
            GooglePlaces.init(getContext());
            placesClient = GooglePlaces.initClient(getContext());
            request = GooglePlaces.initRequest();
            autocompleteFragment = GooglePlaces.initAutoComplete(
                    getContext(),
                    R.id.profile_settings_personal_location_autocomplete,
                    getChildFragmentManager()
            );
        }
    }


    private void initViewModels() {
        if (getActivity() != null) {
            userViewModel = new ViewModelProvider(getActivity()).get(UserViewModel.class);
            storageViewModel = new ViewModelProvider(getActivity()).get(StorageViewModel.class);
            authViewModel = new ViewModelProvider(getActivity()).get(AuthViewModel.class);
            addressViewModel = new ViewModelProvider(getActivity()).get(AddressViewModel.class);
        }
    }


    private void initUser() {
        if (user != null) {
            binding.setUser(user);
            initLocation();
            initUserData();
        } else
            getCurrentUser();
    }


    private void initLocation() {
        if (user.getLocation() != null && !user.getLocation().equals("")) {
            startProgressBar();
            addressViewModel.getAddress(user.getLocation());
            addressViewModel.getAddressData().observe(getViewLifecycleOwner(), address -> {
                if (address != null) {
                    currentLocation = address;
                    currentLocation.setId(user.getLocation());
                    newLocation = currentLocation;
                    autocompleteFragment.setText(currentLocation.getAddress());
                }
                stopProgressBar();
            });
        }
    }


    private void getCurrentUser() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            startProgressBar();
            userViewModel.getUserData(firebaseAuth.getCurrentUser().getUid());
            userViewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
                if (user != null) {
                    this.user = user;
                    binding.setUser(user);
                    initLocation();
                    initUserData();
                } else {
                    this.user = new User();
                    showSnackBar(getResources().getString(R.string.messages_error_no_user_database), Snackbar.LENGTH_LONG);
                }
                stopProgressBar();
            });
        } else
            showSnackBar(getResources().getString(R.string.messages_error_current_user_not_available), Snackbar.LENGTH_LONG);
    }


    private void initUserData() {
        int index = Objects.requireNonNull(user.getPrivacy().get(Constants.DB_EMAIL));
        binding.profileSettingsPrivacyEmailSelect.setSelectedIndex(index);

        index = Objects.requireNonNull(user.getPrivacy().get(Constants.DB_LOCATION));
        binding.profileSettingsPrivacyLocationSelect.setSelectedIndex(index);

        index = Objects.requireNonNull(user.getPrivacy().get(Constants.DB_PREFERENCES));
        binding.profileSettingsPrivacyPreferencesSelect.setSelectedIndex(index);
    }


    //LISTENERS-------------------------------------------------------------------------------------


    private void setListeners() {
        new FormHandler(getContext()).addWatcher(binding.profileSettingsAccountPasswordCurrentInput,
                binding.profileSettingsAccountPasswordCurrentLayout);
        new FormHandler(getContext()).addWatcher(binding.profileSettingsAccountPasswordInput,
                binding.profileSettingsAccountPasswordLayout);
        new FormHandler(getContext()).addWatcher(binding.profileSettingsAccountPasswordConfirmInput,
                binding.profileSettingsAccountPasswordConfirmLayout);
        new FormHandler(getContext()).addWatcher(binding.profileSettingsAccountEmailPasswordCurrentInput,
                binding.profileSettingsAccountEmailPasswordCurrentLayout);
        new FormHandler(getContext()).addWatcher(binding.profileSettingsAccountEmailInput,
                binding.profileSettingsAccountEmailLayout);
        new FormHandler(getContext()).addWatcher(binding.profileSettingsAccountEmailConfirmInput,
                binding.profileSettingsAccountEmailConfirmLayout);
        new FormHandler(getContext()).addWatcher(binding.profileSettingsAccountUsernameInput,
                binding.profileSettingsAccountUsernameLayout);
        binding.profileSettingsArrowButton.setOnClickListener(this);
        binding.profileSettingsPersonalPictureSection.setOnClickListener(this);
        binding.profileSettingsPersonalLocationButton.setOnClickListener(this);
        binding.profileSettingsPersonalSaveButton.setOnClickListener(this);
        binding.profileSettingsAccountUsernameSaveButton.setOnClickListener(this);
        binding.profileSettingsAccountEmailSaveButton.setOnClickListener(this);
        binding.profileSettingsAccountPasswordSaveButton.setOnClickListener(this);
        binding.profileSettingsPrivacySaveButton.setOnClickListener(this);
        binding.profileSettingsAboutCreditsSection.setOnClickListener(this);
        binding.profileSettingsHelpContactSection.setOnClickListener(this);
        if (autocompleteFragment != null && autocompleteFragment.getView() != null) {
            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @SuppressWarnings("ConstantConditions")
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    newLocation = new Address(place.getName(), place.getAddress(),
                            place.getLatLng().latitude, place.getLatLng().longitude);
                    autocompleteFragment.setText(newLocation.getAddress());
                }

                @Override
                public void onError(@NonNull Status status) {
                }
            });
            autocompleteFragment.getView().findViewById(R.id.places_autocomplete_clear_button).setOnClickListener(view -> {
                newLocation = null;
                autocompleteFragment.setText("");
            });
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.profile_settings_arrow_button:
                if (!areAnyChanges()) {
                    hideKeyboard();
                    back();
                } else
                    showUnsavedChangesDialog();
                return;
            case R.id.profile_settings_personal_picture_section:
                changeProfilePhoto();
                return;
            case R.id.profile_settings_personal_location_button:
                detectLocation();
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
                return;
            case R.id.profile_settings_help_contact_section:
                getContactInfo();
        }
    }


    //DIALOGS---------------------------------------------------------------------------------------


    private void showUnsavedChangesDialog() {
        if (getContext() != null && getActivity() != null) {
            Dialog dialog = DialogHandler.createDialog(getContext(), true);
            DialogCustomBinding binding = DialogCustomBinding.inflate(LayoutInflater.from(getContext()));
            dialog.setContentView(binding.getRoot());
            DialogHandler.initContent(getContext(), binding.dialogCustomTitle, R.string.dialog_unsaved_changes_title,
                    binding.dialogCustomDesc, R.string.dialog_unsaved_changes_desc,
                    binding.dialogCustomButtonPositive, R.string.dialog_button_yes,
                    binding.dialogCustomButtonNegative, R.string.dialog_button_no,
                    R.color.main_blue, R.color.blue_bg_lighter);
            binding.dialogCustomButtonPositive.setOnClickListener(v -> {
                hideKeyboard();
                dialog.dismiss();
                back();
            });
            binding.dialogCustomButtonNegative.setOnClickListener(v -> dialog.dismiss());
            dialog.show();
        }
    }


    private void showCreditsDialog() {
        if (getContext() != null) {
            Dialog dialog = DialogHandler.createDialog(getContext(), true);
            DialogCustomBinding binding = DialogCustomBinding.inflate(LayoutInflater.from(getContext()));
            dialog.setContentView(binding.getRoot());
            DialogHandler.initContent(getContext(), binding.dialogCustomTitle, R.string.dialog_credits_title,
                    binding.dialogCustomDesc, R.string.profile_settings_credits_list,
                    binding.dialogCustomButtonPositive, R.string.dialog_button_ok,
                    binding.dialogCustomButtonNegative, R.string.dialog_button_cancel,
                    R.color.main_blue, R.color.blue_bg_lighter);
            binding.dialogCustomDesc.setText(Html.fromHtml(
                    getResources().getString(R.string.profile_settings_credits_list))
            );
            binding.dialogCustomDesc.setMovementMethod(LinkMovementMethod.getInstance());
            binding.dialogCustomButtonPositive.setOnClickListener(v -> dialog.dismiss());
            binding.dialogCustomButtonNegative.setVisibility(View.GONE);
            dialog.show();
        }
    }


    private void getContactInfo() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("plain/text");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getResources().getString(R.string.contact_email)});
        startActivity(Intent.createChooser(intent, ""));
    }


    //CHECKING CHANGES------------------------------------------------------------------------------


    private boolean isImageChanged() {
        return newImageUri != null;
    }


    private boolean isUsernameChanged() {
        return !user.getUsername().equals(
                Objects.requireNonNull(binding.profileSettingsAccountUsernameInput.getText()).toString());
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
        return user.getPrivacy().get(Constants.DB_EMAIL) != binding.profileSettingsPrivacyEmailSelect.getSelectedIndex();
    }


    @SuppressWarnings("ConstantConditions")
    private boolean isPrivacyLocationChanged() {
        return user.getPrivacy().get(Constants.DB_LOCATION) != binding.profileSettingsPrivacyLocationSelect.getSelectedIndex();
    }


    @SuppressWarnings("ConstantConditions")
    private boolean isPrivacyPreferencesChanged() {
        return user.getPrivacy().get(Constants.DB_PREFERENCES) != binding.profileSettingsPrivacyPreferencesSelect.getSelectedIndex();
    }


    private boolean isLocationChanged() {
        if (autocompleteFragment != null) {
            return (user.getLocation() == null && newLocation != null)
                    || (user.getLocation() != null && currentLocation != null
                    && newLocation != null && !currentLocation.equals(newLocation))
                    || (user.getLocation() != null && newLocation == null);
        }
        return false;
    }


    private boolean isPrivacyChanged() {
        return isPrivacyEmailChanged() || isPrivacyLocationChanged() || isPrivacyPreferencesChanged();
    }


    private boolean areAnyChanges() {
        return (isImageChanged() || isPreferenceChanged() || isBioChanged() || isUsernameChanged()
                || isEmailChanged() || isPasswordChanged() || isPrivacyChanged() || isLocationChanged());
    }


    //CHANGING DATA---------------------------------------------------------------------------------


    private void changeUsername() {
        if (validateUsername()) {
            if (isUsernameChanged()) {
                startProgressBar();
                String newUsername = Objects.requireNonNull(binding.profileSettingsAccountUsernameInput.getText()).toString();
                authViewModel.changeUsername(newUsername);
                authViewModel.getChangesStatus().observe(this, status -> {
                    if (!status.contains(Constants.ERROR)) {
                        Map<String, Object> changes = new HashMap<>();
                        changes.put(Constants.DB_USERNAME, newUsername);
                        updateUser(changes);
                    } else
                        showSnackBar(status, Snackbar.LENGTH_LONG);
                    stopProgressBar();
                });
            }
        }
    }


    private void changeEmail() {
        if (validateChangeEmail()) {
            if (!user.getEmail().equals(Objects.requireNonNull(binding.profileSettingsAccountEmailInput.getText()).toString())) {
                startProgressBar();
                String currentPassword = Objects.requireNonNull(binding.profileSettingsAccountEmailPasswordCurrentInput.getText()).toString();
                String newEmail = Objects.requireNonNull(binding.profileSettingsAccountEmailInput.getText()).toString();
                authViewModel.changeEmail(currentPassword, newEmail);
                authViewModel.getChangesStatus().observe(this, status -> {
                    if (!status.contains(Constants.ERROR)) {
                        Map<String, Object> changes = new HashMap<>();
                        changes.put(Constants.DB_EMAIL, newEmail);
                        updateUser(changes);
                    } else
                        showSnackBar(status, Snackbar.LENGTH_LONG);
                    stopProgressBar();
                });
            } else
                showSnackBar(getResources().getString(R.string.messages_error_current_email_equal), Snackbar.LENGTH_LONG);
        }
    }


    private void changePassword() {
        if (validateChangePassword()) {
            startProgressBar();
            String currentPassword = Objects.requireNonNull(
                    binding.profileSettingsAccountPasswordCurrentInput.getText()).toString();
            String newPassword = Objects.requireNonNull(
                    binding.profileSettingsAccountPasswordInput.getText()).toString();
            authViewModel.changePassword(currentPassword, newPassword);
            authViewModel.getChangesStatus().observe(this, status -> {
                if (!status.contains(Constants.ERROR)) {
                    clearInputs();
                    showSnackBar(status, Snackbar.LENGTH_SHORT);
                } else
                    showSnackBar(status, Snackbar.LENGTH_LONG);
                stopProgressBar();
            });
        }
    }


    private void changeProfilePhoto() {
        if (RequestPermissionsHandler.isReadStorageGranted(getContext()))
            selectImage();
        else
            RequestPermissionsHandler.requestReadStorage(getActivity());
    }


    private void selectImage() {
        if (getActivity() != null && getContext() != null) {
            CropImage.activity().setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                    .setAspectRatio(1, 1).start(getContext(), this);
        }
    }


    private void savePersonalChanges() {
        Map<String, Object> changes = new HashMap<>();

        if (isBioChanged()) {
            changes.put(Constants.DB_BIO, Objects.requireNonNull(binding.profileSettingsPersonalBioInput.getText()).toString());
        }

        if (isPreferenceChanged()) {
            changes.put(Constants.DB_PREFERENCES, getUniquePreferences());
        }

        if (isLocationChanged()) {
            addAddress();
        }

        if (isImageChanged()) {
            savePhotoToStorage(changes);
        } else if (!changes.isEmpty()) {
            updateUser(changes);
        }
    }


    private void addAddress() {
        startProgressBar();
        addressViewModel.addAddress(newLocation, user.getLocation());
        addressViewModel.getStatus().observe(getViewLifecycleOwner(), status -> {
            if (status != null) {
                if (!status.contains(Constants.ERROR)) {
                    Map<String, Object> changes = new HashMap<>();
                    changes.put(Constants.DB_LOCATION, status);
                    updateUser(changes);
                    if (newLocation == null) newLocation = new Address(status);
                    currentLocation = newLocation;
                    if (currentLocation.getName() != null)
                        autocompleteFragment.setText(currentLocation.getAddress());
                } else
                    showSnackBar(status, Snackbar.LENGTH_LONG);
            } else
                showSnackBar(getResources().getString(R.string.messages_error_failed_save_location), Snackbar.LENGTH_LONG);
            stopProgressBar();
        });
    }


    private List<String> getUniquePreferences() {
        List<String> preferences = binding.profileSettingsPersonalPreferencesInput.getChipValues();
        LinkedHashSet<String> hashSet = new LinkedHashSet<>(preferences);
        return new ArrayList<>(hashSet);
    }


    private void savePhotoToStorage(Map<String, Object> changes) {
        if (newImageUri.getPath() != null && getContext() != null) {
            byte[] thumb = FileCompressor.compressToByte(getContext(), newImageUri,
                    Constants.USER_IMG_H, Constants.USER_IMG_W);
            storageViewModel.saveImageToStorage(thumb, user.getUid() + ".jpg", user.getUid());
            storageViewModel.getStorageStatus().observe(getViewLifecycleOwner(), status -> {
                if (status.contains(Constants.ERROR)) {
                    showSnackBar(status, Snackbar.LENGTH_LONG);
                } else {
                    changes.put(Constants.DB_PHOTO, status);
                    updateUser(changes);
                }
                stopProgressBar();
            });
        }
    }


    private void savePrivacyChanges() {
        Map<String, Object> changes = new HashMap<>();
        if (isPrivacyEmailChanged()) {
            changes.put(Constants.DB_PRIVACY + "." +
                    Constants.DB_EMAIL, binding.profileSettingsPrivacyEmailSelect.getSelectedIndex());
        }
        if (isPrivacyLocationChanged()) {
            changes.put(Constants.DB_PRIVACY + "." +
                    Constants.DB_LOCATION, binding.profileSettingsPrivacyLocationSelect.getSelectedIndex());
        }
        if (isPrivacyPreferencesChanged()) {
            changes.put(Constants.DB_PRIVACY + "." +
                    Constants.DB_PREFERENCES, binding.profileSettingsPrivacyPreferencesSelect.getSelectedIndex());
        }

        if (!changes.isEmpty()) {
            updateUser(changes);
        }
    }


    private void updateUser(Map<String, Object> changes) {
        startProgressBar();
        userViewModel.updateUser(true, user, changes);
        userViewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                this.user = user;
                userViewModel.setUser(user);
                binding.setUser(user);
                showSnackBar(getResources().getString(R.string.messages_changes_saved), Snackbar.LENGTH_SHORT);
                clearInputs();
                newImageUri = null;
            } else
                showSnackBar(getResources().getString(R.string.messages_error_failed_update), Snackbar.LENGTH_LONG);
            stopProgressBar();
        });
    }


    //VALIDATIONS-----------------------------------------------------------------------------------


    private boolean validateUsername() {
        FormHandler formHandler = new FormHandler(getContext());
        TextInputEditText input = binding.profileSettingsAccountUsernameInput;
        TextInputLayout layout = binding.profileSettingsAccountUsernameLayout;
        int minLength = 4;

        return formHandler.validateInput(input, layout)
                && formHandler.validateLength(input, layout, minLength);
    }


    private boolean validateChangeEmail() {
        FormHandler formHandler = new FormHandler(getContext());
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
        FormHandler formHandler = new FormHandler(getContext());
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


    private void detectLocation() {
        if (RequestPermissionsHandler.isFineLocationGranted(getContext())) {
            startProgressBar();
            addressViewModel.detectAddress(placesClient, request);
            addressViewModel.getDetectedAddress().observe(getViewLifecycleOwner(), response -> {
                if (response != null) {
                    Place place = response.getPlaceLikelihoods().get(0).getPlace();
                    double lat = place.getLatLng() != null ? place.getLatLng().latitude : 0;
                    double lon = place.getLatLng() != null ? place.getLatLng().longitude : 0;
                    newLocation = new Address(place.getName(), place.getAddress(), lat, lon);
                    autocompleteFragment.setText(newLocation.getAddress());
                } else
                    showSnackBar(getResources().getString(R.string.messages_error_localize), Snackbar.LENGTH_LONG);
                stopProgressBar();
            });
        } else
            RequestPermissionsHandler.requestFineLocation(this);
    }


    private void back() {
        if (getParentFragmentManager().getBackStackEntryCount() > 0)
            getParentFragmentManager().popBackStack();
    }


    private void startProgressBar() {
        getProgressBarInteractions().startProgressBar(binding.getRoot(),
                binding.profileSettingsProgressbarLayout, binding.profileSettingsProgressbar);
    }


    private void stopProgressBar() {
        getProgressBarInteractions().stopProgressBar(binding.getRoot(),
                binding.profileSettingsProgressbarLayout, binding.profileSettingsProgressbar);
    }


    private void showSnackBar(String message, int duration) {
        getSnackBarInteractions().showSnackBar(binding.getRoot(), getActivity(), message, duration);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
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
        new FormHandler(getContext()).clearInput(binding.profileSettingsAccountPasswordCurrentInput,
                binding.profileSettingsAccountPasswordCurrentLayout);
        new FormHandler(getContext()).clearInput(binding.profileSettingsAccountPasswordInput,
                binding.profileSettingsAccountPasswordLayout);
        new FormHandler(getContext()).clearInput(binding.profileSettingsAccountPasswordConfirmInput,
                binding.profileSettingsAccountPasswordConfirmLayout);
        new FormHandler(getContext()).clearInput(binding.profileSettingsAccountEmailPasswordCurrentInput,
                binding.profileSettingsAccountEmailPasswordCurrentLayout);
        new FormHandler(getContext()).clearInput(binding.profileSettingsAccountEmailInput,
                binding.profileSettingsAccountEmailLayout);
        new FormHandler(getContext()).clearInput(binding.profileSettingsAccountEmailConfirmInput,
                binding.profileSettingsAccountEmailConfirmLayout);
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


    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

}
