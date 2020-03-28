package com.martynaroj.traveljournal.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.martynaroj.traveljournal.services.models.Travel;
import com.martynaroj.traveljournal.services.respositories.TravelRepository;

public class TravelViewModel extends AndroidViewModel {

    private LiveData<Travel> travelLiveData;
    private TravelRepository travelRepository;

    public TravelViewModel(@NonNull Application application) {
        super(application);
        travelRepository = new TravelRepository(application.getApplicationContext());
    }

    public LiveData<Travel> getTravelData() {
        return travelLiveData;
    }

}