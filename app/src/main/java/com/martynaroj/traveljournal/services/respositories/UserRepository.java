package com.martynaroj.traveljournal.services.respositories;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;

import java.util.Map;

public class UserRepository {

    private FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
    private CollectionReference usersRef = rootRef.collection(Constants.USERS);

    public MutableLiveData<User> getDataSnapshotLiveData(String uid) {
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
}
