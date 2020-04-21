package com.martynaroj.traveljournal.view.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.ItemNotificationBinding;
import com.martynaroj.traveljournal.services.models.Notification;
import com.martynaroj.traveljournal.view.interfaces.OnItemClickListener;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationHolder> {

    private Context context;
    private List<Notification> notifications;
    private OnItemClickListener listener;


    public NotificationAdapter(Context context, List<Notification> notifications) {
        this.context = context;
        this.notifications = notifications;
    }


    @NonNull
    @Override
    public NotificationHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNotificationBinding binding = ItemNotificationBinding.inflate(LayoutInflater.from(context), parent, false);
        return new NotificationHolder(binding);
    }


    private Notification getItem(int position) {
        return this.notifications.get(position);
    }


    @Override
    public void onBindViewHolder(@NonNull NotificationHolder holder, int position) {
        final Notification notification = notifications.get(position);
        Glide.with(context)
                .load(notification.getUserFrom().getPhoto())
                .placeholder(R.drawable.default_avatar)
                .into(holder.binding.notificationItemUserImage);
        holder.binding.notificationItemUserUsername.setText(notification.getUserFrom().getUsername());
        holder.binding.notificationItemTimestamp.setText(getTimePast(notification.getTimestamp().toDate()));

        holder.binding.notificationItem.setOnClickListener(view -> listener.onItemClick(
                getItem(position),
                position,
                holder.binding.notificationItem));
        holder.binding.notificationItemAcceptButton.setOnClickListener(view -> listener.onItemClick(
                getItem(position),
                position,
                holder.binding.notificationItemAcceptButton));
        holder.binding.notificationItemDiscardButton.setOnClickListener(view -> listener.onItemClick(
                getItem(position),
                position,
                holder.binding.notificationItemDiscardButton));

        switch (com.martynaroj.traveljournal.view.others.enums.Notification.values()[notification.getType()]) {
            case FRIEND:
                holder.binding.notificationItemMessage.setText(context.getResources().getString(R.string.notifications_friends_request_message));
                break;
            case START_TRIP:
                holder.binding.notificationItemMessage.setText(context.getResources().getString(R.string.notifications_new_trip_request_message));
                holder.binding.notificationItemAcceptButton.setVisibility(View.INVISIBLE);
                holder.binding.notificationItemDiscardButton.setText(context.getResources().getString(R.string.notifications_remove));
                break;
            case END_TRIP:
                holder.binding.notificationItemMessage.setText(context.getResources().getString(R.string.notifications_end_trip_request_message));
                holder.binding.notificationItemAcceptButton.setVisibility(View.INVISIBLE);
                holder.binding.notificationItemDiscardButton.setText(context.getResources().getString(R.string.notifications_remove));
                break;
        }
    }


    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.listener = onItemClickListener;
    }


    public void remove (int position) {
        notifications.remove(position);
        notifyItemRemoved(position);
        notifyDataSetChanged();
    }


    private String getTimePast(Date date) {
        Calendar currentDate = Calendar.getInstance();
        Calendar notificationDate = Calendar.getInstance();
        notificationDate.setTime(date);

        int cMin = currentDate.get(Calendar.MINUTE);
        int cHour = currentDate.get(Calendar.HOUR_OF_DAY);
        int cDay = currentDate.get(Calendar.DAY_OF_MONTH);
        int cMonth = currentDate.get(Calendar.MONTH);
        int cYear = currentDate.get(Calendar.YEAR);

        int nMin = notificationDate.get(Calendar.MINUTE);
        int nHour = notificationDate.get(Calendar.HOUR_OF_DAY);
        int nDay = notificationDate.get(Calendar.DAY_OF_MONTH);
        int nMonth = notificationDate.get(Calendar.MONTH);
        int nYear = notificationDate.get(Calendar.YEAR);

        if (cYear == nYear)
            if (cMonth == nMonth)
                if (cDay == nDay)
                    if (cHour == nHour)
                        if (cMin == nMin) return "Now";
                        else return cMin - nMin + " min ago";
                    else return cHour - nHour + (cHour - nHour == 1 ? " hour ago" : " hours ago");
                else return cDay - nDay + (cDay - nDay == 1 ? " day ago" : " days ago");
            else return cMonth - nMonth + (cMonth - nMonth == 1 ? " month ago" : " months ago");
        else return cYear - nYear + (cYear - nYear == 1 ? " year ago" : " years ago");
    }


    @Override
    public int getItemCount() {
        return notifications.size();
    }


    static class NotificationHolder extends RecyclerView.ViewHolder {
        private ItemNotificationBinding binding;
        NotificationHolder(ItemNotificationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}