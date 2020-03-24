package com.martynaroj.traveljournal.services.respositories;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.services.models.Marker;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;

import java.util.ArrayList;
import java.util.List;

public class MarkerRepository {

    private CollectionReference markersRef;
    private Context context;


    private MarkerRepository() {
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        markersRef = rootRef.collection(Constants.MARKERS);
    }


    public MarkerRepository(Context context) {
        this();
        this.context = context;
    }


    public MutableLiveData<String> addMarker(Marker marker) {
        MutableLiveData<String> markerResponse = new MutableLiveData<>();

        DocumentReference markerRef = markersRef.document();
        marker.setId(markerRef.getId());

        markerRef.set(marker).addOnCompleteListener(uidTask -> {
            if (uidTask.isSuccessful())
                markerResponse.setValue(markerRef.getId());
            else if (uidTask.getException() != null)
                markerResponse.setValue(context.getResources().getString(R.string.messages_error) + uidTask.getException().getMessage());
        });
        return markerResponse;
    }


    public MutableLiveData<Marker> getMarker(String reference) {
        MutableLiveData<Marker> markerData = new MutableLiveData<>();
        DocumentReference markerRef = markersRef.document(reference);
        markerRef.get().addOnCompleteListener(uidTask -> {
            if (uidTask.isSuccessful()) {
                DocumentSnapshot document = uidTask.getResult();
                if (document != null && document.exists())
                    markerData.setValue(document.toObject(Marker.class));
            } else
                markerData.setValue(null);
        });
        return markerData;
    }


    public MutableLiveData<List<Marker>> getMarkers(List<String> markersIds) {
        MutableLiveData<List<Marker>> markersListData = new MutableLiveData<>();
        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
        for (String id : markersIds)
            tasks.add(markersRef.document(id).get());
        Task<List<DocumentSnapshot>> finalTask = Tasks.whenAllSuccess(tasks);
        finalTask.addOnCompleteListener(task -> {
            if(task.isSuccessful() && task.getResult() != null) {
                List<Marker> markers = new ArrayList<>();
                for (DocumentSnapshot documentSnapshot : task.getResult())
                    markers.add(documentSnapshot.toObject(Marker.class));
                markersListData.setValue(markers);
            } else
                markersListData.setValue(null);
        });
        return markersListData;
    }


    public MutableLiveData<String> removeMarker(String id) {
        MutableLiveData<String> markerResponse = new MutableLiveData<>();
        markersRef.document(id).delete().addOnCompleteListener(task -> {
            if (task.isSuccessful())
                markerResponse.setValue(context.getResources().getString(R.string.messages_remove_marker_success));
            else
                markerResponse.setValue(context.getResources().getString(R.string.messages_error_failed_remove_marker));
        });
        return markerResponse;
    }

}