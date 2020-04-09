package com.martynaroj.traveljournal.services.models;

import androidx.databinding.Bindable;
import androidx.databinding.library.baseAdapters.BR;

import java.io.Serializable;

public class Place extends Note implements Serializable {

    private String address;
    private Integer rate;


    public Place() {
    }


    public Place(String description, String address, Integer rate) {
        super(description);
        this.address = address;
        this.rate = rate;
    }


    @Bindable
    public String getAddress() {
        return address;
    }


    public void setAddress(String address) {
        this.address = address;
        notifyPropertyChanged(BR.address);
    }


    @Bindable
    public Integer getRate() {
        return rate;
    }


    public void setRate(Integer rate) {
        this.rate = rate;
        notifyPropertyChanged(BR.rate);
    }

}
