package com.martynaroj.traveljournal.services.respositories;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.services.models.Notification;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;

import java.util.ArrayList;
import java.util.List;

public class NotificationRepository {

    private CollectionReference notificationsRef;
    private Context context;


    private NotificationRepository() {
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        notificationsRef = rootRef.collection(Constants.NOTIFICATIONS);
    }


    public NotificationRepository(Context context) {
        this();
        this.context = context;
    }


    public String generateId() {
        return notificationsRef.document().getId();
    }


    public MutableLiveData<String> sendNotification(Notification notification) {
        MutableLiveData<String> notificationResponse = new MutableLiveData<>();
        DocumentReference notificationRef = notificationsRef.document(notification.getId());
        notificationRef.set(notification).addOnCompleteListener(task -> {
            if (task.isSuccessful())
                notificationResponse.setValue(notification.getId());
            else
                notificationResponse.setValue(context.getResources().getString(R.string.messages_error_failed_add_notification));
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
                    if (notification != null)
                        notificationData.setValue(notification);
                }
            }
        });
        return notificationData;
    }


    public MutableLiveData<List<Notification>> getNotifications(List<String> notificationsIds) {
        MutableLiveData<List<Notification>> notificationsListData = new MutableLiveData<>();
        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
        for (String id : notificationsIds)
            tasks.add(notificationsRef.document(id).get());
        Task<List<DocumentSnapshot>> finalTask = Tasks.whenAllSuccess(tasks);
        finalTask.addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<Notification> notifications = new ArrayList<>();
                for (DocumentSnapshot documentSnapshot : task.getResult())
                    notifications.add(documentSnapshot.toObject(Notification.class));
                notificationsListData.setValue(notifications);
            } else
                notificationsListData.setValue(null);
        });
        return notificationsListData;
    }


    public MutableLiveData<String> removeNotification(String id) {
        MutableLiveData<String> notificationResponse = new MutableLiveData<>();
        notificationsRef.document(id).delete().addOnCompleteListener(task -> {
            if (task.isSuccessful())
                notificationResponse.setValue(context.getResources().getString(R.string.messages_remove_notification_success));
            else
                notificationResponse.setValue(context.getResources().getString(R.string.messages_error_failed_remove_notification));
        });
        return notificationResponse;
    }

}
