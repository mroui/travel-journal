package com.martynaroj.traveljournal.services.models;

import androidx.databinding.Bindable;

import com.google.firebase.Timestamp;
import com.martynaroj.traveljournal.BR;

import java.io.Serializable;

public class Photo extends Note implements Serializable {

    private String src;


    public Photo() {
    }


    public Photo(Timestamp date, String src, String description) {
        super(date, description);
        this.src = src;
    }


    @Bindable
    public String getSrc() {
        return src;
    }


    public void setSrc(String src) {
        this.src = src;
        notifyPropertyChanged(BR.src);
    }

}
