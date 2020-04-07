package com.martynaroj.traveljournal.services.models;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.library.baseAdapters.BR;

import java.io.Serializable;

public class Note extends BaseObservable implements Serializable {

    private long date;
    private String description;


    Note() {
    }


    Note(long date, String description) {
        this.date = date;
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

}
