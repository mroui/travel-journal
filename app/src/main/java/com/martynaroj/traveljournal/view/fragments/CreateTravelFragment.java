package com.martynaroj.traveljournal.view.fragments;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialStyledDatePickerDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.FragmentCreateTravelBinding;
import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.services.others.GooglePlaces;
import com.martynaroj.traveljournal.view.adapters.HashtagAdapter;
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.view.others.classes.FormHandler;
import com.martynaroj.traveljournal.view.others.classes.InputTextWatcher;
import com.martynaroj.traveljournal.view.others.classes.PickerColorize;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;
import com.martynaroj.traveljournal.viewmodels.UserViewModel;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import static android.app.Activity.RESULT_OK;

public class CreateTravelFragment extends BaseFragment implements View.OnClickListener {

    private FragmentCreateTravelBinding binding;
    private UserViewModel userViewModel;
    private User user;

    private long minDate, maxDate;
    private AutocompleteSupportFragment autocompleteFragment;

    private Uri imageUri;
    private long dateFrom, dateTo, timeFrom, timeTo;
    private Place destination;
    private String transportType, accommodationType;
    private Uri transportFileUri, accommodationFileUri;

    public CreateTravelFragment() {
    }


    private CreateTravelFragment(User user) {
        this.user = user;
    }


    public static CreateTravelFragment newInstance(User user) {
        return new CreateTravelFragment(user);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCreateTravelBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initViewModels();
        initContentData();
        initGooglePlaces();
        observeUserChanges();

        setListeners();

        return view;
    }


    //INIT DATA-------------------------------------------------------------------------------------


    private void initViewModels() {
        if (getActivity() != null) {
            userViewModel = new ViewModelProvider(getActivity()).get(UserViewModel.class);
        }
    }


    private void initContentData() {
        minDate = Calendar.getInstance().getTimeInMillis();
        fillSpinner(binding.createTravelStage5TransportTypeSpinner,
                getResources().getStringArray(R.array.transport));
        fillSpinner(binding.createTravelStage6AccommodationTypeSpinner,
                getResources().getStringArray(R.array.accommodation));
        fillTagsInput();
    }


    private void fillTagsInput() {
        if (getContext() != null) {
            final HashtagAdapter adapter = new HashtagAdapter(
                    getContext(),
                    new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.preferences)))
            );
            binding.createTravelStage8TagsInput.setAdapter(adapter);
            binding.createTravelStage8TagsInput.setThreshold(1);
        }
    }


    private void addTransportAccommodationTags() {
        binding.createTravelStage8TagsInput.setText(new ArrayList<>(Arrays.asList(transportType, accommodationType)));
    }


    private void fillSpinner(MaterialSpinner spinner, String[] stringArray) {
        if (getContext() != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    getContext(),
                    android.R.layout.simple_spinner_dropdown_item,
                    stringArray
            );
            spinner.setAdapter(adapter);
        }
    }


    private void initGooglePlaces() {
        GooglePlaces.init(getContext());
        autocompleteFragment = GooglePlaces.initAutoComplete(
                getContext(),
                R.id.create_travel_stage_4_search_place,
                getChildFragmentManager()
        );
    }


    private void observeUserChanges() {
        userViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            this.user = user;
            if (user == null) {
                showSnackBar(getResources().getString(R.string.messages_not_logged_user), Snackbar.LENGTH_LONG);
                if (getParentFragmentManager().getBackStackEntryCount() > 0)
                    getParentFragmentManager().popBackStack();
            }
        });
    }


    //LISTENERS-------------------------------------------------------------------------------------


    private void setListeners() {
        binding.createTravelNextButton.setOnClickListener(this);
        binding.createTravelPreviousButton.setOnClickListener(this);
        binding.createTravelStage2UploadImageButton.setOnClickListener(this);
        binding.createTravelStage3DateFrom.setOnClickListener(view -> {
            binding.createTravelStage3Error.setVisibility(View.GONE);
            showDatePickerDialog(binding.createTravelStage3DateFrom);
        });
        binding.createTravelStage3TimeFrom.setOnClickListener(view -> {
            binding.createTravelStage3Error.setVisibility(View.GONE);
            showTimePickerDialog(binding.createTravelStage3TimeFrom);
        });
        binding.createTravelStage3DateTo.setOnClickListener(view -> {
            binding.createTravelStage3Error.setVisibility(View.GONE);
            showDatePickerDialog(binding.createTravelStage3DateTo);
        });
        binding.createTravelStage3TimeTo.setOnClickListener(view -> {
            binding.createTravelStage3Error.setVisibility(View.GONE);
            showTimePickerDialog(binding.createTravelStage3TimeTo);
        });
        binding.createTravelStage5TransportUploadFileButton.setOnClickListener(this);
        binding.createTravelStage5TransportTypeSpinner.setOnItemSelectedListener((view, position, id, item) -> {
            transportType = getResources().getStringArray(R.array.transport)[position];
            binding.createTravelStage5Error.setVisibility(View.GONE);
        });
        binding.createTravelStage5TransportFileRemoveButton.setOnClickListener(this);
        binding.createTravelStage6AccommodationUploadFileButton.setOnClickListener(this);
        binding.createTravelStage6AccommodationTypeSpinner.setOnItemSelectedListener((view, position, id, item) -> {
            accommodationType = getResources().getStringArray(R.array.accommodation)[position];
            binding.createTravelStage6Error.setVisibility(View.GONE);
        });
        binding.createTravelStage6AccommodationFileRemoveButton.setOnClickListener(this);
        binding.createTravelStage7BudgetInput.addTextChangedListener(new InputTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (binding.createTravelStage7BudgetInput.hasFocus() && s != null)
                    new FormHandler(getContext()).handleCurrency(s, binding.createTravelStage7BudgetInput);
            }
        });
        binding.createTravelStage9FinishButton.setOnClickListener(this);
        setAutocompleteFragmentListeners();
    }


    private void listenViewChanges() {
        ViewFlipper flipper = binding.createTravelViewFlipper;
        MaterialButton previous = binding.createTravelPreviousButton;
        MaterialButton next = binding.createTravelNextButton;

        if (flipper.getDisplayedChild() == 0)
            previous.setEnabled(false);
        else if (flipper.getDisplayedChild() == flipper.getChildCount() - 1)
            next.setEnabled(false);
        else {
            previous.setEnabled(true);
            next.setEnabled(true);
        }
        if(flipper.getDisplayedChild() == 8)
            addTransportAccommodationTags();
    }


    private void setAutocompleteFragmentListeners() {
        if (autocompleteFragment != null && autocompleteFragment.getView() != null) {
            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    binding.createTravelStage4Error.setVisibility(View.GONE);
                    destination = place;
                }

                @Override
                public void onError(@NonNull Status status) {
                }
            });
            autocompleteFragment.getView()
                    .findViewById(R.id.places_autocomplete_clear_button)
                    .setOnClickListener(view -> {
                        autocompleteFragment.setText("");
                        destination = null;
                    });
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.create_travel_previous_button:
                showPreviousStage();
                break;
            case R.id.create_travel_next_button:
                showNextStage();
                break;
            case R.id.create_travel_stage_2_upload_image_button:
                checkPermissionsToSelectImage();
                break;
            case R.id.create_travel_stage_5_transport_upload_file_button:
            case R.id.create_travel_stage_6_accommodation_upload_file_button:
                checkPermissionsToSelectFile();
                break;
            case R.id.create_travel_stage_5_transport_file_remove_button:
                removeFile(Constants.TRANSPORT_FILE);
                break;
            case R.id.create_travel_stage_6_accommodation_file_remove_button:
                removeFile(Constants.ACCOMMODATION_FILE);
                break;
            case R.id.create_travel_stage_9_finish_button:
                break;
        }
    }


    //STAGES----------------------------------------------------------------------------------------


    private void showPreviousStage() {
        ViewFlipper flipper = binding.createTravelViewFlipper;
        flipper.setInAnimation(getContext(), R.anim.enter_left_to_right);
        flipper.setOutAnimation(getContext(), R.anim.exit_left_to_right);
        if (flipper.getDisplayedChild() > 0) {
            hideKeyboard();
            flipper.showPrevious();
        }
        listenViewChanges();
    }


    private void showNextStage() {
        ViewFlipper flipper = binding.createTravelViewFlipper;
        flipper.setInAnimation(getContext(), R.anim.enter_right_to_left);
        flipper.setOutAnimation(getContext(), R.anim.exit_right_to_left);
        if (handleErrors() && flipper.getDisplayedChild() < flipper.getChildCount() - 1) {
            hideKeyboard();
            flipper.showNext();
        }
        listenViewChanges();
    }


    //VALIDATION-----------------------------------------------------------------------------------


    private boolean handleErrors() {
        boolean noErrors = true;
        switch (binding.createTravelViewFlipper.getDisplayedChild()) {
            case 1:
                noErrors = validateName();
                break;
            case 3:
                noErrors = validateDatesTimes();
                break;
            case 4:
                noErrors = validateDestination();
                break;
            case 5:
                noErrors = validateTransport();
                break;
            case 6:
                noErrors = validateAccommodation();
        }
        return noErrors;
    }


    private boolean validateName() {
        return new FormHandler(getContext()).validateLength(
                binding.createTravelStage1NameTextInput,
                binding.createTravelStage1NameTextLayout,
                8
        );
    }


    private boolean validateDatesTimes() {
        if (dateFrom == 0 || dateTo == 0 || timeFrom == 0 || timeTo == 0) {
            binding.createTravelStage3Error.setVisibility(View.VISIBLE);
            return false;
        }
        return true;
    }


    private boolean validateDestination() {
        if (destination == null) {
            binding.createTravelStage4Error.setVisibility(View.VISIBLE);
            return false;
        }
        return true;
    }


    private boolean validateTransport() {
        if (transportType == null) {
            binding.createTravelStage5Error.setVisibility(View.VISIBLE);
            return false;
        }
        return true;
    }


    private boolean validateAccommodation() {
        if (accommodationType == null) {
            binding.createTravelStage6Error.setVisibility(View.VISIBLE);
            return false;
        }
        return true;
    }


    //FILES-----------------------------------------------------------------------------------------


    private void checkPermissionsToSelectImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if (isReadStoragePermissionsGranted())
                requestReadStoragePermissions();
            else
                selectImage();
        else
            selectImage();
    }

    private void checkPermissionsToSelectFile() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if (!isReadStoragePermissionsGranted())
                requestReadStoragePermissions();
            else
                selectFile();
        else
            selectFile();
    }


    private boolean isReadStoragePermissionsGranted() {
        if (getContext() != null)
            return ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        else
            return false;
    }


    private void requestReadStoragePermissions() {
        if (getActivity() != null) {
            ActivityCompat.requestPermissions(
                    getActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    Constants.RC_EXTERNAL_STORAGE_IMG
            );
        }
    }


    private void selectImage() {
        if (getActivity() != null && getContext() != null) {
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                    .setAspectRatio(3, 2)
                    .start(getContext(), this);
        }
    }


    private void selectFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        String[] mimeTypes = {"image/*", "application/pdf"};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        } else {
            StringBuilder mimeTypesStr = new StringBuilder();
            for (String mimeType : mimeTypes)
                mimeTypesStr.append(mimeType).append("|");
            intent.setType(mimeTypesStr.substring(0, mimeTypesStr.length() - 1));
        }
        startActivityForResult(intent, Constants.RC_EXTERNAL_STORAGE_FILE);
    }


    private void loadImage() {
        if (getContext() != null)
            Glide.with(getContext())
                    .load(imageUri).centerCrop()
                    .into(binding.createTravelStage2UploadImageButton);
    }


    @SuppressLint("SetTextI18n")
    private void loadFile(String type) {
        if (type.equals(Constants.TRANSPORT_FILE)) {
            binding.createTravelStage5TransportFileContainer.setVisibility(View.VISIBLE);
            binding.createTravelStage5TransportFileName.setText(getFileName(transportFileUri));
        } else if (type.equals(Constants.ACCOMMODATION_FILE)) {
            binding.createTravelStage6AccommodationFileContainer.setVisibility(View.VISIBLE);
            binding.createTravelStage6AccommodationFileName.setText(getFileName(accommodationFileUri));
        }
    }


    private void removeFile(String type) {
        if (type.equals(Constants.TRANSPORT_FILE)) {
            transportFileUri = null;
            binding.createTravelStage5TransportFileContainer.setVisibility(View.GONE);
        } else if (type.equals(Constants.ACCOMMODATION_FILE)) {
            accommodationFileUri = null;
            binding.createTravelStage6AccommodationFileContainer.setVisibility(View.GONE);
        }
    }


    private String getFileName(Uri uri) {
        if (getContext() != null) {
            String result = null;
            if (uri.getScheme() != null && uri.getScheme().equals("content")) {
                try (Cursor cursor = getContext().getContentResolver().query(
                        uri, null, null, null, null)) {
                    if (cursor != null && cursor.moveToFirst())
                        result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
            if (result == null) {
                result = uri.getPath();
                if (result != null) {
                    int cut = result.lastIndexOf('/');
                    if (cut != -1) result = result.substring(cut + 1);
                }
            }
            return result;
        }
        return null;
    }


    //DIALOGS---------------------------------------------------------------------------------------


    private void showTimePickerDialog(TextInputEditText editText) {
        if (getContext() != null) {
            Calendar now = Calendar.getInstance();
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    getContext(),
                    R.style.DateTimePicker,
                    (timePicker, hourOfDay, minute) -> setTime(editText, hourOfDay, minute),
                    now.get(Calendar.HOUR),
                    now.get(Calendar.MINUTE),
                    false
            );
            timePickerDialog.show();
            PickerColorize.colorizeTimePickerDialog(timePickerDialog,
                    getResources().getColor(R.color.yellow_active));
        }
    }

    @SuppressLint("RestrictedApi")
    private void showDatePickerDialog(TextInputEditText editText) {
        if (getContext() != null) {
            Calendar now = Calendar.getInstance();
            MaterialStyledDatePickerDialog datePickerDialog = new MaterialStyledDatePickerDialog(
                    getContext(),
                    R.style.DateTimePicker,
                    (datePicker, year, month, day) -> setDate(editText, year, month, day),
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH)
            );

            if (editText == binding.createTravelStage3DateFrom) {
                datePickerDialog.getDatePicker().setMinDate(now.getTimeInMillis());
                if (maxDate != 0) {
                    datePickerDialog.getDatePicker().setMaxDate(maxDate);
                }
            } else if (editText == binding.createTravelStage3DateTo) {
                datePickerDialog.getDatePicker().setMinDate(minDate);
            }

            PickerColorize.colorizeDatePicker(datePickerDialog.getDatePicker(),
                    getResources().getColor(R.color.yellow_active));
            datePickerDialog.show();
        }
    }


    @SuppressLint("SimpleDateFormat")
    private void setDate(TextInputEditText editText, int year, int month, int day) {
        Calendar date = Calendar.getInstance();
        date.set(year, month, day);
        editText.setText(new SimpleDateFormat("dd/MM/yyyy").format(date.getTime()));

        if (editText == binding.createTravelStage3DateTo) {
            maxDate = date.getTimeInMillis();
            dateTo = maxDate;
        } else {
            minDate = date.getTimeInMillis();
            dateFrom = minDate;
        }
    }


    @SuppressLint("SimpleDateFormat")
    private void setTime(TextInputEditText editText, int hoursOfDay, int minute) {
        Calendar date = Calendar.getInstance();
        date.set(0, 0, 0, hoursOfDay, minute);
        editText.setText(new SimpleDateFormat("h:mm a").format(date.getTime()));

        if (editText == binding.createTravelStage3TimeFrom)
            timeFrom = date.getTimeInMillis();
        else
            timeTo = date.getTimeInMillis();
    }


    //OTHERS----------------------------------------------------------------------------------------


    private void showSnackBar(String message, int duration) {
        getSnackBarInteractions().showSnackBar(binding.getRoot(), getActivity(), message, duration);
    }

    @SuppressWarnings("ConstantConditions")
    protected void hideKeyboard() {
        if (getActivity() != null) {
            ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK && result != null) {
                imageUri = result.getUri();
                loadImage();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE && result != null)
                showSnackBar(result.getError().getMessage(), Snackbar.LENGTH_LONG);
        }
        if (requestCode == Constants.RC_EXTERNAL_STORAGE_FILE && resultCode == RESULT_OK && data != null) {
            if (binding.createTravelViewFlipper.getDisplayedChild() == 5) {
                transportFileUri = data.getData();
                loadFile(Constants.TRANSPORT_FILE);
            } else if (binding.createTravelViewFlipper.getDisplayedChild() == 6) {
                accommodationFileUri = data.getData();
                loadFile(Constants.ACCOMMODATION_FILE);
            }
        } else {
            showSnackBar(getResources().getString(R.string.messages_error_no_file_selected), Snackbar.LENGTH_LONG);
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
