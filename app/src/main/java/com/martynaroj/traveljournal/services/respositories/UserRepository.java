package com.martynaroj.traveljournal.services.respositories;

import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserRepository {

    private FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
    private CollectionReference usersRef = rootRef.collection(Constants.USERS);


    public MutableLiveData<User> getUserData(String uid) {
        MutableLiveData<User> userData = new MutableLiveData<>();
        DocumentReference userReference = usersRef.document(uid);
        userReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    User user = document.toObject(User.class);
                    userData.setValue(user);
                }
            }
        });
        return userData;
    }


    public void updateUser(User user, Map<String, Object> map) {
        DocumentReference userReference = usersRef.document(user.getUid());
        userReference.update(map);
    }


    public MutableLiveData<List<User>> getUsersWhereArrayContains(String key, Object value) {
        MutableLiveData<List<User>> usersData = new MutableLiveData<>();
        usersRef.whereArrayContains(key, value).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    List<User> users = new ArrayList<>();
                    for (DocumentSnapshot documentSnapshot : task.getResult().getDocuments())
                        users.add(documentSnapshot.toObject(User.class));
                    usersData.setValue(users);
            }
        });
        return usersData;
    }


    public MutableLiveData<List<User>> getUsers(List<String> usersIds) {
        MutableLiveData<List<User>> usersData = new MutableLiveData<>();
        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
        for (String id : usersIds)
            tasks.add(usersRef.document(id).get());
        Task<List<DocumentSnapshot>> finalTask = Tasks.whenAllSuccess(tasks);
        finalTask.addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<User> users = new ArrayList<>();
                for (DocumentSnapshot documentSnapshot : task.getResult())
                    users.add(documentSnapshot.toObject(User.class));
                usersData.setValue(users);
            }
        });
        return usersData;
    }

}
