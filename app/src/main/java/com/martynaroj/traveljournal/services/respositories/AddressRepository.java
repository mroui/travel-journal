package com.martynaroj.traveljournal.services.respositories;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.services.models.Address;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;

public class AddressRepository {

    private CollectionReference addressesRef;
    private Context context;


    private AddressRepository() {
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        addressesRef = rootRef.collection(Constants.ADDRESSES);
    }


    public AddressRepository(Context context) {
        this();
        this.context = context;
    }

    public String generateId() {
        return addressesRef.document().getId();
    }


    public MutableLiveData<String> addAddress(Address address, String reference) {
        MutableLiveData<String> statusData = new MutableLiveData<>();

        DocumentReference addressRef = reference == null ? addressesRef.document() : addressesRef.document(reference);
        if (address == null) address = new Address();
        address.setId(addressRef.getId());

        addressRef.set(address).addOnCompleteListener(uidTask -> {
            if (uidTask.isSuccessful())
                statusData.setValue(addressRef.getId());
            else if (uidTask.getException() != null)
                statusData.setValue(context.getResources().getString(R.string.messages_error) + uidTask.getException().getMessage());
            else
                statusData.setValue(null);
        });
        return statusData;
    }


    public MutableLiveData<Address> getAddress(String reference) {
        MutableLiveData<Address> addressData = new MutableLiveData<>();
        DocumentReference addressRef = addressesRef.document(reference);
        addressRef.get().addOnCompleteListener(uidTask -> {
            if (uidTask.isSuccessful()) {
                DocumentSnapshot document = uidTask.getResult();
                if (document != null && document.exists())
                    addressData.setValue(document.toObject(Address.class));
            } else
                addressData.setValue(null);
        });
        return addressData;
    }


    public MutableLiveData<FindCurrentPlaceResponse> detectAddress(PlacesClient placesClient, FindCurrentPlaceRequest request) {
        MutableLiveData<FindCurrentPlaceResponse> detectedAddress = new MutableLiveData<>();
        Task<FindCurrentPlaceResponse> placeResponse = placesClient.findCurrentPlace(request);
        placeResponse.addOnCompleteListener(task -> {
            if (task.isSuccessful())
                detectedAddress.setValue(task.getResult());
            else
                detectedAddress.setValue(null);
        });
        return detectedAddress;
    }


    public void removeAddress(String id) {
        DocumentReference addressRef = addressesRef.document(id);
        addressRef.delete();
    }

}