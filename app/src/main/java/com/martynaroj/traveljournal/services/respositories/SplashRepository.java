package com.martynaroj.traveljournal.services.respositories;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.services.models.DataWrapper;
import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.view.others.enums.Status;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;

public class SplashRepository {

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private DataWrapper<User> user = new DataWrapper<>(new User(), Status.LOADING, null);
    private FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
    private CollectionReference usersRef = rootRef.collection(Constants.USERS);
    private Context context;


    public SplashRepository(Context context) {
        this.context = context;
    }


    public MutableLiveData<DataWrapper<User>> checkUserIsAuth() {
        MutableLiveData<DataWrapper<User>> isUserAuthMutableLiveData = new MutableLiveData<>();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null)
            user.setAuthenticated(false);
        else {
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
                if (document != null && document.exists())
                    userMutableLiveData.setValue(new DataWrapper<>(document.toObject(User.class),
                            Status.SUCCESS, context.getResources().getString(R.string.messages_auth_success)));
                else
                    userMutableLiveData.setValue(new DataWrapper<>(null,
                            Status.ERROR, context.getResources().getString(R.string.messages_error_no_user_database)));
            } else if (userTask.getException() != null)
                userMutableLiveData.setValue(new DataWrapper<>(null,
                        Status.ERROR, context.getResources().getString(R.string.messages_error) + userTask.getException().getMessage()));
        });
        return userMutableLiveData;
    }

}