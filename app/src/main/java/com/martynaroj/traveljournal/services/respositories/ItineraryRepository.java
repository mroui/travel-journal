package com.martynaroj.traveljournal.services.respositories;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.martynaroj.traveljournal.services.models.Itinerary;
import com.martynaroj.traveljournal.view.others.enums.Status;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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


    public MutableLiveData<List<Itinerary>> getItineraries(List<String> ids) {
        MutableLiveData<List<Itinerary>> itinerariesData = new MutableLiveData<>();
        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
        for (String id : ids)
            if (id != null)
                tasks.add(itinerariesRef.document(id).get());
        Task<List<DocumentSnapshot>> finalTask = Tasks.whenAllSuccess(tasks);
        finalTask.addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<Itinerary> itineraries = new ArrayList<>();
                for (DocumentSnapshot documentSnapshot : task.getResult())
                    itineraries.add(documentSnapshot.toObject(Itinerary.class));
                itinerariesData.setValue(itineraries);
            }
        });
        return itinerariesData;
    }


    public void removeItinerary(String id) {
        DocumentReference itineraryRef = itinerariesRef.document(id);
        itineraryRef.delete();
    }


    public void updateItinerary(Itinerary itinerary, Map<String, Object> map) {
        DocumentReference itineraryRef = itinerariesRef.document(itinerary.getId());
        itineraryRef.update(map);
    }


    public LiveData<List<Itinerary>> getPublicLimitItinerariesWithOrder(int limit, String orderBy, Query.Direction direction) {
        MutableLiveData<List<Itinerary>> itinerariesData = new MutableLiveData<>();
        itinerariesRef.whereEqualTo(Constants.DB_PRIVACY, 0).limit(100).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<Itinerary> itineraries = new ArrayList<>();
                for (DocumentSnapshot documentSnapshot : task.getResult())
                    itineraries.add(documentSnapshot.toObject(Itinerary.class));
                sortItinerariesBy(itineraries, orderBy, direction);
                itinerariesData.setValue(itineraries.subList(0, limit));
            }
        });
        return itinerariesData;
    }


    private void sortItinerariesBy(List<Itinerary> list, String orderBy, Query.Direction direction) {
        if (orderBy.equals(Constants.DB_POPULARITY))
            Collections.sort(list, (i1, i2) -> {
                if (direction == Query.Direction.ASCENDING)
                    return (int) (i1.getPopularity() - i2.getPopularity());
                else
                    return (int) (i2.getPopularity() - i1.getPopularity());
            });
    }

}
