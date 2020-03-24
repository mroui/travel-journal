package com.martynaroj.traveljournal.services.models.weatherAPI;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.google.gson.annotations.SerializedName;
import com.martynaroj.traveljournal.BR;

import java.io.Serializable;

public class Sys extends BaseObservable implements Serializable {

    @SerializedName("country")
    private String country;

    @SerializedName("sunrise")
    private Integer sunrise;

    @SerializedName("sunset")
    private Integer sunset;


    @Bindable
    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
        notifyPropertyChanged(BR.country);
    }

    @Bindable
    public Integer getSunrise() {
        return sunrise;
    }

    public void setSunrise(Integer sunrise) {
        this.sunrise = sunrise;
        notifyPropertyChanged(BR.sunrise);
    }

    @Bindable
    public Integer getSunset() {
        return sunset;
    }

    public void setSunset(Integer sunset) {
        this.sunset = sunset;
        notifyPropertyChanged(BR.sunset);
    }

}
