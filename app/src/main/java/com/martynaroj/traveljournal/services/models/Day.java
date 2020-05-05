package com.martynaroj.traveljournal.services.models;

import android.annotation.SuppressLint;

import androidx.annotation.Nullable;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.library.baseAdapters.BR;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.martynaroj.traveljournal.view.others.enums.Emoji;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

@IgnoreExtraProperties
public class Day extends BaseObservable implements Serializable {

    private String id;
    private long date;
    private Integer rate;
    private List<Photo> photos;
    private List<Note> notes;
    private List<Place> places;
    private List<Expense> expenses;


    private Day() {
        photos = new ArrayList<>();
        notes = new ArrayList<>();
        places = new ArrayList<>();
        expenses = new ArrayList<>();
        rate = Emoji.NORMAL.ordinal();
    }


    public Day(String id, long date) {
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
    public long getDate() {
        return date;
    }


    public void setDate(long date) {
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
    public List<Expense> getExpenses() {
        return expenses;
    }


    public void setExpenses(List<Expense> expenses) {
        this.expenses = expenses;
        notifyPropertyChanged(BR.expenses);
    }


    @Exclude
    public List<Note> getAllSortedNotes() {
        List<Note> allNote = new ArrayList<>();
        allNote.addAll(places);
        allNote.addAll(photos);
        allNote.addAll(notes);
        Collections.sort(allNote);
        return allNote;
    }


    @Exclude
    public String getDateString() {
        Calendar cdate = Calendar.getInstance();
        cdate.setTimeInMillis(date);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        return format.format(cdate.getTime());
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

    public static class PackingCategory extends BaseObservable implements Serializable {

        private String name;
        private List<Itinerary.PackingItem> items;

        public PackingCategory() {
        }


        public PackingCategory(String name) {
            this.name = name;
            items = new ArrayList<>();
        }

        @Bindable
        public String getName() {
            return name;
        }

        @Bindable
        public List<Itinerary.PackingItem> getItems() {
            return items;
        }

        public void addItem(Itinerary.PackingItem item) {
            items.add(item);
        }

    }
}
