package com.martynaroj.traveljournal.services.models;

import androidx.annotation.Nullable;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.library.baseAdapters.BR;

import com.google.firebase.Timestamp;
import com.martynaroj.traveljournal.view.others.enums.Emoji;

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
        rate = Emoji.NORMAL.ordinal();
    }


    public Day(String id, Timestamp date) {
        this();
        this.id = id;
        this.date = date;
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


    @Bindable
    public List<Cost> getCosts() {
        return costs;
    }


    public void setCosts(List<Cost> costs) {
        this.costs = costs;
        notifyPropertyChanged(BR.costs);
    }


    @Override
    public boolean equals(@Nullable Object obj) {
        try {
            if (obj == null || getClass() != obj.getClass())
                return false;
            Day d = (Day) obj;
            return (id.equals(d.getId()));
        } catch (Exception ex) {
            return false;
        }
    }

}
