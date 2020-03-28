package com.martynaroj.traveljournal.services.respositories;

import android.content.Context;

import com.google.firebase.firestore.FirebaseFirestore;

public class TravelRepository {

    private Context context;

    private TravelRepository() {
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
    }


    public TravelRepository(Context context) {
        this();
        this.context = context;
    }

}
