package com.martynaroj.traveljournal.Services.Models;

public class User {

    private String uid;
    private String username;
    private String email;
    private boolean isNew;
    private boolean isAdded;

    public User() {
    }

    public User(String uid, String username, String email, boolean isNew) {
        this.uid = uid;
        this.username = username;
        this.email = email;
        this.isNew = isNew;
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

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public boolean isAdded() {
        return isAdded;
    }

    public void setAdded(boolean added) {
        isAdded = added;
    }
}
