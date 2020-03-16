package com.martynaroj.traveljournal.view.others.classes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;

public class NotificationBroadcast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Constants.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_alarm_white)
                .setContentTitle(intent.getStringExtra(Constants.TITLE))
                .setContentText(intent.getStringExtra(Constants.DESC))
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(Constants.RC_BROADCAST, builder.build());
    }

}
