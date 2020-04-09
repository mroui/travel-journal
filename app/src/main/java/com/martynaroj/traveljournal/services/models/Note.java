package com.martynaroj.traveljournal.services.models;

import android.annotation.SuppressLint;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.library.baseAdapters.BR;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Note extends BaseObservable implements Serializable {

    private long date;
    private String description;


    Note() {
        this.date = System.currentTimeMillis();
    }


    Note(String description) {
        this();
        this.description = description;
    }


    @Bindable
    public long getDate() {
        return date;
    }


    public void setDate(long date) {
        this.date = date;
        notifyPropertyChanged(BR.date);
    }


    @Bindable
    public String getDescription() {
        return description;
    }


    public void setDescription(String description) {
        this.description = description;
        notifyPropertyChanged(BR.description);
    }


    @SuppressLint("SimpleDateFormat")
    @Exclude
    public String getDateTimeString() {
        return new SimpleDateFormat("dd.MM.yyyy, hh:mm a").format(new Date(date));
    }

}
