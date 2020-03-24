package com.martynaroj.traveljournal.services.models;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.google.firebase.Timestamp;
import com.martynaroj.traveljournal.BR;

import java.io.Serializable;
import java.util.List;

public class Travel extends BaseObservable implements Serializable {

    private String id;
    private String owner;
    private String image;
    private String name;
    private Timestamp datetimeFrom;
    private Timestamp datetimeTo;
    private String address;
    private String transport;
    private String accommodation;
    private Double budget;
    private List<String> tags;


    public Travel() {
    }

    public Travel(String owner, String image, String name, Timestamp datetimeFrom,
                  Timestamp datetimeTo, String address, String transport, String accommodation,
                  Double budget, List<String> tags) {
        this.owner = owner;
        this.image = image;
        this.name = name;
        this.datetimeFrom = datetimeFrom;
        this.datetimeTo = datetimeTo;
        this.address = address;
        this.transport = transport;
        this.accommodation = accommodation;
        this.budget = budget;
        this.tags = tags;
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
    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
        notifyPropertyChanged(BR.image);
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
    public Timestamp getDatetimeFrom() {
        return datetimeFrom;
    }

    public void setDatetimeFrom(Timestamp datetimeFrom) {
        this.datetimeFrom = datetimeFrom;
        notifyPropertyChanged(BR.datetimeFrom);
    }

    @Bindable
    public Timestamp getDatetimeTo() {
        return datetimeTo;
    }

    public void setDatetimeTo(Timestamp datetimeTo) {
        this.datetimeTo = datetimeTo;
        notifyPropertyChanged(BR.datetimeTo);
    }

    @Bindable
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
        notifyPropertyChanged(BR.address);
    }

    @Bindable
    public String getTransport() {
        return transport;
    }

    public void setTransport(String transport) {
        this.transport = transport;
        notifyPropertyChanged(BR.transport);
    }

    @Bindable
    public String getAccommodation() {
        return accommodation;
    }

    public void setAccommodation(String accommodation) {
        this.accommodation = accommodation;
        notifyPropertyChanged(BR.accommodation);
    }

    @Bindable
    public Double getBudget() {
        return budget;
    }

    public void setBudget(Double budget) {
        this.budget = budget;
        notifyPropertyChanged(BR.budget);
    }

    @Bindable
    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
        notifyPropertyChanged(BR.tags);
    }

}
