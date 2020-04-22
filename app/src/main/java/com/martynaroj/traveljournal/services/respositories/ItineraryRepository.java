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
import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.view.others.enums.Privacy;
import com.martynaroj.traveljournal.view.others.enums.Status;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;

import java.util.ArrayList;
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


    public LiveData<List<Itinerary>> getItinerariesOrderBy(User user, int limit, String orderBy, Query.Direction direction) {
        MutableLiveData<List<Itinerary>> itinerariesData = new MutableLiveData<>();
        itinerariesRef.orderBy(orderBy, direction).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<Itinerary> itineraries = new ArrayList<>();
                for (int i = 0; i<task.getResult().size(); i++) {
                    Itinerary itinerary = task.getResult().getDocuments().get(i).toObject(Itinerary.class);
                    if (itinerary != null
                            && (itinerary.getPrivacy() == Privacy.PUBLIC.ordinal()
                            || ( itinerary.getPrivacy() == Privacy.FRIENDS.ordinal()
                                && user.getFriends().contains(itinerary.getOwner()))))
                        itineraries.add(itinerary);
                    if(itineraries.size() == limit)
                        break;
                }
                itinerariesData.setValue(itineraries);
            }
        });
        return itinerariesData;
    }

}
