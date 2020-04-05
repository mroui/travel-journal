package com.martynaroj.traveljournal.services.respositories;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.martynaroj.traveljournal.services.models.Day;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;

import java.util.ArrayList;
import java.util.List;

public class DayRepository {

    private Context context;
    private CollectionReference daysRef;

    private DayRepository() {
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        daysRef = rootRef.collection(Constants.DAYS);
    }


    public DayRepository(Context context) {
        this();
        this.context = context;
    }


    public String generateId() {
        return daysRef.document().getId();
    }

}
