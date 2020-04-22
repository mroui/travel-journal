package com.martynaroj.traveljournal.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.Query;
import com.martynaroj.traveljournal.services.models.Itinerary;
import com.martynaroj.traveljournal.services.respositories.ItineraryRepository;
import com.martynaroj.traveljournal.view.others.enums.Status;

import java.util.List;
import java.util.Map;

public class ItineraryViewModel extends AndroidViewModel {

    private ItineraryRepository itineraryRepository;
    private LiveData<Status> statusLiveData;
    private LiveData<List<Itinerary>> itinerariesLiveData;
    private final MutableLiveData<Itinerary> itinerary;

    public ItineraryViewModel(@NonNull Application application) {
        super(application);
        itineraryRepository = new ItineraryRepository(application.getApplicationContext());
        itinerary = new MutableLiveData<>();
    }

    public void addItinerary(Itinerary itinerary) {
        statusLiveData = itineraryRepository.addItinerary(itinerary);
    }

    public LiveData<Status> getStatusData() {
        return statusLiveData;
    }

    public void setItinerary(Itinerary itinerary) {
        this.itinerary.setValue(itinerary);
    }

    public LiveData<Itinerary> getItinerary() {
        return itinerary;
    }

    public void getItinerariesListData(List<String> ids) {
        itinerariesLiveData = itineraryRepository.getItineraries(ids);
    }

    public LiveData<List<Itinerary>> getItinerariesList() {
        return itinerariesLiveData;
    }

    public void removeItinerary(String id) {
        itineraryRepository.removeItinerary(id);
    }

    public void updateItinerary(Itinerary itinerary, Map<String, Object> map) {
        itineraryRepository.updateItinerary(itinerary, map);
    }

    public void getPublicLimitItinerariesWithOrder(int limit, String order, Query.Direction direction) {
        itinerariesLiveData = itineraryRepository.getPublicLimitItinerariesWithOrder(limit, order, direction);
    }

}