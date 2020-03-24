package com.martynaroj.traveljournal.services.models.currencyAPI;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.google.gson.annotations.SerializedName;
import com.martynaroj.traveljournal.BR;

import java.io.Serializable;
import java.util.Map;

public class CurrencyExchangeResult extends BaseObservable implements Serializable {

    @SerializedName("rates")
    private Map<String, Double> rates = null;

    @SerializedName("base")
    private String base;

    @SerializedName("date")
    private String date;


    @Bindable
    public Map<String, Double> getRates() {
        return rates;
    }

    public void setRates(Map<String, Double> rates) {
        this.rates = rates;
        notifyPropertyChanged(BR.rates);
    }

    @Bindable
    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
        notifyPropertyChanged(BR.base);
    }

    @Bindable
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
        notifyPropertyChanged(BR.date);
    }

}