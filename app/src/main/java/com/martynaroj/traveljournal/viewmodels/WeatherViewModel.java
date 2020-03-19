package com.martynaroj.traveljournal.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.google.android.gms.maps.model.LatLng;
import com.martynaroj.traveljournal.services.models.weatherAPI.WeatherForecastResult;
import com.martynaroj.traveljournal.services.models.weatherAPI.WeatherResult;
import com.martynaroj.traveljournal.services.respositories.WeatherRepository;

public class WeatherViewModel extends AndroidViewModel {

    private LiveData<WeatherResult> weatherResultLiveData;
    private LiveData<WeatherForecastResult> weatherForecastResultLiveData;
    private WeatherRepository weatherRepository;

    public WeatherViewModel(@NonNull Application application) {
        super(application);
        weatherRepository = new WeatherRepository(application.getApplicationContext());
    }

    public void getWeather(LatLng latLng) {
        weatherResultLiveData = weatherRepository.getWeatherResult(latLng);
    }

    public LiveData<WeatherResult> getWeatherResultData() {
        return weatherResultLiveData;
    }

    public void getWeatherForecast(LatLng latLng) {
        weatherForecastResultLiveData = weatherRepository.getWeatherForecastResult(latLng);
    }

    public LiveData<WeatherForecastResult> getWeatherForecastResultData() {
        return weatherForecastResultLiveData;
    }

}