package com.martynaroj.traveljournal.services.respositories;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.maps.model.LatLng;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.services.models.weatherAPI.WeatherForecastResult;
import com.martynaroj.traveljournal.services.models.weatherAPI.WeatherResult;
import com.martynaroj.traveljournal.services.retrofit.Rest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherRepository {

    private Context context;

    public WeatherRepository(Context context) {
        this.context = context;
    }

    public MutableLiveData<WeatherResult> getWeatherResult(LatLng latLng) {
        MutableLiveData<WeatherResult> weatherResultData = new MutableLiveData<>();
        Rest.getWeatherService().getWeatherByLatLon(
                String.valueOf(latLng.latitude),
                String.valueOf(latLng.longitude),
                context.getString(R.string.open_weather_api_key)
        ).enqueue(new Callback<WeatherResult>() {
            @Override
            public void onResponse(@NonNull Call<WeatherResult> call, @NonNull Response<WeatherResult> response) {
                if (response.isSuccessful())
                    weatherResultData.setValue(response.body());
            }

            @Override
            public void onFailure(@NonNull Call<WeatherResult> call, @NonNull Throwable t) {
                weatherResultData.setValue(null);
            }
        });
        return weatherResultData;
    }


    public MutableLiveData<WeatherForecastResult> getWeatherForecastResult(LatLng latLng) {
        MutableLiveData<WeatherForecastResult> weatherForecastResultData = new MutableLiveData<>();
        Rest.getWeatherService().getWeatherForecastByLatLon(
                String.valueOf(latLng.latitude),
                String.valueOf(latLng.longitude),
                context.getString(R.string.open_weather_api_key)
        ).enqueue(new Callback<WeatherForecastResult>() {
            @Override
            public void onResponse(@NonNull Call<WeatherForecastResult> call,
                                   @NonNull Response<WeatherForecastResult> response) {
                if (response.isSuccessful())
                    weatherForecastResultData.setValue(response.body());
            }

            @Override
            public void onFailure(@NonNull Call<WeatherForecastResult> call, @NonNull Throwable t) {
                weatherForecastResultData.setValue(null);
            }
        });
        return weatherForecastResultData;
    }

}