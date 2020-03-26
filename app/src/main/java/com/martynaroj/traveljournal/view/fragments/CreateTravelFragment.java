package com.martynaroj.traveljournal.view.fragments;


import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialStyledDatePickerDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.FragmentCreateTravelBinding;
import com.martynaroj.traveljournal.services.models.User;
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
        observeUserChanges();

        setListeners();

        return view;
    }


    private void initContentData() {
         minDate = Calendar.getInstance().getTimeInMillis();
    }


    //INIT DATA-------------------------------------------------------------------------------------


    private void initViewModels() {
        if (getActivity() != null) {
            userViewModel = new ViewModelProvider(getActivity()).get(UserViewModel.class);
        }
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
        setViewFlipperListeners();
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
