package com.martynaroj.traveljournal.services.models;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.martynaroj.traveljournal.BR;

import java.io.Serializable;

@IgnoreExtraProperties
public class Reservation extends BaseObservable implements Serializable {

    private String id;
    private String type;
    private String file;
    private String contact;


    public Reservation() {
    }

    public Reservation(String id, String type, String file, String contact) {
        this.id = id;
        this.type = type;
        this.file = file;
        this.contact = contact;
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
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
        notifyPropertyChanged(BR.type);
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
    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
        notifyPropertyChanged(BR.contact);
    }

    @Exclude
    public boolean isContactEmpty() {
        return contact == null || contact.equals("");
    }

}
