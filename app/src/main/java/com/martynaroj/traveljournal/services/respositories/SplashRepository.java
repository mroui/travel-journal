package com.martynaroj.traveljournal.services.respositories;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.martynaroj.traveljournal.services.models.DataWrapper;
import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;
import com.martynaroj.traveljournal.view.others.enums.Status;

public class SplashRepository {

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private DataWrapper<User> user = new DataWrapper<>(new User(), Status.LOADING, null);
    private FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
    private CollectionReference usersRef = rootRef.collection(Constants.USERS);


    public MutableLiveData<DataWrapper<User>> checkUserIsAuth() {
        MutableLiveData<DataWrapper<User>> isUserAuthMutableLiveData = new MutableLiveData<>();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            user.setAuthenticated(false);
        } else {
            user.getData().setUid(firebaseUser.getUid());
            user.setAuthenticated(true);
        }
        isUserAuthMutableLiveData.setValue(user);
        return isUserAuthMutableLiveData;
    }


    public MutableLiveData<DataWrapper<User>> getUserFromDatabase(String uid) {
        MutableLiveData<DataWrapper<User>> userMutableLiveData = new MutableLiveData<>();
        usersRef.document(uid).get().addOnCompleteListener(userTask -> {
            if (userTask.isSuccessful()) {
                DocumentSnapshot document = userTask.getResult();
                if (document != null && document.exists()) {
                    User user = document.toObject(User.class);
                    userMutableLiveData.setValue(new DataWrapper<>(user, Status.SUCCESS, "Authorization successful!"));
                } else {
                    userMutableLiveData.setValue(new DataWrapper<>(null, Status.ERROR, "ERROR: Current user doesn't exist"));
                }
            } else if(userTask.getException() != null) {
                userMutableLiveData.setValue(new DataWrapper<>(null, Status.ERROR, "ERROR: " + userTask.getException().getMessage()));
            }
        });
        return userMutableLiveData;
    }

}