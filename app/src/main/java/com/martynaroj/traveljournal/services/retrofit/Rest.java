package com.martynaroj.traveljournal.services.retrofit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class Rest {

    private static RestInterface placesService, weatherService;

    private Rest() {
    }

    public static RestInterface getPlacesService() {
        return placesService;
    }

    public static RestInterface getWeatherService() {
        return weatherService;
    }

    private static Retrofit init(String url) {
        Gson gson = new GsonBuilder().create();
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        return new Retrofit
                .Builder()
                .baseUrl(url)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    public static void initPlaces() {
        placesService = init("https://maps.googleapis.com/maps/api/").create(RestInterface.class);
    }

    public static void initWeather() {
        weatherService = init("https://api.openweathermap.org/data/2.5/").create(RestInterface.class);
    }

}