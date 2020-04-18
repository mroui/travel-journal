package com.martynaroj.traveljournal.services.respositories;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.martynaroj.traveljournal.services.models.Reservation;
import com.martynaroj.traveljournal.view.others.enums.Status;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;

import java.util.ArrayList;
import java.util.List;

public class ReservationRepository {

    private Context context;
    private CollectionReference reservationsRef;

    private ReservationRepository() {
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        reservationsRef = rootRef.collection(Constants.RESERVATIONS);
    }


    public ReservationRepository(Context context) {
        this();
        this.context = context;
    }


    public String generateId() {
        return reservationsRef.document().getId();
    }


    public MutableLiveData<Status> addReservation(Reservation reservation) {
        MutableLiveData<Status> status = new MutableLiveData<>();
        DocumentReference reservationRef = reservationsRef.document(reservation.getId());
        reservationRef.set(reservation).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                status.setValue(Status.SUCCESS);
            } else
                status.setValue(Status.ERROR);
        });
        return status;
    }


    public MutableLiveData<List<Reservation>> getReservations(List<String> ids) {
        MutableLiveData<List<Reservation>> reservationsData = new MutableLiveData<>();
        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
        for (String id : ids)
            tasks.add(reservationsRef.document(id).get());
        Task<List<DocumentSnapshot>> finalTask = Tasks.whenAllSuccess(tasks);
        finalTask.addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<Reservation> reservations = new ArrayList<>();
                for (DocumentSnapshot documentSnapshot : task.getResult())
                    reservations.add(documentSnapshot.toObject(Reservation.class));
                reservationsData.setValue(reservations);
            }
        });
        return reservationsData;
    }


    public void removeReservation(String id) {
        DocumentReference reservationRef = reservationsRef.document(id);
        reservationRef.delete();
    }

}
