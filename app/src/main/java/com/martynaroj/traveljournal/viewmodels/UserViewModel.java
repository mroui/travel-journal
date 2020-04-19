package com.martynaroj.traveljournal.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.services.respositories.UserRepository;

import java.util.List;
import java.util.Map;

public class UserViewModel extends AndroidViewModel {

    private UserRepository userRepository;
    private MutableLiveData<User> userLiveData;
    private final MutableLiveData<User> user;
    private LiveData<List<User>> usersListLiveData;

    public UserViewModel(Application application) {
        super(application);
        userRepository = new UserRepository();
        user = new MutableLiveData<>();
    }

    public LiveData<User> getUserLiveData() {
        return userLiveData;
    }

    public void getUserData(String uid) {
        userLiveData = userRepository.getUserData(uid);
    }

    public void updateUser(boolean reload, User user, Map<String, Object> map) {
        userRepository.updateUser(user, map);
        if (reload)
            getUserData(user.getUid());
    }

    public void setUser(User user) {
        this.user.setValue(user);
    }

    public LiveData<User> getUser() {
        return user;
    }

    public void getUsersListData(List<String> usersIds) {
        usersListLiveData = userRepository.getUsers(usersIds);
    }

    public void getUsersWhereArrayContains(String key, Object value) {
        usersListLiveData = userRepository.getUsersWhereArrayContains(key, value);
    }

    public LiveData<List<User>> getUsersList() {
        return usersListLiveData;
    }

}
