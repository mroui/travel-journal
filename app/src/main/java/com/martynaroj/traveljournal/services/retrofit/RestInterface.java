package com.martynaroj.traveljournal.services.retrofit;

import com.martynaroj.traveljournal.services.models.PlacesAPI.PlacesResult;

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

}