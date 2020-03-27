package com.martynaroj.traveljournal.services.others;

import android.app.AlarmManager;
import android.app.PendingIntent;
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
                .setContentTitle(context.getResources().getString(R.string.alarm_title))
                .setContentText(intent.getStringExtra(Constants.ALARM_DESC))
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(Constants.RC_BROADCAST, builder.build());
    }

    public static void sendBroadcast(Context context, Intent broadcastIntent, Long time, String note) {
        broadcastIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        broadcastIntent.putExtra(Constants.ALARM_DESC, note);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                Constants.RC_BROADCAST,
                broadcastIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null)
            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + time, pendingIntent);
    }

}
