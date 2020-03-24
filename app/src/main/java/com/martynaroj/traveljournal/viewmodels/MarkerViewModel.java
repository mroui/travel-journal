package com.martynaroj.traveljournal.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.martynaroj.traveljournal.services.models.Marker;
import com.martynaroj.traveljournal.services.respositories.MarkerRepository;

import java.util.List;

public class MarkerViewModel extends AndroidViewModel {

    private MarkerRepository markerRepository;
    private LiveData<String> markerResponse;
    private LiveData<Marker> markerLiveData;
    private LiveData<List<Marker>> markersListLiveData;

    public MarkerViewModel(@NonNull Application application) {
        super(application);
        markerRepository = new MarkerRepository(application.getApplicationContext());
    }

    public void addMarker(Marker marker) {
        markerResponse = markerRepository.addMarker(marker);
    }

    public LiveData<String> getMarkerResponse() {
        return markerResponse;
    }

    public void getMarkerData(String id) {
        markerLiveData = markerRepository.getMarker(id);
    }

    public LiveData<Marker> getMarker() {
        return markerLiveData;
    }

    public void getMarkersListData(List<String> markersIds) {
        markersListLiveData = markerRepository.getMarkers(markersIds);
    }

    public LiveData<List<Marker>> getMarkersList() {
        return markersListLiveData;
    }

    public void removeMarker(String id) {
        markerResponse = markerRepository.removeMarker(id);
    }

}
