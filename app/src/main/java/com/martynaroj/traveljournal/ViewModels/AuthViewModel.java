package com.martynaroj.traveljournal.ViewModels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.google.firebase.auth.AuthCredential;
import com.martynaroj.traveljournal.Services.Models.DataWrapper;
import com.martynaroj.traveljournal.Services.Models.User;
import com.martynaroj.traveljournal.Services.Respositories.AuthRepository;

public class AuthViewModel extends AndroidViewModel {

    private AuthRepository authRepository;
    private LiveData<DataWrapper<User>> userLiveData;
    private LiveData<DataWrapper<User>> addedUserLiveData;

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
}
