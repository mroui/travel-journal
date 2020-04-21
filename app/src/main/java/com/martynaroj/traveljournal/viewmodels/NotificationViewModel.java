package com.martynaroj.traveljournal.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.martynaroj.traveljournal.services.models.Notification;
import com.martynaroj.traveljournal.services.respositories.NotificationRepository;

import java.util.List;

public class NotificationViewModel extends AndroidViewModel {

    private NotificationRepository notificationRepository;
    private LiveData<String> notificationResponse;
    private LiveData<Notification> notificationLiveData;
    private LiveData<List<Notification>> notificationsListLiveData;

    public NotificationViewModel(@NonNull Application application) {
        super(application);
        notificationRepository = new NotificationRepository(application.getApplicationContext());
    }

    public String generateId() {
        return notificationRepository.generateId();
    }

    public void sendNotification(Notification notification) {
        notificationResponse = notificationRepository.sendNotification(notification);
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

    public void getNotificationsListData(List<String> notificationsIds) {
        notificationsListLiveData = notificationRepository.getNotifications(notificationsIds);
    }

    public LiveData<List<Notification>> getNotificationsList() {
        return notificationsListLiveData;
    }

    public void removeNotification(String id) {
        notificationResponse = notificationRepository.removeNotification(id);
    }

}
