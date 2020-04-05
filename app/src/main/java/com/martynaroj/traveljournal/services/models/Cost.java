package com.martynaroj.traveljournal.services.models;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.martynaroj.traveljournal.BR;

import java.io.Serializable;

public class Cost extends BaseObservable implements Serializable {

    private String category;
    private Double amount;


    public Cost() {
    }


    public Cost(String category, Double amount) {
        this.category = category;
        this.amount = amount;
    }


    @Bindable
    public String getCategory() {
        return category;
    }


    public void setCategory(String category) {
        this.category = category;
        notifyPropertyChanged(BR.category);
    }


    @Bindable
    public Double getAmount() {
        return amount;
    }


    public void setAmount(Double amount) {
        this.amount = amount;
        notifyPropertyChanged(BR.amount);
    }

}
