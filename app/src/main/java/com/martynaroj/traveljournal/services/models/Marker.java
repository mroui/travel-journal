package com.martynaroj.traveljournal.services.models;

import androidx.annotation.Nullable;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.library.baseAdapters.BR;

import java.io.Serializable;

public class Marker extends BaseObservable implements Serializable {

    private String id;
    private String description;
    private float color;
    private double latitude;
    private double longitude;


    public Marker() {
    }

    public Marker(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Marker(String description, float color, double latitude, double longitude) {
        this.description = description;
        this.color = color;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Bindable
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        notifyPropertyChanged(BR.id);
    }

    @Bindable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        notifyPropertyChanged(BR.description);
    }

    @Bindable
    public float getColor() {
        return color;
    }

    public void setColor(float color) {
        this.color = color;
        notifyPropertyChanged(BR.color);
    }

    @Bindable
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
        notifyPropertyChanged(BR.latitude);
    }

    @Bindable
    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
        notifyPropertyChanged(BR.longitude);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        try {
            if (obj == null || getClass() != obj.getClass())
                return false;
            Marker m = (Marker) obj;
            return (latitude == m.latitude) && (longitude == m.longitude);
        } catch (Exception ex) {
            return false;
        }
    }

}
