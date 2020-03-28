package com.martynaroj.traveljournal.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.martynaroj.traveljournal.services.respositories.StorageRepository;

public class StorageViewModel extends AndroidViewModel {

    private StorageRepository storageRepository;
    private MutableLiveData<String> statusLiveData;

    public StorageViewModel(Application application) {
        super(application);
        storageRepository = new StorageRepository(application.getApplicationContext());
    }

    public LiveData<String> getStorageStatus() {
        return statusLiveData;
    }

    public void saveToStorage(byte[] bytes, String name, String path) {
        statusLiveData = storageRepository.saveToStorage(bytes, name, path);
    }

}