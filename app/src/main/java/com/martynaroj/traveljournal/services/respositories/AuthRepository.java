package com.martynaroj.traveljournal.services.respositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.martynaroj.traveljournal.services.models.DataWrapper;
import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.view.others.enums.Status;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;

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
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(username).build();
                    firebaseUser.updateProfile(profileUpdates);
                    User user = new User(firebaseUser.getUid(), username, email);
                    userLiveData.setValue(new DataWrapper<>(user, Status.LOADING, null));
                } else {
                    userLiveData.setValue(new DataWrapper<>(null, Status.ERROR,
                            "ERROR: User identity error. Please try again later"));
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
                            "Verification email has been sent. Check your email to verify account"));
                } else {
                    handleUserLiveDataErrors(task, userLiveData);
                }
            });
        } else {
            userLiveData.setValue(new DataWrapper<>(null, Status.ERROR,
                    "ERROR: User identity error. Please try again later"));
        }
        return userLiveData;
    }


    public LiveData<DataWrapper<User>> sendPasswordResetEmail(String email) {
        MutableLiveData<DataWrapper<User>> userLiveData = new MutableLiveData<>();
        firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                userLiveData.setValue(new DataWrapper<>(null, Status.SUCCESS,
                        "Password reset email has been sent. Check your email to reset your password"));
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
                    User user = new User(uid, name, email);
                    usersRef.document(uid).get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            boolean isAdded;
                            isAdded = document != null && document.exists();
                            userLiveData.setValue(new DataWrapper<>(user, Status.SUCCESS,
                                    "Authorization successful!", true, isAdded, true));
                        } else {
                            handleUserLiveDataErrors(task, userLiveData);
                        }
                    });
                } else {
                    userLiveData.setValue(new DataWrapper<>(null, Status.ERROR,
                            "ERROR: User identity error. Please try again later"));
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
                user.setAdded(true);
                DocumentSnapshot document = uidTask.getResult();
                if (document != null && !document.exists()) {
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


    public LiveData<DataWrapper<User>> logInWithEmail(String email, String password) {
        MutableLiveData<DataWrapper<User>> userLiveData = new MutableLiveData<>();
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(authTask -> {
            if (authTask.isSuccessful()) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    String uid = firebaseUser.getUid();
                    String name = firebaseUser.getDisplayName();
                    boolean isVerified = firebaseUser.isEmailVerified();
                    User user = new User(uid, name, email);
                    userLiveData.setValue(new DataWrapper<>(user, Status.SUCCESS,
                            "Authorization successful!", true, false, isVerified));
                } else {
                    userLiveData.setValue(new DataWrapper<>(null, Status.ERROR,
                            "ERROR: User identity error. Please try again later"));
                }
            } else {
                handleUserLiveDataErrors(authTask, userLiveData);
            }
        });
        return userLiveData;
    }


    public LiveData<DataWrapper<User>> getUser(String uid) {
        MutableLiveData<DataWrapper<User>> userLiveData = new MutableLiveData<>();
        DocumentReference userReference = usersRef.document(uid);
        userReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    User user = document.toObject(User.class);
                    userLiveData.setValue(new DataWrapper<>(user, Status.SUCCESS,
                            "Getting user successful!", true, true, true));
                } else {
                    userLiveData.setValue(new DataWrapper<>(null, Status.ERROR,
                            "ERROR: No such user in database", true, false, true));
                }
            } else {
                handleUserLiveDataErrors(task, userLiveData);
            }
        });
        return userLiveData;
    }


    public LiveData<String> changePassword(String currentPassword, String newPassword) {
        MutableLiveData<String> status = new MutableLiveData<>();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getEmail() != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
            user.reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    user.updatePassword(newPassword).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            status.setValue("Password successfully changed!");
                        } else if(task1.getException() != null) {
                            status.setValue("ERROR: " + task1.getException().getMessage());
                        }
                    });
                } else if(task.getException() != null) {
                    status.setValue("ERROR: " + task.getException().getMessage());
                }
            });
        }
        return status;
    }


    public LiveData<String> changeEmail(String currentPassword, String newEmail) {
        MutableLiveData<String> status = new MutableLiveData<>();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getEmail() != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
            user.reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    user.updateEmail(newEmail).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            status.setValue("Email successfully changed!");
                        } else if(task1.getException() != null) {
                            status.setValue("ERROR: " + task1.getException().getMessage());
                        }
                    });
                } else if(task.getException() != null) {
                    status.setValue("ERROR: " + task.getException().getMessage());
                }
            });
        }
        return status;
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