package com.martynaroj.traveljournal.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.google.firebase.auth.AuthCredential;
import com.martynaroj.traveljournal.services.models.DataWrapper;
import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.services.respositories.AuthRepository;

public class AuthViewModel extends AndroidViewModel {

    private AuthRepository authRepository;
    private LiveData<DataWrapper<User>> userLiveData;
    private LiveData<DataWrapper<User>> addedUserLiveData;
    private LiveData<DataWrapper<User>> userVerificationLiveData;
    private LiveData<DataWrapper<User>> userForgotPasswordLiveData;

    public AuthViewModel(Application application) {
        super(application);
        authRepository = new AuthRepository();
    }

    public void signInWithGoogle(AuthCredential googleAuthCredential) {
        userLiveData = authRepository.signInWithGoogle(googleAuthCredential);
    }

    public void signUpWithEmail(String email, String password, String username) {
        userLiveData = authRepository.signUpWithEmail(email, password, username);
    }

    public LiveData<DataWrapper<User>> getUserLiveData() {
        return userLiveData;
    }

    public void addUser(DataWrapper<User> user) {
        addedUserLiveData = authRepository.addUserToDatabase(user);
    }

    public LiveData<DataWrapper<User>> getAddedUserLiveData() {
        return addedUserLiveData;
    }

    public void sendVerificationMail() {
        userVerificationLiveData = authRepository.sendVerificationMail();
    }

    public LiveData<DataWrapper<User>> getUserVerificationLiveData() {
        return userVerificationLiveData;
    }

    public void sendPasswordResetEmail(String email) {
        userForgotPasswordLiveData = authRepository.sendPasswordResetEmail(email);
    }

    public LiveData<DataWrapper<User>> getUserForgotPasswordLiveData() {
        return userForgotPasswordLiveData;
    }

    public void logInWithEmail(String email, String password) {
        userLiveData = authRepository.logInWithEmail(email, password);
    }

    public void getUserFromDatabase(String uid) {
        userLiveData = authRepository.getUser(uid);
    }
}
