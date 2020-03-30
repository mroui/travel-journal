package com.martynaroj.traveljournal.view.others.classes;

import android.content.Context;

import com.martynaroj.traveljournal.view.others.interfaces.Constants;

import static android.content.Context.MODE_PRIVATE;

public abstract class SharedPreferencesUtils {


    public static boolean getBoolean(Context context, String key, boolean value) {
        if (context != null)
            return context.getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE).getBoolean(key, value);
        else
            return false;
    }


    public static void setBoolean(Context context, String key, boolean value) {
        if (context != null)
            context.getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE)
                    .edit()
                    .putBoolean(key, value)
                    .apply();
    }


    public static String getString(Context context, String key, String value) {
        if (context != null)
            return context.getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE).getString(key, value);
        else
            return "";
    }


    public static long getLong(Context context, String key, long value) {
        if (context != null)
            return context.getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE).getLong(key, value);
        else
            return 0;
    }


    public static void saveAlarmSet(Context context, long time, String note) {
        if (context != null)
            context.getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE)
                    .edit()
                    .putLong(Constants.ALARM_TIME, time)
                    .putString(Constants.ALARM_NOTE, note)
                    .apply();
    }

}
