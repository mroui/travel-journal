package com.martynaroj.traveljournal.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.martynaroj.traveljournal.services.models.Itinerary;
import com.martynaroj.traveljournal.services.respositories.ItineraryRepository;
import com.martynaroj.traveljournal.view.others.enums.Status;

public class ItineraryViewModel extends AndroidViewModel {

    private ItineraryRepository itineraryRepository;
    private LiveData<Status> statusLiveData;

    public ItineraryViewModel(@NonNull Application application) {
        super(application);
        itineraryRepository = new ItineraryRepository(application.getApplicationContext());
    }

    public void addItinerary(Itinerary itinerary) {
        statusLiveData = itineraryRepository.addItinerary(itinerary);
    }

    public LiveData<Status> getStatusData() {
        return statusLiveData;
    }

}