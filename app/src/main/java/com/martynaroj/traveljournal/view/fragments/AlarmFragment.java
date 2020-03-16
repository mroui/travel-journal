package com.martynaroj.traveljournal.view.fragments;

import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.FragmentAlarmBinding;
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.view.others.classes.NotificationBroadcast;
import com.martynaroj.traveljournal.view.others.classes.RippleDrawable;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;

import static android.content.Context.MODE_PRIVATE;

public class AlarmFragment extends BaseFragment implements View.OnClickListener {

    private FragmentAlarmBinding binding;
    private Intent broadcastIntent;
    private boolean isBroadcastWorking;

    public static AlarmFragment newInstance() {
        return new AlarmFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAlarmBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initBroadcastIntent();
        createNotificationChannel();

        setListeners();

        checkPermissionsDialog();

        return view;
    }


    private void initBroadcastIntent() {
        broadcastIntent = new Intent(getContext(), NotificationBroadcast.class);
        isBroadcastWorking = (PendingIntent.getBroadcast(getContext(), Constants.RC_BROADCAST,
                broadcastIntent, PendingIntent.FLAG_NO_CREATE) != null);
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

//        binding.button.setOnClickListener(view1 -> {
//            broadcastIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            broadcastIntent.putExtra(Constants.TITLE, "title");
//            broadcastIntent.putExtra(Constants.DESC, "desc");
//            PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), Constants.RC_BROADCAST, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//            AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
//            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000, pendingIntent);
//        });
//        binding.button2.setOnClickListener(view1 -> {
//            AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
//            PendingIntent pendingIntent = PendingIntent.getBroadcast(
//                    getContext(), Constants.RC_BROADCAST, broadcastIntent,
//                    PendingIntent.FLAG_UPDATE_CURRENT);
//            alarmManager.cancel(pendingIntent);
//        });
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.alarm_arrow_button:
                if (getParentFragmentManager().getBackStackEntryCount() > 0)
                    getParentFragmentManager().popBackStack();
                break;
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
                saveToSharedPreferences();
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


    private void saveToSharedPreferences() {
        if (getContext() != null) {
            SharedPreferences.Editor editor = getContext().getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE).edit();
            editor.putBoolean(Constants.ALARM_DIALOG, true);
            editor.apply();
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
