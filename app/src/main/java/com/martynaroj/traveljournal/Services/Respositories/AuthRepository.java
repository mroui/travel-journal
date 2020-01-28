package com.martynaroj.traveljournal.Services.Respositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.martynaroj.traveljournal.Services.Models.DataWrapper;
import com.martynaroj.traveljournal.Services.Models.User;
import com.martynaroj.traveljournal.View.Others.Status;

public class AuthRepository {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
    private CollectionReference usersRef = rootRef.collection("users");

    public AuthRepository() {
        this.firebaseAuth = FirebaseAuth.getInstance();
    }


    public MutableLiveData<DataWrapper<User>> firebaseSignInWithGoogle(AuthCredential googleAuthCredential) {
        MutableLiveData<DataWrapper<User>> userLiveData = new MutableLiveData<>();
        firebaseAuth.signInWithCredential(googleAuthCredential).addOnCompleteListener(authTask -> {
            if (authTask.isSuccessful()) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    String uid = firebaseUser.getUid();
                    String name = firebaseUser.getDisplayName();
                    String email = firebaseUser.getEmail();
                    boolean isNew = (authTask.getResult() != null && authTask.getResult().getAdditionalUserInfo() != null)
                                    && authTask.getResult().getAdditionalUserInfo().isNewUser();
                    User user = new User(uid, name, email, isNew);
                    userLiveData.setValue(new DataWrapper<>(user, Status.SUCCESS, "Authorization successful!"));
                }
            } else if (authTask.getException() != null) {
                try {
                    throw authTask.getException();
                } catch (FirebaseNetworkException e) {
                    userLiveData.setValue(new DataWrapper<>(null, Status.ERROR,
                            "Error: Please check your network connection"));
                } catch (Exception e) {
                    userLiveData.setValue(new DataWrapper<>(null, Status.ERROR, "Error: " + e.getMessage()));
                }
            } else {
                userLiveData.setValue(new DataWrapper<>(null, Status.ERROR, "Error: Unhandled authorization error"));
            }
        });
        return userLiveData;
    }


    public LiveData<DataWrapper<User>> addUserToDatabase(DataWrapper<User> user) {
        MutableLiveData<DataWrapper<User>> newUserMutableLiveData = new MutableLiveData<>();
        DocumentReference uidRef = usersRef.document(user.getData().getUid());
        uidRef.get().addOnCompleteListener(uidTask -> {
            if (uidTask.isSuccessful()) {
                DocumentSnapshot document = uidTask.getResult();
                if (document != null && !document.exists()) {
                    uidRef.set(user).addOnCompleteListener(addingTask -> {
                        if (addingTask.isSuccessful()) {
                            user.getData().setAdded(true);
                            newUserMutableLiveData.setValue(user);
                        } else {
                            //TODO: error handling
                        }
                    });
                } else {
                    newUserMutableLiveData.setValue(user);
                }
            } else {
                //TODO: error handling
            }
        });
        return newUserMutableLiveData;
    }

}