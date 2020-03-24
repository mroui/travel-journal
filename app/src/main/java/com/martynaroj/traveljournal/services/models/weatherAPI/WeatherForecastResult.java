package com.martynaroj.traveljournal.services.models.weatherAPI;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.google.gson.annotations.SerializedName;
import com.martynaroj.traveljournal.BR;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class WeatherForecastResult extends BaseObservable implements Serializable {

    @SerializedName("list")
    private java.util.List<WeatherList> list = null;


    @Bindable
    public List<WeatherList> getList() {
        return list;
    }

    public void setList(List<WeatherList> list) {
        this.list = list;
        notifyPropertyChanged(BR.list);
    }

    public WeatherList getForecastForDay(int days) {
        Calendar date = Calendar.getInstance();
        WeatherList result;

        Calendar futureDate = Calendar.getInstance();
        futureDate.add(Calendar.DATE, days);

        for(WeatherList weather : this.list) {
            date.setTime(new Date(weather.getDt()*1000L));
            if (date.get(Calendar.DAY_OF_MONTH) == futureDate.get(Calendar.DAY_OF_MONTH)
            && date.get(Calendar.HOUR_OF_DAY) >= 12 && date.get(Calendar.HOUR_OF_DAY) <= 15) {
                result = weather;
                return result;
            }
        }
        return null;
    }

}