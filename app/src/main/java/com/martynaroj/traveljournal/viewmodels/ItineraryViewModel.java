package com.martynaroj.traveljournal.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.martynaroj.traveljournal.services.models.Itinerary;
import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.services.respositories.ItineraryRepository;
import com.martynaroj.traveljournal.view.others.enums.Criterion;
import com.martynaroj.traveljournal.view.others.enums.Status;

import java.util.List;
import java.util.Map;

public class ItineraryViewModel extends AndroidViewModel {

    private ItineraryRepository itineraryRepository;
    private LiveData<Status> statusLiveData;
    private LiveData<List<Itinerary>> itinerariesLiveData;
    private final MutableLiveData<Itinerary> itinerary;
    private LiveData<List<DocumentSnapshot>> documentsLiveData;

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

    public void getItinerariesOrderBy(User user, int limit, String order, Query.Direction direction) {
        itinerariesLiveData = itineraryRepository.getItinerariesOrderBy(user, limit, order, direction);
    }

    public void getDocumentsListStartAt(User user, DocumentSnapshot last, int limit, String orderBy,
                                        Query.Direction direction, Criterion ... criteria) {
        documentsLiveData = itineraryRepository.getItinerariesDocumentsListStartAt(user, limit, last, orderBy, direction, criteria);
    }

    public LiveData<List<DocumentSnapshot>> getDocumentsData() {
        return documentsLiveData;
    }

}