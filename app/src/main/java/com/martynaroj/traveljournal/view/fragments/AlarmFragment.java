package com.martynaroj.traveljournal.view.fragments;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.FragmentAlarmBinding;
import com.martynaroj.traveljournal.services.others.NotificationBroadcast;
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.view.others.classes.PickerColorize;
import com.martynaroj.traveljournal.view.others.classes.RippleDrawable;
import com.martynaroj.traveljournal.view.others.classes.SharedPreferencesUtils;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;
import com.martynaroj.traveljournal.viewmodels.UserViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class AlarmFragment extends BaseFragment implements View.OnClickListener {

    private FragmentAlarmBinding binding;
    private UserViewModel userViewModel;
    private Intent broadcastIntent;
    private Timer timer;
    private boolean isAlarmCanceled;

    public static AlarmFragment newInstance() {
        return new AlarmFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAlarmBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initViewModels();
        observeUserChanges();

        initBroadcastIntent();
        checkBroadcast();
        createNotificationChannel();

        checkPermissionsDialog();

        setListeners();

        return view;
    }


    //INIT DATA-------------------------------------------------------------------------------------


    private void initViewModels() {
        if (getActivity() != null) {
            userViewModel = new ViewModelProvider(getActivity()).get(UserViewModel.class);
        }
    }


    private void observeUserChanges() {
        userViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user == null)
                back();
        });
    }


    private void initBroadcastIntent() {
        broadcastIntent = new Intent(getContext(), NotificationBroadcast.class);
    }


    private void checkBroadcast() {
        boolean isBroadcastWorking = (PendingIntent.getBroadcast(getContext(), Constants.RC_BROADCAST,
                broadcastIntent, PendingIntent.FLAG_NO_CREATE) != null);
        binding.setIsBroadcastWorking(isBroadcastWorking);
        if (isBroadcastWorking)
            getAlarmSet();
    }


    private void createNotificationChannel() {
        if (getContext() != null) {
            NotificationBroadcast.createNotificationChannel(getContext());
        }
    }


    private void initTimer(long alarmTime) {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Calendar now = Calendar.getInstance();
                if (alarmTime < now.getTimeInMillis() || isAlarmCanceled) {
                    if (binding != null)
                        binding.setIsBroadcastWorking(false);
                    timer.cancel();
                }
            }
        }, 0, 1);
    }


    private void checkPermissionsDialog() {
        if (!SharedPreferencesUtils.getBoolean(getContext(), Constants.ALARM_DIALOG, false)
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            showPermissionsDialog();
        }
    }


    //SHARED PREFS----------------------------------------------------------------------------------


    private void getAlarmSet() {
        if (getContext() != null) {
            String alarmNote = SharedPreferencesUtils.getString(getContext(), Constants.ALARM_NOTE, "");
            Calendar alarmDate = Calendar.getInstance();
            alarmDate.setTimeInMillis(SharedPreferencesUtils.getLong(getContext(), Constants.ALARM_TIME, 0));
            setAlarmData(alarmDate, alarmNote);
            initTimer(alarmDate.getTimeInMillis());
        }
    }


    @SuppressLint("SimpleDateFormat")
    private void setAlarmData(Calendar alarmDate, String alarmNote) {
        binding.alarmAlarmSetDate.setText(new SimpleDateFormat("dd.MM.yyyy").format(alarmDate.getTime()));
        binding.alarmAlarmSetTime.setText(new SimpleDateFormat("hh:mm a").format(alarmDate.getTime()));
        binding.alarmAlarmSetNote.setText(alarmNote);
    }


    private void saveShownAlarmDialog() {
        if (getContext() != null) {
            SharedPreferencesUtils.setBoolean(getContext(), Constants.ALARM_DIALOG, true);
        }
    }


    //ALARM-----------------------------------------------------------------------------------------


    private void setAlarm(Dialog dialog, DatePicker datePicker, TimePicker timePicker) {
        int dMin, dHour;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            dMin = timePicker.getMinute();
            dHour = timePicker.getHour();
        } else {
            dMin = timePicker.getCurrentMinute();
            dHour = timePicker.getCurrentHour();
        }
        int dDay = datePicker.getDayOfMonth();
        int dMonth = datePicker.getMonth();
        int dYear = datePicker.getYear();

        TextInputEditText editText = dialog.findViewById(R.id.dialog_alarm_note_input);
        String note = editText.getText() != null && !editText.getText().toString().isEmpty()
                ? editText.getText().toString() : "";

        Calendar dateStart = Calendar.getInstance();
        Calendar dateEnd = Calendar.getInstance();
        dateEnd.set(dYear, dMonth, dDay, dHour, dMin, 0);
        Long timeDifference = (dateEnd.getTimeInMillis() - dateStart.getTimeInMillis());

        if (timeDifference <= 0) {
            (dialog.findViewById(R.id.dialog_alarm_error)).setVisibility(View.VISIBLE);
        } else {
            isAlarmCanceled = false;
            SharedPreferencesUtils.saveAlarmSet(getContext(), dateEnd.getTimeInMillis(), note);
            setNotificationBroadcast(note, timeDifference);
            dialog.dismiss();
        }
    }


    private void setNotificationBroadcast(String note, Long time) {
        if (getContext() != null) {
            NotificationBroadcast.sendBroadcast(getContext(), broadcastIntent, time, note);
            showSnackBar(getResources().getString(R.string.messages_alarm_set_success), Snackbar.LENGTH_SHORT);
            checkBroadcast();
        }
    }


    private void cancelAlarm() {
        if (getContext() != null) {
            AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    getContext(),
                    Constants.RC_BROADCAST,
                    broadcastIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );
            if (alarmManager != null)
                alarmManager.cancel(pendingIntent);
            isAlarmCanceled = true;
        }
    }


    //LISTENERS-------------------------------------------------------------------------------------


    private void setListeners() {
        binding.alarmArrowButton.setOnClickListener(this);
        binding.alarmSetButton.setOnClickListener(this);
        binding.alarmAlarmSetCancelButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.alarm_arrow_button:
                if (getParentFragmentManager().getBackStackEntryCount() > 0)
                    getParentFragmentManager().popBackStack();
                break;
            case R.id.alarm_set_button:
                showSetAlarmDialog();
                break;
            case R.id.alarm_alarm_set_cancel_button:
                cancelAlarm();
                break;
        }
    }


    //DIALOGS---------------------------------------------------------------------------------------


    private void showSetAlarmDialog() {
        if (getContext() != null) {
            Dialog dialog = new Dialog(getContext());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(true);
            dialog.setContentView(R.layout.dialog_alarm);

            MaterialButton buttonPositive = dialog.findViewById(R.id.dialog_alarm_buttom_positive);
            MaterialButton buttonNegative = dialog.findViewById(R.id.dialog_alarm_button_negative);
            TimePicker timePicker = dialog.findViewById(R.id.dialog_alarm_time_picker);
            DatePicker datePicker = dialog.findViewById(R.id.dialog_alarm_date_picker);
            TextView errorMessage = dialog.findViewById(R.id.dialog_alarm_error);

            int color = getResources().getColor(R.color.light_gray);
            PickerColorize.colorizeDatePicker(datePicker, color);
            PickerColorize.colorizeTimePicker(timePicker, color);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                datePicker.setOnDateChangedListener(
                        (datePicker1, i, i1, i2) -> errorMessage.setVisibility(View.GONE)
                );
                timePicker.setOnTimeChangedListener(
                        (timePicker1, i, i1) -> errorMessage.setVisibility(View.GONE)
                );
            }
            datePicker.setMinDate(new Date().getTime() - 1000);
            buttonPositive.setOnClickListener(v -> setAlarm(dialog, datePicker, timePicker));
            buttonNegative.setOnClickListener(v -> dialog.dismiss());

            dialog.show();
        }
    }


    private void showPermissionsDialog() {
        if (getContext() != null) {
            Dialog dialog = new Dialog(getContext());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(true);
            dialog.setContentView(R.layout.dialog_custom);

            TextView title = dialog.findViewById(R.id.dialog_custom_title);
            TextView message = dialog.findViewById(R.id.dialog_custom_desc);
            MaterialButton buttonPositive = dialog.findViewById(R.id.dialog_custom_buttom_positive);
            MaterialButton buttonNegative = dialog.findViewById(R.id.dialog_custom_button_negative);

            title.setText(getResources().getString(R.string.dialog_alarm_perms_title));
            message.setText(getResources().getString(R.string.dialog_alarm_perms_desc));
            buttonPositive.setText(getResources().getString(R.string.dialog_button_settings));
            RippleDrawable.setRippleEffectButton(
                    buttonPositive,
                    Color.TRANSPARENT,
                    getResources().getColor(R.color.yellow_bg_lighter)
            );
            buttonPositive.setTextColor(getResources().getColor(R.color.yellow_active));
            buttonPositive.setOnClickListener(v -> {
                dialog.dismiss();
                saveShownAlarmDialog();
                openSettings();
            });
            buttonNegative.setText(getResources().getString(R.string.dialog_button_no_thanks));
            RippleDrawable.setRippleEffectButton(
                    buttonNegative,
                    Color.TRANSPARENT,
                    getResources().getColor(R.color.yellow_bg_lighter)
            );
            buttonNegative.setTextColor(getResources().getColor(R.color.yellow_active));
            buttonNegative.setOnClickListener(v -> dialog.dismiss());

            dialog.show();
        }
    }


    //OTHERS----------------------------------------------------------------------------------------


    private void openSettings() {
        if (getContext() != null) {
            Intent intent = new Intent();
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("app_package", getContext().getPackageName());
            intent.putExtra("app_uid", getContext().getApplicationInfo().uid);
            intent.putExtra("android.provider.extra.APP_PACKAGE", getContext().getPackageName());
            startActivity(intent);
        }
    }


    private void showSnackBar(String message, int duration) {
        getSnackBarInteractions().showSnackBar(binding.getRoot(), getActivity(), message, duration);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
