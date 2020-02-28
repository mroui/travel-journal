package com.martynaroj.traveljournal.view.interfaces;

import com.google.firebase.firestore.DocumentSnapshot;

public interface OnItemClickListener {

    void onItemClick(DocumentSnapshot snapshot, int position);

}