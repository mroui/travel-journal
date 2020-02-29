package com.martynaroj.traveljournal.services.respositories;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.services.models.Notification;
import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;

public class NotificationRepository {

    private FirebaseFirestore rootRef;
    private CollectionReference notificationsRef;
    private CollectionReference usersRef;
    private Context context;

    private NotificationRepository() {
        rootRef = FirebaseFirestore.getInstance();
        notificationsRef = rootRef.collection(Constants.NOTIFICATIONS);
        usersRef = rootRef.collection(Constants.USERS);
    }


    public NotificationRepository(Context context) {
        this();
        this.context = context;
    }


    public MutableLiveData<String> sendNotification(User from, User to, Integer type) {
        MutableLiveData<String> notificationResponse = new MutableLiveData<>();
        Notification newNotification = new Notification(from.getUid(), to.getUid(), type);
        DocumentReference notificationRef = notificationsRef.document();
        newNotification.setId(notificationRef.getId());
        notificationRef.set(newNotification).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                notificationResponse.setValue(newNotification.getId());
            } else {
                notificationResponse.setValue(context.getResources().getString(R.string.messages_error_failed_add_notification));
            }
        });
        return notificationResponse;
    }


    public MutableLiveData<Notification> getNotification(String id) {
        MutableLiveData<Notification> notificationData = new MutableLiveData<>();
        notificationsRef.document(id).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    Notification notification = document.toObject(Notification.class);
                    if (notification != null) {
                        notificationData.setValue(notification);
                    }
                }
            }
        });
        return notificationData;
    }

}
