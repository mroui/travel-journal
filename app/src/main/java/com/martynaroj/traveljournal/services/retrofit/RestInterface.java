package com.martynaroj.traveljournal.services.retrofit;

import com.martynaroj.traveljournal.services.models.currencyAPI.CurrencyExchangeResult;
import com.martynaroj.traveljournal.services.models.placesAPI.PlacesResult;
import com.martynaroj.traveljournal.services.models.translatorAPI.DetectLangResult;
import com.martynaroj.traveljournal.services.models.translatorAPI.LangsResult;
import com.martynaroj.traveljournal.services.models.translatorAPI.TranslationResult;
import com.martynaroj.traveljournal.services.models.weatherAPI.WeatherForecastResult;
import com.martynaroj.traveljournal.services.models.weatherAPI.WeatherResult;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RestInterface {

    @GET("place/nearbysearch/json")
    Call<PlacesResult> getNearbyPlaces(
            @Query("location") String location,
            @Query("radius") int radius,
            @Query("type") String type,
            @Query("key") String key
    );

    @GET("weather")
    Call<WeatherResult> getWeatherByLatLon(
            @Query("lat") String lat,
            @Query("lon") String lon,
            @Query("appid") String appid
    );

    @GET("forecast")
    Call<WeatherForecastResult> getWeatherForecastByLatLon(
            @Query("lat") String lat,
            @Query("lon") String lon,
            @Query("appid") String appid
    );

    @GET("getLangs")
    Call<LangsResult> getTranslatorLangs(
            @Query("key") String key,
            @Query("ui") String ui
    );

    @GET("detect")
    Call<DetectLangResult> detectLang(
            @Query("key") String key,
            @Query("text") String text
    );

    @GET("translate")
    Call<TranslationResult> getTranslation(
            @Query("key") String key,
            @Query("text") String text,
            @Query("lang") String lang
    );

    @GET("latest")
    Call<CurrencyExchangeResult> getLatestCurrencyExchange(
            @Query("base") String base,
            @Query("symbols") String symbols
    );

}