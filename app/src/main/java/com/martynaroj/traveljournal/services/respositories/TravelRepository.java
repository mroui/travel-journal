package com.martynaroj.traveljournal.services.respositories;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.martynaroj.traveljournal.services.models.Travel;
import com.martynaroj.traveljournal.view.others.enums.Status;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;

import java.util.Map;

public class TravelRepository {

    private Context context;
    private CollectionReference travelsRef;

    private TravelRepository() {
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        travelsRef = rootRef.collection(Constants.TRAVELS);
    }


    public TravelRepository(Context context) {
        this();
        this.context = context;
    }


    public String generateId() {
        return travelsRef.document().getId();
    }


    public MutableLiveData<Status> addTravel(Travel travel) {
        MutableLiveData<Status> status = new MutableLiveData<>();
        DocumentReference travelRef = travelsRef.document(travel.getId());
        travelRef.set(travel).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                status.setValue(Status.SUCCESS);
            } else
                status.setValue(Status.ERROR);
        });
        return status;
    }


    public void updateTravel(String id, Map<String, Object> changes) {
        DocumentReference travelRef = travelsRef.document(id);
        travelRef.update(changes);
    }


    public LiveData<Travel> getTravel(String id) {
        MutableLiveData<Travel> travelData = new MutableLiveData<>();
        DocumentReference travelRef = travelsRef.document(id);
        travelRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    Travel travel = document.toObject(Travel.class);
                    travelData.setValue(travel);
                }
            }
        });
        return travelData;
    }


    public void removeTravel(String id) {
        DocumentReference travelRef = travelsRef.document(id);
        travelRef.delete();
    }

}
