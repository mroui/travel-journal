package com.martynaroj.traveljournal.services.respositories;

import android.content.Context;
import android.net.Uri;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.martynaroj.traveljournal.R;

public class StorageRepository {

    private StorageReference storageReference;
    private Context context;

    public StorageRepository(Context context) {
        storageReference = FirebaseStorage.getInstance().getReference();
        this.context = context;
    }

    public MutableLiveData<String> saveImageToStorage(byte[] bytes, String name, String path) {
        MutableLiveData<String> statusData = new MutableLiveData<>();
        StorageReference ref = storageReference.child(path).child(name);
        ref.putBytes(bytes).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                ref.getDownloadUrl().addOnCompleteListener(uri -> {
                    if (uri.isSuccessful() && uri.getResult() != null)
                        statusData.setValue(uri.getResult().toString());
                    else if (uri.getException() != null)
                        statusData.setValue(context.getResources().getString(R.string.messages_error) + uri.getException().getMessage());
                });
            } else if (task.getException() != null)
                statusData.setValue(context.getResources().getString(R.string.messages_error)
                        + task.getException().getMessage());
        });
        return statusData;
    }


    public MutableLiveData<String> saveFileToStorage(Uri uri, String name, String path) {
        MutableLiveData<String> statusData = new MutableLiveData<>();
        StorageReference ref = storageReference.child(path).child(name);
        ref.putFile(uri).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                ref.getDownloadUrl().addOnCompleteListener(url -> {
                    if (url.isSuccessful() && url.getResult() != null)
                        statusData.setValue(url.getResult().toString());
                    else if (url.getException() != null)
                        statusData.setValue(context.getResources().getString(R.string.messages_error) + url.getException().getMessage());
                });
            } else if (task.getException() != null)
                statusData.setValue(context.getResources().getString(R.string.messages_error)
                        + task.getException().getMessage());
        });
        return statusData;
    }


    public void remove(String reference) {
        StorageReference fileRef = storageReference.child(reference);
        fileRef.delete();
    }

}
