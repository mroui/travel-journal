package com.martynaroj.traveljournal.services.models;

import com.google.android.libraries.places.api.model.Place;
import com.google.firebase.firestore.DocumentReference;

import java.io.Serializable;
import java.util.List;

public class User implements Serializable {

    private String uid;
    private String username;
    private String email;
    private String photo;
    private String bio;
    private Place location;
    private List<String> preferences;
    private List<DocumentReference> friends;
    //private List<Travel> travels;

    public User() {
    }

    public User(String uid, String username, String email) {
        this.uid = uid;
        this.username = username;
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Place getLocation() {
        return location;
    }

    public void setLocation(Place location) {
        this.location = location;
    }

    public List<String> getPreferences() {
        return preferences;
    }

    public void setPreferences(List<String> preferences) {
        this.preferences = preferences;
    }

    public List<DocumentReference> getFriends() {
        return friends;
    }

    public void setFriends(List<DocumentReference> friends) {
        this.friends = friends;
    }
}
