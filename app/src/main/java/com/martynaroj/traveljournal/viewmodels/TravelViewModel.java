package com.martynaroj.traveljournal.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.martynaroj.traveljournal.services.models.Travel;
import com.martynaroj.traveljournal.services.respositories.TravelRepository;
import com.martynaroj.traveljournal.view.others.enums.Status;

import java.util.Map;

public class TravelViewModel extends AndroidViewModel {

    private TravelRepository travelRepository;
    private LiveData<Status> statusLiveData;
    private LiveData<Travel> travelLiveData;
    private MutableLiveData<Travel> travel;

    public TravelViewModel(@NonNull Application application) {
        super(application);
        travelRepository = new TravelRepository(application.getApplicationContext());
        travel = new MutableLiveData<>();
    }

    public String generateId() {
        return travelRepository.generateId();
    }

    public void addTravel(Travel travel) {
        statusLiveData = travelRepository.addTravel(travel);
    }

    public LiveData<Status> getStatusData() {
        return statusLiveData;
    }

    public void updateTravel(String id, Map<String, Object> changes) {
        travelRepository.updateTravel(id, changes);
        getTravelData(id);
    }

    public void getTravelData(String id) {
        travelLiveData = travelRepository.getTravel(id);
    }

    public LiveData<Travel> getTravelLiveData() {
        return travelLiveData;
    }

    public void setTravel(Travel travel) {
        this.travel.setValue(travel);
    }

    public LiveData<Travel> getTravel() {
        return travel;
    }

    public void removeTravel(String id) {
        travelRepository.removeTravel(id);
    }


}