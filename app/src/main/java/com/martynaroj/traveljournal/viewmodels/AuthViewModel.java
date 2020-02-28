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
    private LiveData<String> changesStatus;

    public AuthViewModel(Application application) {
        super(application);
        authRepository = new AuthRepository(application.getApplicationContext());
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
        userLiveData = authRepository.addUserToDatabase(user);
    }

    public LiveData<DataWrapper<User>> getAddedUserLiveData() {
        return userLiveData;
    }

    public void sendVerificationMail() {
        userLiveData = authRepository.sendVerificationMail();
    }

    public LiveData<DataWrapper<User>> getUserVerificationLiveData() {
        return userLiveData;
    }

    public void sendPasswordResetEmail(String email) {
        userLiveData = authRepository.sendPasswordResetEmail(email);
    }

    public LiveData<DataWrapper<User>> getUserForgotPasswordLiveData() {
        return userLiveData;
    }

    public void logInWithEmail(String email, String password) {
        userLiveData = authRepository.logInWithEmail(email, password);
    }

    public void getUserFromDatabase(String uid) {
        userLiveData = authRepository.getUser(uid);
    }

    public void changePassword(String currentPassword, String newPassword) {
        changesStatus = authRepository.changePassword(currentPassword, newPassword);
    }

    public LiveData<String> getChangesStatus() {
        return changesStatus;
    }

    public void changeEmail(String currentPassword, String newEmail) {
        changesStatus = authRepository.changeEmail(currentPassword, newEmail);
    }

    public void changeUsername(String newUsername) {
        changesStatus = authRepository.changeUsername(newUsername);
    }
}
