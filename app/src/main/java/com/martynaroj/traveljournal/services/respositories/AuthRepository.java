package com.martynaroj.traveljournal.services.respositories;

import android.content.Context;

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
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.services.models.DataWrapper;
import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.view.others.enums.Status;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;

public class AuthRepository {

    private FirebaseAuth firebaseAuth;
    private CollectionReference usersRef;
    private Context context;


    private AuthRepository() {
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.usersRef = rootRef.collection(Constants.USERS);
    }


    public AuthRepository(Context context) {
        this();
        this.context = context;
    }


    public MutableLiveData<DataWrapper<User>> signUpWithEmail(String email, String password, String username) {
        MutableLiveData<DataWrapper<User>> userLiveData = new MutableLiveData<>();
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(authTask -> {
            if (authTask.isSuccessful()) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest
                            .Builder().setDisplayName(username).build();
                    firebaseUser.updateProfile(profileUpdates);
                    User user = new User(firebaseUser.getUid(), username, email);
                    userLiveData.setValue(new DataWrapper<>(user, Status.LOADING, null));
                } else
                    userLiveData.setValue(new DataWrapper<>(null, Status.ERROR,
                            context.getResources().getString(R.string.messages_error_user_identity)));
            } else
                handleUserLiveDataErrors(authTask, userLiveData);
        });
        return userLiveData;
    }


    public MutableLiveData<DataWrapper<User>> sendVerificationMail() {
        MutableLiveData<DataWrapper<User>> userLiveData = new MutableLiveData<>();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            firebaseUser.sendEmailVerification().addOnCompleteListener(task -> {
                if (task.isSuccessful())
                    userLiveData.setValue(new DataWrapper<>(null, Status.SUCCESS,
                            context.getResources().getString(R.string.messages_verification_sent)));
                else
                    handleUserLiveDataErrors(task, userLiveData);
            });
        } else
            userLiveData.setValue(new DataWrapper<>(null, Status.ERROR,
                    context.getResources().getString(R.string.messages_error_user_identity)));
        return userLiveData;
    }


    public LiveData<DataWrapper<User>> sendPasswordResetEmail(String email) {
        MutableLiveData<DataWrapper<User>> userLiveData = new MutableLiveData<>();
        firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful())
                userLiveData.setValue(new DataWrapper<>(null, Status.SUCCESS,
                        context.getResources().getString(R.string.messages_reset_password_sent)));
            else
                handleUserLiveDataErrors(task, userLiveData);
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
                                    context.getResources().getString(R.string.messages_auth_success), true, isAdded, true));
                        } else
                            handleUserLiveDataErrors(task, userLiveData);
                    });
                } else
                    userLiveData.setValue(new DataWrapper<>(null, Status.ERROR,
                            context.getResources().getString(R.string.messages_error_user_identity)));
            } else
                handleUserLiveDataErrors(authTask, userLiveData);
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
                        if (addingTask.isSuccessful())
                            newUserLiveData.setValue(user);
                        else if (addingTask.getException() != null)
                            newUserLiveData.setValue(new DataWrapper<>(null, Status.ERROR,
                                    context.getResources().getString(R.string.messages_error)
                                            + addingTask.getException().getMessage()));
                    });
                } else
                    newUserLiveData.setValue(user);
            } else if (uidTask.getException() != null)
                newUserLiveData.setValue(new DataWrapper<>(null, Status.ERROR,
                        context.getResources().getString(R.string.messages_error) + uidTask.getException().getMessage()));
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
                            context.getResources().getString(R.string.messages_auth_success), true, false, isVerified));
                } else
                    userLiveData.setValue(new DataWrapper<>(null, Status.ERROR,
                            context.getResources().getString(R.string.messages_error_user_identity)));
            } else
                handleUserLiveDataErrors(authTask, userLiveData);
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
                            context.getResources().getString(R.string.messages_auth_success), true, true, true));
                } else
                    userLiveData.setValue(new DataWrapper<>(null, Status.ERROR,
                            context.getResources().getString(R.string.messages_error_no_user_database),
                            true, false, true));
            } else
                handleUserLiveDataErrors(task, userLiveData);
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
                        if (task1.isSuccessful())
                            status.setValue(context.getResources().getString(R.string.messages_password_changed));
                        else if (task1.getException() != null)
                            status.setValue(context.getResources().getString(R.string.messages_error) + task1.getException().getMessage());
                    });
                } else if (task.getException() != null) {
                    status.setValue(context.getResources().getString(R.string.messages_error) + task.getException().getMessage());
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
                        if (task1.isSuccessful())
                            status.setValue(context.getResources().getString(R.string.messages_email_changed));
                        else if (task1.getException() != null)
                            status.setValue(context.getResources().getString(R.string.messages_error) + task1.getException().getMessage());
                    });
                } else if (task.getException() != null)
                    status.setValue(context.getResources().getString(R.string.messages_error) + task.getException().getMessage());
            });
        }
        return status;
    }


    public LiveData<String> changeUsername(String newUsername) {
        MutableLiveData<String> status = new MutableLiveData<>();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest
                    .Builder().setDisplayName(newUsername).build();
            user.updateProfile(profileUpdates);
            status.setValue(context.getResources().getString(R.string.messages_username_changed));
        } else
            status.setValue(context.getResources().getString(R.string.messages_error_user_identity));
        return status;
    }


    private void handleUserLiveDataErrors(Task authTask, MutableLiveData<DataWrapper<User>> userLiveData) {
        if (authTask.getException() != null) {
            try {
                throw authTask.getException();
            } catch (FirebaseNetworkException e) {
                userLiveData.setValue(new DataWrapper<>(null, Status.ERROR,
                        context.getResources().getString(R.string.messages_error_network_connection)));
            } catch (Exception e) {
                userLiveData.setValue(new DataWrapper<>(null, Status.ERROR,
                        context.getResources().getString(R.string.messages_error) + e.getMessage()));
            }
        } else {
            userLiveData.setValue(new DataWrapper<>(null, Status.ERROR,
                    context.getResources().getString(R.string.messages_auth_failed)));
        }
    }

}