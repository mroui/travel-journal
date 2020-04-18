package com.martynaroj.traveljournal.services.respositories;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.martynaroj.traveljournal.services.models.Day;
import com.martynaroj.traveljournal.view.others.enums.Status;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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


    public MutableLiveData<List<Day>> getDays(List<String> daysIds) {
        MutableLiveData<List<Day>> daysData = new MutableLiveData<>();
        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
        for (String id : daysIds)
            if (id != null)
                tasks.add(daysRef.document(id).get());
        Task<List<DocumentSnapshot>> finalTask = Tasks.whenAllSuccess(tasks);
        finalTask.addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<Day> days = new ArrayList<>();
                for (DocumentSnapshot documentSnapshot : task.getResult())
                    days.add(documentSnapshot.toObject(Day.class));
                daysData.setValue(days);
            }
        });
        return daysData;
    }


    public MutableLiveData<Status> addDay(Day day) {
        MutableLiveData<Status> status = new MutableLiveData<>();
        DocumentReference dayRef = daysRef.document(day.getId());
        dayRef.set(day).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                status.setValue(Status.SUCCESS);
            } else
                status.setValue(Status.ERROR);
        });
        return status;
    }


    public void updateDay(String id, Map<String, Object> changes) {
        daysRef.document(id).update(changes);
    }


    public void removeDay(String id) {
        DocumentReference dayRef = daysRef.document(id);
        dayRef.delete();
    }


}
