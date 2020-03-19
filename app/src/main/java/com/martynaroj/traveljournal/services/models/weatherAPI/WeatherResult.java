package com.martynaroj.traveljournal.services.models.weatherAPI;

import android.annotation.SuppressLint;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.google.gson.annotations.SerializedName;
import com.martynaroj.traveljournal.BR;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class WeatherResult extends BaseObservable implements Serializable {

    @SerializedName("weather")
    private List<Weather> weather = null;

    @SerializedName("base")
    private String base;

    @SerializedName("main")
    private Main main;

    @SerializedName("wind")
    private Wind wind;

    @SerializedName("sys")
    private Sys sys;

    @SerializedName("name")
    private String name;

    @SerializedName("timezone")
    private Integer timezone;


    @Bindable
    public List<Weather> getWeather() {
        return weather;
    }

    public void setWeather(List<Weather> weather) {
        this.weather = weather;
        notifyPropertyChanged(BR.weather);
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
    public Main getMain() {
        return main;
    }

    public void setMain(Main main) {
        this.main = main;
        notifyPropertyChanged(BR.main);
    }

    @Bindable
    public Wind getWind() {
        return wind;
    }

    public void setWind(Wind wind) {
        this.wind = wind;
        notifyPropertyChanged(BR.wind);
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
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        notifyPropertyChanged(BR.name);
    }

    public Integer getTimezone() {
        return timezone;
    }

    public void setTimezone(Integer timezone) {
        this.timezone = timezone;
    }

    @Bindable
    public String getFullName() {
        return this.name + ", " + this.sys.country;
    }

    @SuppressLint("SimpleDateFormat")
    public String getTimeString(Integer time) {
        Date date = new Date((time + (timezone - TimeZone.getDefault().getOffset(time))) * 1000L);
        return new SimpleDateFormat("hh:mm aa").format(date);
    }

}
