package com.martynaroj.traveljournal.Services.Models;

import com.martynaroj.traveljournal.View.Others.Status;

public class DataWrapper<T> {

    private T data;
    private Status error;
    private String message;

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
}