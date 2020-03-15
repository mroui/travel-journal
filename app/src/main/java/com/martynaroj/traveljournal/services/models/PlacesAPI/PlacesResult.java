package com.martynaroj.traveljournal.services.models.PlacesAPI;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PlacesResult implements Serializable {

    @SerializedName("htmlAttributions")
    private List<Object> htmlAttributions = new ArrayList<>();

    @SerializedName("nextPageToken")
    private String nextPageToken;

    @SerializedName("results")
    private List<Place> places = new ArrayList<>();

    @SerializedName("status")
    private String status;

    public List<Object> getHtmlAttributions() {
        return htmlAttributions;
    }

    public void setHtmlAttributions(List<Object> htmlAttributions) {
        this.htmlAttributions = htmlAttributions;
    }

    public String getNextPageToken() {
        return nextPageToken;
    }

    public void setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }

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
