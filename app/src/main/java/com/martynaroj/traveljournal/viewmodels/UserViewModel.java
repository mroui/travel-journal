package com.martynaroj.traveljournal.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.services.respositories.UserRepository;

public class UserViewModel extends AndroidViewModel {

    private UserRepository userRepository;
    private MutableLiveData<User> userMutableLiveData;

    public UserViewModel(Application application) {
        super(application);
        userRepository = new UserRepository();
    }

    public void getDataSnapshotLiveData(String uid) {//init
        userMutableLiveData = userRepository.getDataSnapshotLiveData(uid);
    }

    public LiveData<User> getUserLiveData() {//getStates
        return userMutableLiveData;
    }

}
