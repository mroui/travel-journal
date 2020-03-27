package com.martynaroj.traveljournal.view.fragments;


import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
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
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.view.others.classes.PickerColorize;
import com.martynaroj.traveljournal.viewmodels.UserViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CreateTravelFragment extends BaseFragment implements View.OnClickListener {

    private FragmentCreateTravelBinding binding;
    private UserViewModel userViewModel;
    private User user;

    private long minDate, maxDate;

    private AutocompleteSupportFragment autocompleteFragment;

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
        binding.createTravelStage3DateFrom.setOnClickListener(view ->
                showDatePickerDialog(binding.createTravelStage3DateFrom)
        );
        binding.createTravelStage3TimeFrom.setOnClickListener(view ->
                showTimePickerDialog(binding.createTravelStage3TimeFrom)
        );
        binding.createTravelStage3DateTo.setOnClickListener(view ->
                showDatePickerDialog(binding.createTravelStage3DateTo)
        );
        binding.createTravelStage3TimeTo.setOnClickListener(view ->
                showTimePickerDialog(binding.createTravelStage3TimeTo)
        );
        binding.createTravelStage5TransportUploadFileButton.setOnClickListener(this);
        setViewFlipperListeners();
        setAutocompleteFragmentListeners();
    }


    private void setViewFlipperListeners() {
        ViewFlipper flipper = binding.createTravelViewFlipper;
        MaterialButton previous = binding.createTravelPreviousButton;
        MaterialButton next = binding.createTravelNextButton;

        flipper.addOnLayoutChangeListener((view, l, t, r, b, ol, ot, or, ob) -> {
            if (flipper.getDisplayedChild() == 0)
                previous.setEnabled(false);
            else if (flipper.getDisplayedChild() == flipper.getChildCount() - 1)
                next.setEnabled(false);
            else {
                previous.setEnabled(true);
                next.setEnabled(true);
            }
        });
    }


    private void setAutocompleteFragmentListeners() {
        if (autocompleteFragment != null && autocompleteFragment.getView() != null) {
            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull com.google.android.libraries.places.api.model.Place place) {
                    LatLng latLng = place.getLatLng();
                    if (latLng != null) {
                        //todo
                    }
                }

                @Override
                public void onError(@NonNull Status status) {
                }
            });
            autocompleteFragment.getView()
                    .findViewById(R.id.places_autocomplete_clear_button)
                    .setOnClickListener(view -> autocompleteFragment.setText(""));
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
                break;
            case R.id.create_travel_stage_5_transport_upload_file_button:
                break;
        }
    }


    //STAGES----------------------------------------------------------------------------------------


    private void showPreviousStage() {
        ViewFlipper flipper = binding.createTravelViewFlipper;
        flipper.setInAnimation(getContext(), R.anim.enter_left_to_right);
        flipper.setOutAnimation(getContext(), R.anim.exit_left_to_right);
        if (flipper.getDisplayedChild() > 0)
            flipper.showPrevious();
    }


    private void showNextStage() {
        ViewFlipper flipper = binding.createTravelViewFlipper;
        flipper.setInAnimation(getContext(), R.anim.enter_right_to_left);
        flipper.setOutAnimation(getContext(), R.anim.exit_right_to_left);
        if (flipper.getDisplayedChild() < flipper.getChildCount() - 1)
            flipper.showNext();
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

            if (editText == binding.createTravelStage3DateFrom && maxDate != 0) {
                datePickerDialog.getDatePicker().setMinDate(now.getTimeInMillis());
                datePickerDialog.getDatePicker().setMaxDate(maxDate);
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

        if (editText == binding.createTravelStage3DateTo)
            maxDate = date.getTimeInMillis();
        else
            minDate = date.getTimeInMillis();
    }


    @SuppressLint("SimpleDateFormat")
    private void setTime(TextInputEditText editText, int hoursOfDay, int minute) {
        Calendar date = Calendar.getInstance();
        date.set(0, 0, 0, hoursOfDay, minute);
        editText.setText(new SimpleDateFormat("h:mm a").format(date.getTime()));
    }


    //OTHERS----------------------------------------------------------------------------------------


    private void showSnackBar(String message, int duration) {
        getSnackBarInteractions().showSnackBar(binding.getRoot(), getActivity(), message, duration);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
