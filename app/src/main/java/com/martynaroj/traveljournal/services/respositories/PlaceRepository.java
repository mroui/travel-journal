package com.martynaroj.traveljournal.services.respositories;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.maps.model.LatLng;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.services.models.placesAPI.PlacesResult;
import com.martynaroj.traveljournal.services.retrofit.Rest;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlaceRepository {

    private Context context;

    public PlaceRepository(Context context) {
        this.context = context;
    }

    public MutableLiveData<PlacesResult> getPlacesResult(LatLng latLng, String type) {
        MutableLiveData<PlacesResult> placesResultData = new MutableLiveData<>();
        Rest.getPlacesService().getNearbyPlaces(
                latLng.latitude + "," + latLng.longitude,
                Constants.NEARBY_PLACES_RADIUS,
                type,
                context.getString(R.string.google_api_key)
        ).enqueue(new Callback<PlacesResult>() {
            @Override
            public void onResponse(@NonNull Call<PlacesResult> call, @NonNull Response<PlacesResult> response) {
                if (response.isSuccessful()) {
                    placesResultData.setValue(response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<PlacesResult> call, @NonNull Throwable t) {
                placesResultData.setValue(null);
            }
        });
        return placesResultData;
    }

}