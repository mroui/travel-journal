package com.martynaroj.traveljournal.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.martynaroj.traveljournal.services.models.Reservation;
import com.martynaroj.traveljournal.services.respositories.ReservationRepository;
import com.martynaroj.traveljournal.view.others.enums.Status;

public class ReservationViewModel extends AndroidViewModel {

    private ReservationRepository reservationRepository;
    private LiveData<Status> statusLiveData;

    public ReservationViewModel(@NonNull Application application) {
        super(application);
        reservationRepository = new ReservationRepository(application.getApplicationContext());
    }

    public String generateId() {
        return reservationRepository.generateId();
    }

    public void addReservation(Reservation reservation) {
        statusLiveData = reservationRepository.addReservation(reservation);
    }

    public LiveData<Status> getStatusData() {
        return statusLiveData;
    }

}