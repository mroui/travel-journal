package com.martynaroj.traveljournal.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.martynaroj.traveljournal.services.models.Notification;
import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.services.respositories.NotificationRepository;

public class NotificationViewModel extends AndroidViewModel {

    private NotificationRepository notificationRepository;
    private LiveData<String> notificationResponse;
    private LiveData<Notification> notificationLiveData;

    public NotificationViewModel(@NonNull Application application) {
        super(application);
        notificationRepository = new NotificationRepository(application.getApplicationContext());
    }

    public void sendNotification(User from, User to, Integer type) {
        notificationResponse = notificationRepository.sendNotification(from, to, type);
    }

    public LiveData<String> getNotificationResponse() {
        return notificationResponse;
    }

    public void getNotificationData(String id) {
        notificationLiveData = notificationRepository.getNotification(id);
    }

    public LiveData<Notification> getNotification() {
        return notificationLiveData;
    }

}
