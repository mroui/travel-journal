package com.martynaroj.traveljournal.services.models;

import androidx.databinding.Bindable;

import com.google.firebase.firestore.Exclude;
import com.martynaroj.traveljournal.BR;

import java.io.Serializable;
import java.text.DecimalFormat;

public class Expense extends Note implements Serializable, Comparable<Expense> {

    private String category;
    private Double amount;

    public Expense() {
    }


    public Expense(String category, Double amount) {
        super();
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


    @Exclude
    public String getAmountString() {
        return (amount > 0 ? "+" : "") + new DecimalFormat("#.00").format(amount);
    }


    @Override
    public int compareTo(Expense e) {
        return Long.compare(getDate(), e.getDate());
    }

}
