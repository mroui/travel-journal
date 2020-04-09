package com.martynaroj.traveljournal.services.models;

import androidx.databinding.Bindable;

import com.martynaroj.traveljournal.BR;

import java.io.Serializable;

public class Photo extends Note implements Serializable {

    private String src;


    public Photo() {
    }


    public Photo(String src, String description) {
        super(description);
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
