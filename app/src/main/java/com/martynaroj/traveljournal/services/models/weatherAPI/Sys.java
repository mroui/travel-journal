package com.martynaroj.traveljournal.services.models.weatherAPI;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.google.gson.annotations.SerializedName;
import com.martynaroj.traveljournal.BR;

import java.io.Serializable;

public class Sys extends BaseObservable implements Serializable {

    @SerializedName("type")
    public Integer type;

    @SerializedName("id")
    public Integer id;

    @SerializedName("message")
    public Double message;

    @SerializedName("country")
    public String country;

    @SerializedName("sunrise")
    public Integer sunrise;

    @SerializedName("sunset")
    public Integer sunset;


    @Bindable
    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
        notifyPropertyChanged(BR.type);
    }

    @Bindable
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
        notifyPropertyChanged(BR.id);
    }

    @Bindable
    public Double getMessage() {
        return message;
    }

    public void setMessage(Double message) {
        this.message = message;
        notifyPropertyChanged(BR.message);
    }

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
