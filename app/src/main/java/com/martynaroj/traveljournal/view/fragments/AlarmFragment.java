package com.martynaroj.traveljournal.view.fragments;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.FragmentAlarmBinding;
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.view.others.classes.NotificationBroadcast;
import com.martynaroj.traveljournal.view.others.classes.RippleDrawable;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.MODE_PRIVATE;

public class AlarmFragment extends BaseFragment implements View.OnClickListener {

    private FragmentAlarmBinding binding;
    private Intent broadcastIntent;

    public static AlarmFragment newInstance() {
        return new AlarmFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAlarmBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initBroadcastIntent();
        checkBroadcast();
        createNotificationChannel();
        setListeners();
        checkPermissionsDialog();

        return view;
    }


    private void initTimer(long alarmTime) {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Calendar now = Calendar.getInstance();
                if (alarmTime < now.getTimeInMillis()) {
                    binding.setIsBroadcastWorking(false);
                    timer.cancel();
                }
            }
        }, 0, 1);
    }


    private void initBroadcastIntent() {
        broadcastIntent = new Intent(getContext(), NotificationBroadcast.class);
    }


    private void checkBroadcast() {
        boolean isBroadcastWorking = (PendingIntent.getBroadcast(getContext(), Constants.RC_BROADCAST,
                broadcastIntent, PendingIntent.FLAG_NO_CREATE) != null);
        binding.setIsBroadcastWorking(isBroadcastWorking);
        if (isBroadcastWorking) {
            getAlarmSet();
        }
    }


    private void getAlarmSet() {
        if (getContext() != null) {
            SharedPreferences prefs = getContext().getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE);

            String alarmNote = prefs.getString(Constants.ALARM_NOTE, "");
            Calendar alarmDate = Calendar.getInstance();
            alarmDate.setTimeInMillis(prefs.getLong(Constants.ALARM_TIME, 0));

            initTimer(alarmDate.getTimeInMillis());
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
            if (alarmManager != null) alarmManager.cancel(pendingIntent);
        }
    }


    private void saveAlarmTimeRemaining(long time) {
        if (getContext() != null) {
            SharedPreferences prefs = getContext().getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE);
            prefs.edit().putLong(Constants.ALARM_TIME, time).apply();
        }
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && getContext() != null) {
            NotificationChannel channel = new NotificationChannel(
                    Constants.CHANNEL_ID,
                    Constants.CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(getResources().getString(R.string.alarm_channel_info));
            NotificationManager notificationManager = getContext().getSystemService(NotificationManager.class);
            if (notificationManager != null) notificationManager.createNotificationChannel(channel);
        }
    }


    private void checkPermissionsDialog() {
        if (getContext() != null) {
            SharedPreferences prefs = getContext().getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE);
            if (!prefs.getBoolean(Constants.ALARM_DIALOG, false)
                    && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                showPermissionsDialog();
            }
        }
    }


    private void setListeners() {
        binding.alarmArrowButton.setOnClickListener(this);
        binding.alarmSetButton.setOnClickListener(this);
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
        }
    }


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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                datePicker.setOnDateChangedListener(
                        (datePicker1, i, i1, i2) -> errorMessage.setVisibility(View.GONE)
                );
                timePicker.setOnTimeChangedListener(
                        (timePicker1, i, i1) -> errorMessage.setVisibility(View.GONE)
                );
            }
            datePicker.setMinDate(new Date().getTime());
            buttonPositive.setOnClickListener(v -> setNewAlarm(dialog, datePicker, timePicker));
            buttonNegative.setOnClickListener(v -> dialog.dismiss());

            dialog.show();
        }
    }


    private void setNewAlarm(Dialog dialog, DatePicker datePicker, TimePicker timePicker) {
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
            saveAlarmTimeRemaining(dateEnd.getTimeInMillis());
            setNotificationBroadcast(note, timeDifference);
            dialog.dismiss();
        }
    }


    private void setNotificationBroadcast(String note, Long time) {
        if (getContext() != null) {
            broadcastIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            broadcastIntent.putExtra(Constants.ALARM_DESC, note);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    getContext(),
                    Constants.RC_BROADCAST,
                    broadcastIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );
            AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null)
                alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + time, pendingIntent);
            showSnackBar(getResources().getString(R.string.messages_alarm_set_success), Snackbar.LENGTH_SHORT);
            checkBroadcast();
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


    private void saveShownAlarmDialog() {
        if (getContext() != null) {
            SharedPreferences.Editor editor = getContext().getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE).edit();
            editor.putBoolean(Constants.ALARM_DIALOG, true);
            editor.apply();
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
