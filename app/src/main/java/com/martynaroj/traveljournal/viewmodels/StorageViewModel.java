package com.martynaroj.traveljournal.viewmodels;

import android.app.Application;
import android.net.Uri;

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

    public void saveImageToStorage(byte[] bytes, String name, String path) {
        statusLiveData = storageRepository.saveImageToStorage(bytes, name, path);
    }

    public void saveFileToStorage(Uri uri, String name, String path) {
        statusLiveData = storageRepository.saveFileToStorage(uri, name, path);
    }

    public void removeFileFromStorage(String reference) {
        storageRepository.remove(reference);
    }

}