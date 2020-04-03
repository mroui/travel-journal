package com.martynaroj.traveljournal.services.models.weatherAPI;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.google.gson.annotations.SerializedName;
import com.martynaroj.traveljournal.BR;

import java.io.Serializable;
import java.text.DecimalFormat;

public class Main extends BaseObservable implements Serializable {

    @SerializedName("temp")
    private Double temp;

    @SerializedName("temp_min")
    private Double tempMin;

    @SerializedName("temp_max")
    private Double tempMax;

    @SerializedName("pressure")
    private Integer pressure;

    @SerializedName("humidity")
    private Integer humidity;


    @Bindable
    public Double getTemp() {
        return temp;
    }

    public void setTemp(Double temp) {
        this.temp = temp;
        notifyPropertyChanged(BR.temp);
    }

    @Bindable
    public Double getTempMin() {
        return tempMin;
    }

    public void setTempMin(Double tempMin) {
        this.tempMin = tempMin;
        notifyPropertyChanged(BR.tempMin);
    }

    @Bindable
    public Double getTempMax() {
        return tempMax;
    }

    public void setTempMax(Double tempMax) {
        this.tempMax = tempMax;
        notifyPropertyChanged(BR.tempMax);
    }

    @Bindable
    public Integer getPressure() {
        return pressure;
    }

    public void setPressure(Integer pressure) {
        this.pressure = pressure;
        notifyPropertyChanged(BR.pressure);
    }

    @Bindable
    public Integer getHumidity() {
        return humidity;
    }

    public void setHumidity(Integer humidity) {
        this.humidity = humidity;
        notifyPropertyChanged(BR.humidity);
    }

    private String getTempInUnit(Double temp, boolean tempUnits) {
        DecimalFormat decimalFormat = new DecimalFormat("#0");
        return tempUnits
                ? decimalFormat.format(temp - 273.15D)
                : decimalFormat.format((temp * 9D/5D) - 459.67D);
    }

    private String getProperDegrees(boolean units) {
        return units ? "°C" : "°F";
    }

    public String getTempDegrees(Double temp, boolean tempUnits) {
        return getTempInUnit(temp, tempUnits) + getProperDegrees(tempUnits);
    }

    public String getGeneralTempDegrees(boolean tempUnits) {
        return getTempInUnit(this.temp, tempUnits) + getProperDegrees(tempUnits);
    }

    public String getMinMaxTemp(boolean tempUnits) {
        if (this.tempMin.equals(this.tempMax)) {
            return getTempInUnit(this.tempMin, tempUnits) + " " + getProperDegrees(tempUnits);
        } else {
            return getTempInUnit(this.tempMin, tempUnits)
                    + " - "
                    + getTempInUnit(this.tempMax, tempUnits)
                    + " "
                    + getProperDegrees(tempUnits);
        }
    }

    public String getPressureString() {
        return this.pressure + " hPa";
    }

    public String getHumidityString() {
        return this.humidity + " %";
    }

    public String getUniversalTempString() {
        return getGeneralTempDegrees(false) + " / " + getGeneralTempDegrees(true);
    }

}
