package com.martynaroj.traveljournal.services.models;

import com.google.android.libraries.places.api.model.Place;
import com.google.firebase.firestore.DocumentReference;
import com.martynaroj.traveljournal.view.others.enums.Privacy;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User implements Serializable {

    private String uid;
    private String username;
    private String email;
    private String photo;
    private String bio;
    private Place location;
    private List<String> preferences;
    private List<DocumentReference> friends;
    private Map<String, Integer> privacy;
    //private List<Travel> travels;

    public User() {
    }

    public User(String uid, String username, String email) {
        this.uid = uid;
        this.username = username;
        this.email = email;

        this.privacy = new HashMap<>();
        defineDefaultPrivacy();
    }

    private void defineDefaultPrivacy() {
        privacy.put(Constants.EMAIL, Privacy.PUBLIC.ordinal());
        privacy.put(Constants.LOCATION, Privacy.PUBLIC.ordinal());
        privacy.put(Constants.PREFERENCES, Privacy.PUBLIC.ordinal());
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

    public Map<String, Integer> getPrivacy() {
        return privacy;
    }

    public void setPrivacy(Map<String, Integer> privacy) {
        this.privacy = privacy;
    }
}
