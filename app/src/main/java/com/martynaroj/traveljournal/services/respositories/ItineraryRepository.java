package com.martynaroj.traveljournal.services.respositories;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.martynaroj.traveljournal.services.models.Itinerary;
import com.martynaroj.traveljournal.view.others.enums.Status;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;

public class ItineraryRepository {

    private Context context;
    private CollectionReference itinerariesRef;

    private ItineraryRepository() {
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        itinerariesRef = rootRef.collection(Constants.ITINERARIES);
    }


    public ItineraryRepository(Context context) {
        this();
        this.context = context;
    }


    public MutableLiveData<Status> addItinerary(Itinerary itinerary) {
        MutableLiveData<Status> status = new MutableLiveData<>();
        DocumentReference itineraryRef = itinerariesRef.document(itinerary.getId());
        itineraryRef.set(itinerary).addOnCompleteListener(task -> {
            if (task.isSuccessful())
                status.setValue(Status.SUCCESS);
            else
                status.setValue(Status.ERROR);
        });
        return status;
    }

}
