package com.martynaroj.traveljournal.Services.Models;

import com.martynaroj.traveljournal.View.Others.Status;

public class DataWrapper<T> {

    private T data;
    private Status status;
    private String message;
    private boolean isNew = false;
    private boolean isAdded = false;
    private boolean isAuthenticated = false;

    public DataWrapper(T data, Status status, String message, boolean isNew, boolean isAdded) {
        this.data = data;
        this.status = status;
        this.message = message;
        this.isNew = isNew;
        this.isAdded = isAdded;
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

    public String getMessage() {
        return message;
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

    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        isAuthenticated = authenticated;
    }
}