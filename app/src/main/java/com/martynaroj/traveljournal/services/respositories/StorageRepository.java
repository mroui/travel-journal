package com.martynaroj.traveljournal.services.respositories;

import android.content.Context;

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

    public MutableLiveData<String> saveToStorage(byte[] bytes, String userUid) {
        MutableLiveData<String> statusData = new MutableLiveData<>();
        StorageReference ref = storageReference.child("images/profiles").child(userUid + ".jpg");
        ref.putBytes(bytes).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                ref.getDownloadUrl().addOnCompleteListener(uri -> {
                    if (uri.isSuccessful() && uri.getResult() != null) {
                        statusData.setValue(uri.getResult().toString());
                    } else if (uri.getException() != null){
                        statusData.setValue(context.getResources().getString(R.string.messages_error) + uri.getException().getMessage());
                    }
                });
            } else if (task.getException() != null){
                statusData.setValue(context.getResources().getString(R.string.messages_error)
                        + task.getException().getMessage());
            }
        });
        return statusData;
    }

}
