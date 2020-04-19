package com.martynaroj.traveljournal.services.respositories;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.martynaroj.traveljournal.services.models.Itinerary;
import com.martynaroj.traveljournal.view.others.enums.Status;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;

import java.util.ArrayList;
import java.util.List;

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

}
