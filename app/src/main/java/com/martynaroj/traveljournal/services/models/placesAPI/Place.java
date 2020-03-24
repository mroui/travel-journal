package com.martynaroj.traveljournal.services.models.placesAPI;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Place implements Serializable {

    @SerializedName("name")
    private String name;

    @SerializedName("geometry")
    private Geometry geometry;

    @SerializedName("vicinity")
    private String vicinity;

    @SerializedName("rating")
    private Double rating;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public String getVicinity() {
        return vicinity;
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

}
