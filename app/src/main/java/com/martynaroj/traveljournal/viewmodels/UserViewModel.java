package com.martynaroj.traveljournal.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.services.respositories.UserRepository;

import java.util.Map;

public class UserViewModel extends AndroidViewModel {

    private UserRepository userRepository;
    private MutableLiveData<User> userLiveData;
    private final MutableLiveData<User> user;

    public UserViewModel(Application application) {
        super(application);
        userRepository = new UserRepository();
        user = new MutableLiveData<>();
    }

    public LiveData<User> getUserLiveData() {
        return userLiveData;
    }

    public void getDataSnapshotLiveData(String uid) {
        userLiveData = userRepository.getDataSnapshotLiveData(uid);
    }

    public void updateUser(User user, Map<String, Object> map) {
        userRepository.updateUser(user, map);
        userLiveData = userRepository.getDataSnapshotLiveData(user.getUid());
    }

    public void setUser(User user) {
        this.user.setValue(user);
    }

    public LiveData<User> getUser() {
        return user;
    }

}
