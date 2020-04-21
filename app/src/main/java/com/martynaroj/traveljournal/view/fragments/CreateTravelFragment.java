package com.martynaroj.traveljournal.view.fragments;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
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
import com.martynaroj.traveljournal.databinding.DialogCustomBinding;
import com.martynaroj.traveljournal.databinding.FragmentCreateTravelBinding;
import com.martynaroj.traveljournal.services.models.Address;
import com.martynaroj.traveljournal.services.models.Reservation;
import com.martynaroj.traveljournal.services.models.Travel;
import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.services.others.GooglePlaces;
import com.martynaroj.traveljournal.services.others.NotificationBroadcast;
import com.martynaroj.traveljournal.view.adapters.HashtagAdapter;
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.view.interfaces.IOnBackPressed;
import com.martynaroj.traveljournal.view.others.classes.DialogHandler;
import com.martynaroj.traveljournal.view.others.classes.FileCompressor;
import com.martynaroj.traveljournal.view.others.classes.FileUriUtils;
import com.martynaroj.traveljournal.view.others.classes.FormHandler;
import com.martynaroj.traveljournal.view.others.classes.InputTextWatcher;
import com.martynaroj.traveljournal.view.others.classes.PickerColorize;
import com.martynaroj.traveljournal.view.others.classes.RequestPermissionsHandler;
import com.martynaroj.traveljournal.view.others.classes.SharedPreferencesUtils;
import com.martynaroj.traveljournal.view.others.enums.Notification;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;
import com.martynaroj.traveljournal.viewmodels.AddressViewModel;
import com.martynaroj.traveljournal.viewmodels.NotificationViewModel;
import com.martynaroj.traveljournal.viewmodels.ReservationViewModel;
import com.martynaroj.traveljournal.viewmodels.StorageViewModel;
import com.martynaroj.traveljournal.viewmodels.TravelViewModel;
import com.martynaroj.traveljournal.viewmodels.UserViewModel;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class CreateTravelFragment extends BaseFragment implements View.OnClickListener, IOnBackPressed {

    private FragmentCreateTravelBinding binding;

    private UserViewModel userViewModel;
    private StorageViewModel storageViewModel;
    private TravelViewModel travelViewModel;
    private AddressViewModel addressViewModel;
    private ReservationViewModel reservationViewModel;
    private NotificationViewModel notificationViewModel;

    private User user;
    private List<User> friends;

    private long minDate, maxDate;
    private AutocompleteSupportFragment autocompleteFragment;
    private boolean transportAccommodationTagsAdded;

    private Uri imageUri;
    private long dateFrom, dateTo, timeFrom, timeTo;
    private Place destination;
    private String transportType, accommodationType;
    private Uri transportFileUri, accommodationFileUri;

    private Travel travel;
    private String travelId, destinationId, accommodationId, transportId;

    private MutableLiveData<Boolean> destinationLiveData, travelLiveData,
            reservationLiveData, changesLiveData;



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
        initLiveData();
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
            storageViewModel = new ViewModelProvider(getActivity()).get(StorageViewModel.class);
            travelViewModel = new ViewModelProvider(getActivity()).get(TravelViewModel.class);
            addressViewModel = new ViewModelProvider(getActivity()).get(AddressViewModel.class);
            reservationViewModel = new ViewModelProvider(getActivity()).get(ReservationViewModel.class);
            notificationViewModel = new ViewModelProvider(getActivity()).get(NotificationViewModel.class);
        }
    }


    private void initLiveData() {
        destinationLiveData = new MutableLiveData<>(false);
        travelLiveData = new MutableLiveData<>(false);
        reservationLiveData = new MutableLiveData<>(false);
        changesLiveData = new MutableLiveData<>(false);
    }


    private void initContentData() {
        minDate = Calendar.getInstance().getTimeInMillis();
        fillSpinner(binding.createTravelStage5TransportTypeSpinner,
                getResources().getStringArray(R.array.transport));
        fillSpinner(binding.createTravelStage6AccommodationTypeSpinner,
                getResources().getStringArray(R.array.accommodation));
        fillTagsInput();
        generateIds();
        loadUserFriends();
    }


    private void loadUserFriends() {
        this.friends = new ArrayList<>();
        if (user != null && user.getFriends() != null && !user.getFriends().isEmpty()) {
            userViewModel.getUsersListData(user.getFriends());
            userViewModel.getUsersList().observe(getViewLifecycleOwner(), users -> {
                if (users != null)
                    this.friends = users;
            });
        }
    }


    private void generateIds() {
        travelId = travelViewModel.generateId();
        destinationId = addressViewModel.generateId();
        transportId = reservationViewModel.generateId();
        accommodationId = reservationViewModel.generateId();
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
        binding.createTravelStage8TagsInput.setText(
                new ArrayList<>(Arrays.asList("#" + transportType, "#" + accommodationType))
        );
        transportAccommodationTagsAdded = true;
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
                back();
            }
        });
    }


    //LISTENERS-------------------------------------------------------------------------------------


    private void setListeners() {
        binding.createTravelArrowButton.setOnClickListener(this);
        binding.createTravelNextButton.setOnClickListener(this);
        binding.createTravelPreviousButton.setOnClickListener(this);
        binding.createTravelStage2UploadImageButton.setOnClickListener(this);
        binding.createTravelStage2UploadImageFileRemoveButton.setOnClickListener(this);
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
        binding.createTravelStage8TagsInput.setOnClickListener(view ->
                binding.createTravelStage8Error.setVisibility(View.GONE)
        );
        binding.createTravelStage8TagsInput.setOnFocusChangeListener((view, focus) ->
                binding.createTravelStage8Error.setVisibility(View.GONE)
        );
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
        if (flipper.getDisplayedChild() == 8 && !transportAccommodationTagsAdded)
            addTransportAccommodationTags();
    }


    private void setAutocompleteFragmentListeners() {
        if (autocompleteFragment != null && autocompleteFragment.getView() != null) {
            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    binding.createTravelStage4Error.setVisibility(View.GONE);
                    if (place.getLatLng() != null)
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
            case R.id.create_travel_arrow_button:
                showUnsavedChangesDialog();
                break;
            case R.id.create_travel_previous_button:
                showPreviousStage();
                break;
            case R.id.create_travel_next_button:
                showNextStage();
                break;
            case R.id.create_travel_stage_2_upload_image_button:
                checkPermissionsToSelectImage();
                break;
            case R.id.create_travel_stage_2_upload_image_file_remove_button:
                removeFile(Constants.TRAVEL_IMAGE_FILE);
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
                finish();
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
                break;
            case 8:
                noErrors = validateTags();
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


    private boolean validateTags() {
        binding.createTravelStage8TagsInput.setText(getUniqueTags());
        if (binding.createTravelStage8TagsInput.getChipValues().size() < 3) {
            binding.createTravelStage8Error.setVisibility(View.VISIBLE);
            return false;
        }
        return true;
    }


    //FILES-----------------------------------------------------------------------------------------


    private void checkPermissionsToSelectImage() {
        if (RequestPermissionsHandler.isReadStorageGranted(getContext()))
            selectImage();
        else
            RequestPermissionsHandler.requestReadStorage(getActivity());
    }


    private void checkPermissionsToSelectFile() {
        if (RequestPermissionsHandler.isReadStorageGranted(getContext()))
            selectFile();
        else
            RequestPermissionsHandler.requestReadStorage(getActivity());
    }


    private void selectImage() {
        if (getContext() != null) {
            CropImage.activity().setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                    .setAspectRatio(3, 2).start(getContext(), this);
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
                    .load(imageUri)
                    .centerCrop()
                    .placeholder(R.drawable.ic_add_a_photo_gray)
                    .into(binding.createTravelStage2UploadImageButton);
        binding.createTravelStage2UploadImageFileContainer.setVisibility(View.VISIBLE);
        binding.createTravelStage2UploadImageFileName.setText(FileUriUtils.getFileName(getContext(), imageUri));
    }


    @SuppressLint("SetTextI18n")
    private void loadFile(String type) {
        if (type.equals(Constants.TRANSPORT_FILE)) {
            binding.createTravelStage5TransportFileContainer.setVisibility(View.VISIBLE);
            binding.createTravelStage5TransportFileName.setText(FileUriUtils.getFileName(getContext(), transportFileUri));
        } else if (type.equals(Constants.ACCOMMODATION_FILE)) {
            binding.createTravelStage6AccommodationFileContainer.setVisibility(View.VISIBLE);
            binding.createTravelStage6AccommodationFileName.setText(FileUriUtils.getFileName(getContext(), accommodationFileUri));
        }
    }


    private void removeFile(String type) {
        switch (type) {
            case Constants.TRANSPORT_FILE:
                transportFileUri = null;
                binding.createTravelStage5TransportFileContainer.setVisibility(View.GONE);
                break;
            case Constants.ACCOMMODATION_FILE:
                accommodationFileUri = null;
                binding.createTravelStage6AccommodationFileContainer.setVisibility(View.GONE);
                break;
            case Constants.TRAVEL_IMAGE_FILE:
                imageUri = null;
                binding.createTravelStage2UploadImageButton.setImageResource(R.drawable.ic_add_a_photo_gray);
                binding.createTravelStage2UploadImageFileContainer.setVisibility(View.GONE);
                break;
        }
    }


    //DIALOGS & DATES-------------------------------------------------------------------------------


    private void showTimePickerDialog(TextInputEditText editText) {
        if (getContext() != null) {
            Calendar now = Calendar.getInstance();
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    getContext(),
                    R.style.DateTimePicker,
                    (timePicker, hourOfDay, minute) -> setTime(editText, hourOfDay, minute),
                    now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE),
                    false
            );
            timePickerDialog.show();
            PickerColorize.colorizeTimePickerDialog(timePickerDialog,
                    getResources().getColor(R.color.main_yellow));
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
                    getResources().getColor(R.color.main_yellow));
            datePickerDialog.show();
        }
    }


    private void showUnsavedChangesDialog() {
        if (getContext() != null && getActivity() != null) {
            Dialog dialog = DialogHandler.createDialog(getContext(), true);
            DialogCustomBinding binding = DialogCustomBinding.inflate(LayoutInflater.from(getContext()));
            dialog.setContentView(binding.getRoot());
            DialogHandler.initContent(
                    getContext(), binding.dialogCustomTitle, R.string.dialog_unsaved_changes_title,
                    binding.dialogCustomDesc, R.string.dialog_unsaved_changes_desc,
                    binding.dialogCustomButtonPositive, R.string.dialog_button_yes,
                    binding.dialogCustomButtonNegative, R.string.dialog_button_no,
                    R.color.main_yellow, R.color.yellow_bg_lighter
            );
            binding.dialogCustomButtonPositive.setOnClickListener(v -> {
                hideKeyboard();
                dialog.dismiss();
                back();
            });
            binding.dialogCustomButtonNegative.setOnClickListener(v -> dialog.dismiss());
            dialog.show();
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


    private long getDateTime(long dateFrom, long timeFrom) {
        Calendar now = Calendar.getInstance();
        Calendar date = Calendar.getInstance();
        Calendar time = Calendar.getInstance();
        date.setTime(new Date(dateFrom));
        time.setTime(new Date(timeFrom));
        now.set(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH),
                time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE));
        return now.getTimeInMillis();
    }


    //DATABASE--------------------------------------------------------------------------------------


    private void finish() {
        startProgressBar();
        createTravel();
        setAlarm();
        createDestinationAddress();
        checkFilesToSave();
        sendNotifications();
        observeFinishTravel();
    }


    private void observeFinishTravel() {
        changesLiveData.observe(getViewLifecycleOwner(), result -> {
            if (destinationLiveData.getValue() != null && destinationLiveData.getValue()
                    && travelLiveData.getValue() != null && travelLiveData.getValue()
                    && reservationLiveData.getValue() != null && reservationLiveData.getValue()) {
                addTravelToUser();
            }
        });
    }


    private void addTravelToUser() {
        userViewModel.updateUser(true, user, new HashMap<String, Object>() {{
            put(Constants.DB_ACTIVE_TRAVEL_ID, travelId);
        }});
        userViewModel.getUserLiveData().observe(getViewLifecycleOwner(), newUser -> {
            if (newUser != null) {
                userViewModel.setUser(newUser);
                back();
            }
            stopProgressBar();
        });
    }


    private void createTravel() {
        travel = new Travel(
                travelId,
                user.getUid(),
                Objects.requireNonNull(binding.createTravelStage1NameTextInput.getText()).toString(),
                getDateTime(dateFrom, timeFrom),
                getDateTime(dateTo, timeTo),
                destinationId,
                transportId,
                accommodationId,
                Double.valueOf(Objects.requireNonNull(binding.createTravelStage7BudgetInput.getText()).toString()),
                binding.createTravelStage8TagsInput.getChipValues()
        );
        addTravel();
    }


    private void addTravel() {
        travelViewModel.addTravel(travel);
        travelViewModel.getStatusData().observe(getViewLifecycleOwner(), status -> {
            if (status != null) {
                if (!status.equals(com.martynaroj.traveljournal.view.others.enums.Status.ERROR)) {
                    travelLiveData.setValue(true);
                    changesLiveData.setValue(true);
                } else {
                    showSnackBar(getResources().getString(R.string.messages_error_failed_add_travel),
                            Snackbar.LENGTH_LONG);
                    stopProgressBar();
                }
            }
        });
    }


    private void setAlarm() {
        if (binding.createTravelStage3SetAlarm.isChecked()) {
            String note = getResources().getString(R.string.messages_travel_start);
            long dateEnd = getDateTime(dateFrom, timeFrom);
            long timeDifference = dateEnd - Calendar.getInstance().getTimeInMillis();

            SharedPreferencesUtils.saveAlarmSet(getContext(), dateEnd, note);
            NotificationBroadcast.createNotificationChannel(getContext());
            NotificationBroadcast.sendBroadcast(
                    getContext(),
                    new Intent(getContext(), NotificationBroadcast.class),
                    timeDifference,
                    getResources().getString(R.string.messages_travel_start)
            );
        }
    }


    private void checkFilesToSave() {
        String path = user.getUid() + "/" + Constants.STORAGE_TRAVELS + "/" + travelId;

        if (imageUri != null)
            prepareFileToSave(Constants.IMAGE, null, imageUri, path,
                    Constants.TRAVEL_IMG_H, Constants.TRAVEL_IMG_W);

        if (transportFileUri != null)
            prepareFileToSave(Constants.TRANSPORT, transportId, transportFileUri, path,
                    Constants.RESERVATION_IMG_H, Constants.RESERVATIONS_IMG_W);
        else
            createReservation(Constants.TRANSPORT, transportId, null);

        if (accommodationFileUri != null)
            prepareFileToSave(Constants.ACCOMMODATION, accommodationId, accommodationFileUri, path,
                    Constants.RESERVATION_IMG_H, Constants.RESERVATIONS_IMG_W);
        else
            createReservation(Constants.ACCOMMODATION, accommodationId, null);
    }


    private void createDestinationAddress() {
        Address destination = new Address(
                destinationId,
                this.destination.getName(),
                this.destination.getAddress(),
                Objects.requireNonNull(this.destination.getLatLng()).latitude,
                this.destination.getLatLng().longitude
        );
        addDestination(destination);
    }


    private void addDestination(Address destination) {
        addressViewModel.addAddress(destination, destinationId);
        addressViewModel.getStatus().observe(getViewLifecycleOwner(), status -> {
            if (status != null) {
                if (!status.contains(getResources().getString(R.string.messages_error))) {
                    destinationLiveData.setValue(true);
                    changesLiveData.setValue(true);
                } else {
                    showSnackBar(getResources().getString(R.string.messages_error_failed_add_destination),
                            Snackbar.LENGTH_LONG);
                    stopProgressBar();
                }
            }
        });
    }


    private void prepareFileToSave(String kind, String reservationId, Uri uri, String path,
                                   Integer maxHeight, Integer maxWidth) {
        if (uri.getPath() != null && getContext() != null) {
            String fileName = FileUriUtils.getFileName(getContext(), uri);
            if (fileName != null) {
                String fileExtension = fileName.substring(fileName.lastIndexOf("."));
                if (Objects.equals(fileExtension.trim(), Constants.PDF_EXT))
                    addFile(kind, reservationId, uri, null, kind + Constants.PDF_EXT, path);
                else {
                    byte[] thumb = FileCompressor.compressToByte(getContext(), uri, maxHeight, maxWidth);
                    if (thumb != null)
                        addFile(kind, reservationId, null, thumb, kind + Constants.JPG_EXT, path);
                    else {
                        showSnackBar(getResources().getString(R.string.messages_error_failed_load_file),
                                Snackbar.LENGTH_LONG);
                        stopProgressBar();
                    }
                }
            }
        }
    }


    private void addFile(String kind, String reservationId, Uri uri, byte[] thumb, String name, String path) {
        if (uri == null)
            storageViewModel.saveImageToStorage(thumb, name, path);
        else
            storageViewModel.saveFileToStorage(uri, name, path);

        storageViewModel.getStorageStatus().observe(getViewLifecycleOwner(), status -> {
            if (status != null && !status.contains(getResources().getString(R.string.messages_error)))
                if (kind.equals(Constants.IMAGE))
                    updateTravel(new HashMap<String, Object>() {{
                        put(kind, status);
                    }});
                else
                    createReservation(kind, reservationId, status);
            else {
                showSnackBar(status, Snackbar.LENGTH_LONG);
                stopProgressBar();
            }
        });
    }


    private void updateTravel(Map<String, Object> changes) {
        travelViewModel.updateTravel(travel.getId(), changes);
        travelViewModel.getTravelLiveData().observe(getViewLifecycleOwner(), travelData -> {
            if (travelData != null) {
                travel = travelData;
            }
        });
    }


    private void createReservation(String kind, String id, String file) {
        String contact = null;
        String type = null;
        if (kind.equals(Constants.TRANSPORT)) {
            Editable s = binding.createTravelStage5TransportContactInput.getText();
            contact = s != null ? s.toString() : null;
            type = transportType;
        } else if (kind.equals(Constants.ACCOMMODATION)) {
            Editable s = binding.createTravelStage6AccommodationContactInput.getText();
            contact = s != null ? s.toString() : null;
            type = accommodationType;
        }
        addReservation(kind, new Reservation(id, type, file, contact));
    }


    private void addReservation(String kind, Reservation reservation) {
        reservationViewModel.addReservation(reservation);
        reservationViewModel.getStatusData().observe(getViewLifecycleOwner(), status -> {
            if (status != null) {
                if (!status.equals(com.martynaroj.traveljournal.view.others.enums.Status.ERROR)) {
                    changesLiveData.setValue(true);
                    reservationLiveData.setValue(true);
                } else {
                    stopProgressBar();
                    if (kind.equals(Constants.TRANSPORT))
                        showSnackBar(getResources().getString(R.string.messages_error_failed_add_transport),
                                Snackbar.LENGTH_LONG);
                    else if (kind.equals(Constants.ACCOMMODATION))
                        showSnackBar(getResources().getString(R.string.messages_error_failed_add_accommodation),
                                Snackbar.LENGTH_LONG);
                }
            }
        });
    }


    private void sendNotifications() {
        for (User friend : this.friends) {
            String id = notificationViewModel.generateId();
            notificationViewModel.sendNotification(
                    new com.martynaroj.traveljournal.services.models.Notification(
                            id, user.getUid(), friend.getUid(), Notification.START_TRIP.ordinal()
                    ));
            List<String> notifications = friend.getNotifications();
            notifications.add(id);
            userViewModel.updateUser(false, friend, new HashMap<String, Object>(){{
                put(Constants.DB_NOTIFICATIONS, notifications);
            }});
        }
    }


    //OTHERS----------------------------------------------------------------------------------------


    private void back() {
        if (getParentFragmentManager().getBackStackEntryCount() > 0)
            getParentFragmentManager().popBackStack();
    }


    private List<String> getUniqueTags() {
        return new ArrayList<>(new LinkedHashSet<>(binding.createTravelStage8TagsInput.getChipValues()));
    }


    private void showSnackBar(String message, int duration) {
        getSnackBarInteractions().showSnackBar(binding.getRoot(), getActivity(), message, duration);
    }


    private void startProgressBar() {
        getProgressBarInteractions().startProgressBar(binding.getRoot(),
                binding.createTravelProgressbarLayout, binding.createTravelProgressbar);
    }


    private void stopProgressBar() {
        getProgressBarInteractions().stopProgressBar(binding.getRoot(),
                binding.createTravelProgressbarLayout, binding.createTravelProgressbar);
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
        } else if (requestCode == Constants.RC_EXTERNAL_STORAGE_FILE && resultCode == RESULT_OK && data != null) {
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
    public boolean onBackPressed() {
        showUnsavedChangesDialog();
        return true;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
