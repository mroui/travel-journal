package com.martynaroj.traveljournal.Services.Models;

import com.martynaroj.traveljournal.View.Others.Status;

public class DataWrapper<T> {

    private T data;
    private Status error;
    private String message;
    private boolean isNew = false;
    private boolean isAdded = false;

    public DataWrapper(T data, Status error, String message, boolean isNew, boolean isAdded) {
        this.data = data;
        this.error = error;
        this.message = message;
        this.isNew = isNew;
        this.isAdded = isAdded;
    }

    public DataWrapper(T data, Status error, String message) {
        this.data = data;
        this.error = error;
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public Status getError() {
        return error;
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
}