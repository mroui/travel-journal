package com.martynaroj.traveljournal.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.google.android.gms.maps.model.LatLng;
import com.martynaroj.traveljournal.services.models.placesAPI.PlacesResult;
import com.martynaroj.traveljournal.services.respositories.PlaceRepository;

public class PlaceViewModel extends AndroidViewModel {

    private LiveData<PlacesResult> placesResultLiveData;
    private PlaceRepository placeRepository;

    public PlaceViewModel(@NonNull Application application) {
        super(application);
        placeRepository = new PlaceRepository(application.getApplicationContext());
    }

    public void getPlaces(LatLng latLng, String type) {
        placesResultLiveData = placeRepository.getPlacesResult(latLng, type);
    }

    public LiveData<PlacesResult> getPlacesResultData() {
        return placesResultLiveData;
    }

}