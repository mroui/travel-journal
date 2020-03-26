package com.martynaroj.traveljournal.view.others.classes;

import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.widget.DatePicker;
import android.widget.NumberPicker;
import android.widget.TimePicker;

import java.lang.reflect.Field;

public abstract class PickerColorize {

    public static void colorizeDatePicker(DatePicker datePicker, int color) {
        Resources system = Resources.getSystem();
        int dayId = system.getIdentifier("day", "id", "android");
        int monthId = system.getIdentifier("month", "id", "android");
        int yearId = system.getIdentifier("year", "id", "android");

        NumberPicker dayPicker = datePicker.findViewById(dayId);
        NumberPicker monthPicker = datePicker.findViewById(monthId);
        NumberPicker yearPicker = datePicker.findViewById(yearId);

        setDividerColor(dayPicker, color);
        setDividerColor(monthPicker, color);
        setDividerColor(yearPicker, color);
    }

    public static void colorizeTimePicker(TimePicker timePicker, int color){
        Resources system = Resources.getSystem();
        int hourId = system.getIdentifier("hour", "id", "android");
        int minuteId = system.getIdentifier("minute", "id", "android");
        int amPmId = system.getIdentifier("amPm", "id", "android");

        NumberPicker hourPicker = timePicker.findViewById(hourId);
        NumberPicker minutePicker = timePicker.findViewById(minuteId);
        NumberPicker amPmPicker = timePicker.findViewById(amPmId);

        setDividerColor(hourPicker, color);
        setDividerColor(minutePicker, color);
        setDividerColor(amPmPicker, color);
    }

    private static void setDividerColor(NumberPicker picker, int color) {
        if (picker == null)
            return;
        final int count = picker.getChildCount();
        for (int i = 0; i < count; i++) {
            try {
                Field dividerField = picker.getClass().getDeclaredField("mSelectionDivider");
                dividerField.setAccessible(true);
                ColorDrawable colorDrawable = new ColorDrawable(color);
                dividerField.set(picker, colorDrawable);
                picker.invalidate();
            } catch (Exception ignored) {
            }
        }
    }

}
