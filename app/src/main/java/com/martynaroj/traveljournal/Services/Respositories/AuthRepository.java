package com.martynaroj.traveljournal.Services.Respositories;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.martynaroj.traveljournal.Services.Models.DataWrapper;
import com.martynaroj.traveljournal.Services.Models.User;
import com.martynaroj.traveljournal.View.Others.Status;

public class AuthRepository {

    private FirebaseAuth firebaseAuth;

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
                    User user = new User(uid, name, email);
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
}