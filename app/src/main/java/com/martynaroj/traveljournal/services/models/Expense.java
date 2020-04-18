package com.martynaroj.traveljournal.services.models;

import androidx.annotation.Nullable;
import androidx.databinding.Bindable;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.martynaroj.traveljournal.BR;

import java.io.Serializable;
import java.text.DecimalFormat;

@IgnoreExtraProperties
public class Expense extends Note implements Serializable {

    private String category;
    private Double amount;
    @Exclude
    private String description;


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
        return (amount > 0 ? "+" : "") + new DecimalFormat("0.00").format(amount);
    }


    @Override
    @Exclude
    public String getDescription() {
        return super.getDescription();
    }


    @Override
    public boolean equals(@Nullable Object obj) {
        try {
            if (obj == null || getClass() != obj.getClass())
                return false;
            Expense e = (Expense) obj;
            return (getDate() == e.getDate()
                    && category.equals(e.category)
                    && amount.equals(e.getAmount()));
        } catch (Exception ex) {
            return false;
        }
    }
}
