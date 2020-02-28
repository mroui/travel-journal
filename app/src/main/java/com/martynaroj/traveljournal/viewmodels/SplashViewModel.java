package com.martynaroj.traveljournal.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.martynaroj.traveljournal.services.models.DataWrapper;
import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.services.respositories.SplashRepository;

public class SplashViewModel extends AndroidViewModel {

    private SplashRepository splashRepository;
    private LiveData<DataWrapper<User>> isUserAuthLiveData;
    private LiveData<DataWrapper<User>> userLiveData;

    public SplashViewModel(Application application) {
        super(application);
        splashRepository = new SplashRepository(application.getApplicationContext());
    }

    public void checkCurrentUserAuth() {
        isUserAuthLiveData = splashRepository.checkUserIsAuth();
    }

    public void setUid(String uid) {
        userLiveData = splashRepository.getUserFromDatabase(uid);
    }

    public LiveData<DataWrapper<User>> getIsUserAuthLiveData() {
        return isUserAuthLiveData;
    }

    public LiveData<DataWrapper<User>> getUserLiveData() {
        return userLiveData;
    }
}