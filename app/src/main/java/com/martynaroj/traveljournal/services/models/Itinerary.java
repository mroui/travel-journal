package com.martynaroj.traveljournal.services.models;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.martynaroj.traveljournal.BR;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Itinerary extends BaseObservable implements Serializable {

    private String id;
    private String owner;
    private String name;
    private String image;
    private long datetimeFrom;
    private long datetimeTo;
    private String destination;
    private String description;
    private List<String> tags;
    private String file;
    private int privacy;


    private Itinerary() {
        tags = new ArrayList<>();
    }


    public Itinerary(String id, String owner, String name, String image, long datetimeFrom, long datetimeTo,
                     String destination, String description, List<String> tags, String file, int privacy) {
        this();
        this.id = id;
        this.owner = owner;
        this.name = name;
        this.image = image;
        this.datetimeFrom = datetimeFrom;
        this.datetimeTo = datetimeTo;
        this.destination = destination;
        this.description = description;
        this.tags = tags;
        this.file = file;
        this.privacy = privacy;
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
    public String getOwner() {
        return owner;
    }


    public void setOwner(String owner) {
        this.owner = owner;
        notifyPropertyChanged(BR.owner);
    }


    @Bindable
    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
        notifyPropertyChanged(BR.name);
    }


    @Bindable
    public String getImage() {
        return image;
    }


    public void setImage(String image) {
        this.image = image;
        notifyPropertyChanged(BR.image);
    }


    @Bindable
    public long getDatetimeFrom() {
        return datetimeFrom;
    }


    public void setDatetimeFrom(long datetimeFrom) {
        this.datetimeFrom = datetimeFrom;
        notifyPropertyChanged(BR.datetimeFrom);
    }


    @Bindable
    public long getDatetimeTo() {
        return datetimeTo;
    }


    public void setDatetimeTo(long datetimeTo) {
        this.datetimeTo = datetimeTo;
        notifyPropertyChanged(BR.datetimeTo);
    }


    @Bindable
    public String getDestination() {
        return destination;
    }


    public void setDestination(String destination) {
        this.destination = destination;
        notifyPropertyChanged(BR.destination);
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
    public List<String> getTags() {
        return tags;
    }


    public void setTags(List<String> tags) {
        this.tags = tags;
        notifyPropertyChanged(BR.tags);
    }


    @Bindable
    public String getFile() {
        return file;
    }


    public void setFile(String file) {
        this.file = file;
        notifyPropertyChanged(BR.file);
    }


    @Bindable
    public int getPrivacy() {
        return privacy;
    }


    public void setPrivacy(int privacy) {
        this.privacy = privacy;
        notifyPropertyChanged(BR.privacy);
    }

}