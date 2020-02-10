package com.martynaroj.traveljournal.services.models;

import com.martynaroj.traveljournal.view.others.enums.Status;

public class DataWrapper<T> {

    private T data;
    private Status status;
    private String message;
    private boolean isAdded = false;
    private boolean isAuthenticated = false;
    private boolean isVerified = false;

    public DataWrapper(T data, Status status, String message, boolean isAuthenticated, boolean isAdded, boolean isVerified) {
        this.data = data;
        this.status = status;
        this.message = message;
        this.isAuthenticated = isAuthenticated;
        this.isAdded = isAdded;
        this.isVerified = isVerified;
    }

    public DataWrapper(T data, Status status, String message) {
        this.data = data;
        this.status = status;
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isAdded() {
        return isAdded;
    }

    public void setAdded(boolean added) {
        isAdded = added;
    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        isAuthenticated = authenticated;
    }

    public boolean isVerified() {
        return isVerified;
    }
}