package com.martynaroj.traveljournal.services.models;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.library.baseAdapters.BR;

import com.google.firebase.Timestamp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Day extends BaseObservable implements Serializable {

    private String id;
    private Timestamp date;
    private Integer rate;
    private List<Photo> photos;
    private List<Note> notes;
    private List<Place> places;
    private List<Cost> costs;


    private Day() {
        photos = new ArrayList<>();
        notes = new ArrayList<>();
        places = new ArrayList<>();
        costs = new ArrayList<>();
    }


    public Day(String id, Timestamp date, Integer rate, List<Photo> photos, List<Note> notes, List<Place> places) {
        this();
        this.id = id;
        this.date = date;
        this.rate = rate;
        this.photos = photos;
        this.notes = notes;
        this.places = places;
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
    public Timestamp getDate() {
        return date;
    }


    public void setDate(Timestamp date) {
        this.date = date;
        notifyPropertyChanged(BR.date);
    }


    @Bindable
    public Integer getRate() {
        return rate;
    }


    public void setRate(Integer rate) {
        this.rate = rate;
        notifyPropertyChanged(BR.rate);
    }


    @Bindable
    public List<Photo> getPhotos() {
        return photos;
    }


    public void setPhotos(List<Photo> photos) {
        this.photos = photos;
        notifyPropertyChanged(BR.photos);
    }


    @Bindable
    public List<Note> getNotes() {
        return notes;
    }


    public void setNotes(List<Note> notes) {
        this.notes = notes;
        notifyPropertyChanged(BR.notes);
    }


    @Bindable
    public List<Place> getPlaces() {
        return places;
    }


    public void setPlaces(List<Place> places) {
        this.places = places;
        notifyPropertyChanged(BR.places);
    }

}
