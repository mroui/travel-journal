package com.martynaroj.traveljournal.services.models;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.library.baseAdapters.BR;

import com.google.firebase.Timestamp;

import java.io.Serializable;

public class Note extends BaseObservable implements Serializable {

    private Timestamp date;
    private String description;


    Note() {
    }


    Note(Timestamp date, String description) {
        this.date = date;
        this.description = description;
    }


    @Bindable
    public Timestamp getDate() {
        return date;
    }


    public void setDate(Timestamp date) {
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

}
