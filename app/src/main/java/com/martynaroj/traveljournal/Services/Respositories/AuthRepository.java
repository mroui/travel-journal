package com.martynaroj.traveljournal.Services.Respositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
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
import com.martynaroj.traveljournal.View.Others.Enums.Status;
import com.martynaroj.traveljournal.View.Others.Interfaces.Constants;

public class AuthRepository {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
    private CollectionReference usersRef = rootRef.collection(Constants.USERS);

    public AuthRepository() {
        this.firebaseAuth = FirebaseAuth.getInstance();
    }


    public MutableLiveData<DataWrapper<User>> signUpWithEmail(String email, String password, String username) {
        MutableLiveData<DataWrapper<User>> userLiveData = new MutableLiveData<>();
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(authTask -> {
            if (authTask.isSuccessful()) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    User user = new User(firebaseUser.getUid(), username, email);
                    userLiveData.setValue(new DataWrapper<>(user, Status.LOADING,
                            "Sending verification email..."));
                }
            } else {
                handleUserLiveDataErrors(authTask, userLiveData);
            }
        });
        return userLiveData;
    }


    public MutableLiveData<DataWrapper<User>> sendVerificationMail() {
        MutableLiveData<DataWrapper<User>> userLiveData = new MutableLiveData<>();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            firebaseUser.sendEmailVerification().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    userLiveData.setValue(new DataWrapper<>(null, Status.SUCCESS,
                            "Verification email has been sent. Check your email and verify your account to log in",
                            true, false));
                } else {
                    handleUserLiveDataErrors(task, userLiveData);
                }
            });
        }
        return userLiveData;
    }


    public LiveData<DataWrapper<User>> sendPasswordResetEmail(String email) {
        MutableLiveData<DataWrapper<User>> userLiveData = new MutableLiveData<>();
        firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                userLiveData.setValue(new DataWrapper<>(null, Status.SUCCESS,
                        "Password reset email has been sent. Click the link in the email to reset your password"));
            } else {
                handleUserLiveDataErrors(task, userLiveData);
            }
        });
        return userLiveData;
    }


    public MutableLiveData<DataWrapper<User>> signInWithGoogle(AuthCredential googleAuthCredential) {
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
                    User user = new User(uid, name, email);
                    userLiveData.setValue(new DataWrapper<>(user, Status.SUCCESS,
                            "Authorization successful!", isNew, false));
                }
            } else {
                handleUserLiveDataErrors(authTask, userLiveData);
            }
        });
        return userLiveData;
    }


    public LiveData<DataWrapper<User>> addUserToDatabase(DataWrapper<User> user) {
        MutableLiveData<DataWrapper<User>> newUserLiveData = new MutableLiveData<>();
        DocumentReference uidRef = usersRef.document(user.getData().getUid());
        uidRef.get().addOnCompleteListener(uidTask -> {
            if (uidTask.isSuccessful()) {
                DocumentSnapshot document = uidTask.getResult();
                if (document != null && !document.exists()) {
                    user.setAdded(true);
                    uidRef.set(user.getData()).addOnCompleteListener(addingTask -> {
                        if (addingTask.isSuccessful()) {
                            newUserLiveData.setValue(user);
                        } else if (addingTask.getException() != null){
                            newUserLiveData.setValue(new DataWrapper<>(null, Status.ERROR,
                                    "Error: " + addingTask.getException().getMessage()));
                        }
                    });
                } else {
                    newUserLiveData.setValue(user);
                }
            } else if (uidTask.getException() != null) {
                newUserLiveData.setValue(new DataWrapper<>(null, Status.ERROR,
                        "Error: " + uidTask.getException().getMessage()));
            }
        });
        return newUserLiveData;
    }


    private void handleUserLiveDataErrors(Task authTask, MutableLiveData<DataWrapper<User>> userLiveData) {
        if (authTask.getException() != null) {
            try {
                throw authTask.getException();
            } catch (FirebaseNetworkException e) {
                userLiveData.setValue(new DataWrapper<>(null, Status.ERROR,
                        "Error: Please check your network connection"));
            } catch (Exception e) {
                userLiveData.setValue(new DataWrapper<>(null, Status.ERROR,
                        "Error: " + e.getMessage()));
            }
        } else {
            userLiveData.setValue(new DataWrapper<>(null, Status.ERROR,
                    "Error: Unhandled authorization error"));
        }
    }

}