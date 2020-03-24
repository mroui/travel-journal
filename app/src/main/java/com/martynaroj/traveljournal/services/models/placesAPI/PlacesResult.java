package com.martynaroj.traveljournal.services.models.placesAPI;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PlacesResult implements Serializable {

    @SerializedName("results")
    private List<Place> places = new ArrayList<>();

    @SerializedName("status")
    private String status;


    public List<Place> getPlaces() {
        return places;
    }

    public void setPlaces(List<Place> places) {
        this.places = places;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
