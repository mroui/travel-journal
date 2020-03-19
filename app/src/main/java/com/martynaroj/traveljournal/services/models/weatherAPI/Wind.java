package com.martynaroj.traveljournal.services.models.weatherAPI;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.google.gson.annotations.SerializedName;
import com.martynaroj.traveljournal.BR;

import java.io.Serializable;
import java.text.DecimalFormat;

public class Wind extends BaseObservable implements Serializable {

    @SerializedName("speed")
    private Double speed;


    @Bindable
    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
        notifyPropertyChanged(BR.speed);
    }

    private Double convertMSToKmH(Double variable) {
        return variable * 3.6D;
    }

    public String getSpeedString() {
        return new DecimalFormat("#0.0").format(convertMSToKmH(this.speed)) + " km/h";
    }

}
