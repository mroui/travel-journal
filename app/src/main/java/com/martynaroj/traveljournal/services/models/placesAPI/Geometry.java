package com.martynaroj.traveljournal.services.models.placesAPI;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Geometry implements Serializable {

    @SerializedName("location")
    private Location location;


    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

}
