package com.martynaroj.traveljournal.services.models.weatherAPI;

import android.annotation.SuppressLint;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.google.gson.annotations.SerializedName;
import com.martynaroj.traveljournal.BR;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class WeatherList extends BaseObservable implements Serializable {

    @SerializedName("dt")
    private Integer dt;

    @SerializedName("main")
    private Main main;

    @SerializedName("weather")
    private java.util.List<Weather> weather = null;

    @SerializedName("sys")
    private Sys sys;

    @SerializedName("dt_txt")
    private String dtTxt;


    @Bindable
    public Integer getDt() {
        return dt;
    }

    public void setDt(Integer dt) {
        this.dt = dt;
        notifyPropertyChanged(BR.dt);
    }

    @Bindable
    public Main getMain() {
        return main;
    }

    public void setMain(Main main) {
        this.main = main;
        notifyPropertyChanged(BR.main);
    }

    @Bindable
    public List<Weather> getWeather() {
        return weather;
    }

    public void setWeather(List<Weather> weather) {
        this.weather = weather;
        notifyPropertyChanged(BR.weather);
    }

    @Bindable
    public Sys getSys() {
        return sys;
    }

    public void setSys(Sys sys) {
        this.sys = sys;
        notifyPropertyChanged(BR.sys);
    }

    @Bindable
    public String getDtTxt() {
        return dtTxt;
    }

    public void setDtTxt(String dtTxt) {
        this.dtTxt = dtTxt;
        notifyPropertyChanged(BR.dtTxt);
    }

    @SuppressLint("SimpleDateFormat")
    public String getDateDayOfWeekString(int days) {
        Calendar date = Calendar.getInstance();
        date.add(Calendar.DATE, days);
        return new SimpleDateFormat("EE").format(date.getTime());
    }

}