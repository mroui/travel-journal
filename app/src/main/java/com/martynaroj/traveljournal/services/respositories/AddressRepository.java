package com.martynaroj.traveljournal.services.respositories;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.martynaroj.traveljournal.services.models.Address;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;

public class AddressRepository {

    private FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
    private CollectionReference addressesRef = rootRef.collection(Constants.ADDRESSES);


    public MutableLiveData<String> saveAddress(Address address, String reference) {
        MutableLiveData<String> statusData = new MutableLiveData<>();

        DocumentReference addressRef = reference == null ? addressesRef.document() : addressesRef.document(reference);
        if (address == null) address = new Address(addressRef.getId());

        addressRef.set(address).addOnCompleteListener(uidTask -> {
            if (uidTask.isSuccessful()) {
                statusData.setValue(addressRef.getId());
            } else if (uidTask.getException() != null) {
                statusData.setValue("ERROR: " + uidTask.getException().getMessage());
            }
        });
        return statusData;
    }


    public MutableLiveData<Address> getAddress(String reference) {
        MutableLiveData<Address> addressData = new MutableLiveData<>();
        DocumentReference addressRef = addressesRef.document(reference);
        addressRef.get().addOnCompleteListener(uidTask -> {
            if (uidTask.isSuccessful()) {
                DocumentSnapshot document = uidTask.getResult();
                if (document != null && document.exists()) {
                    Address address = document.toObject(Address.class);
                    addressData.setValue(address);
                }
            } else if (uidTask.getException() != null) {
                addressData.setValue(null);
            }
        });
        return addressData;
    }

}