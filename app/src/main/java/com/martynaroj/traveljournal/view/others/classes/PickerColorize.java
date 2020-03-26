package com.martynaroj.traveljournal.view.others.classes;

import android.app.TimePickerDialog;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.widget.DatePicker;
import android.widget.NumberPicker;
import android.widget.TimePicker;

import java.lang.reflect.Field;

public abstract class PickerColorize {


    public static void colorizeDatePicker(DatePicker datePicker, int color) {
        setDividerColor(datePicker.findViewById(getId("day")), color);
        setDividerColor(datePicker.findViewById(getId("month")), color);
        setDividerColor(datePicker.findViewById(getId("year")), color);
    }


    public static void colorizeTimePicker(TimePicker timePicker, int color) {
        setDividerColor(timePicker.findViewById(getId("hour")), color);
        setDividerColor(timePicker.findViewById(getId("minute")), color);
        setDividerColor(timePicker.findViewById(getId("amPm")), color);
    }


    public static void colorizeTimePickerDialog(TimePickerDialog timePickerDialog, int color) {
        setDividerColor(timePickerDialog.findViewById(getId("hour")), color);
        setDividerColor(timePickerDialog.findViewById(getId("minute")), color);
        setDividerColor(timePickerDialog.findViewById(getId("amPm")), color);
    }


    private static int getId(String name) {
        return Resources.getSystem().getIdentifier(name, "id", "android");
    }


    private static void setDividerColor(NumberPicker picker, int color) {
        if (picker == null)
            return;
        for (int i = 0; i < picker.getChildCount(); i++) {
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
