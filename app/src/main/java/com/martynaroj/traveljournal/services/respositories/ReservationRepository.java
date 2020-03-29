package com.martynaroj.traveljournal.services.respositories;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.martynaroj.traveljournal.services.models.Reservation;
import com.martynaroj.traveljournal.view.others.enums.Status;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;

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

}
