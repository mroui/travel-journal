package com.martynaroj.traveljournal.services.models;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;
import com.martynaroj.traveljournal.BR;

import java.io.Serializable;

public class Notification  extends BaseObservable implements Serializable {

    private String id;
    private String idFrom;
    private String idTo;
    private Integer type;
    @ServerTimestamp
    private Timestamp timestamp;

    public Notification() {
    }

    public Notification(String idFrom, String idTo, Integer type) {
        this.idFrom = idFrom;
        this.idTo = idTo;
        this.type = type;
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
    public String getIdFrom() {
        return idFrom;
    }

    public void setIdFrom(String idFrom) {
        this.idFrom = idFrom;
        notifyPropertyChanged(BR.idFrom);
    }

    @Bindable
    public String getIdTo() {
        return idTo;
    }

    public void setIdTo(String idTo) {
        this.idTo = idTo;
        notifyPropertyChanged(BR.idTo);
    }

    @Bindable
    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
        notifyPropertyChanged(BR.type);
    }

    @Bindable
    public Timestamp getTimestamp() {
        return timestamp;
    }

}
