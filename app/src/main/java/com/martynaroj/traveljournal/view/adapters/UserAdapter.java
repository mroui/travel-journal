package com.martynaroj.traveljournal.view.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.ItemUserBinding;
import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.view.interfaces.OnItemClickListener;
import com.martynaroj.traveljournal.view.others.enums.Privacy;

import java.util.List;
import java.util.Objects;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserHolder> {

    private Context context;
    private List<User> users;
    private OnItemClickListener listener;
    private boolean isUserProfile;


    public UserAdapter(Context context, List<User> users, boolean isUserProfile) {
        this.context = context;
        this.users = users;
        this.isUserProfile = isUserProfile;
    }


    @NonNull
    @Override
    public UserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUserBinding binding = ItemUserBinding.inflate(LayoutInflater.from(context), parent, false);
        return new UserHolder(binding);
    }


    private User getItem(int position) {
        return this.users.get(position);
    }


    @Override
    public void onBindViewHolder(@NonNull UserHolder holder, int position) {
        final User user = users.get(position);
        holder.binding.userItemUsername.setText(user.getUsername());
        if (user.getPrivacyEmail() == Privacy.PUBLIC.ordinal()) {
            holder.binding.userItemEmail.setText(user.getEmail());
        } else {
            holder.binding.userItemEmail.setVisibility(View.GONE);
        }
        Glide.with(context)
                .load(user.getPhoto())
                .placeholder(R.drawable.default_avatar)
                .into(holder.binding.userItemImage);
        holder.binding.userItem.setOnClickListener(view -> listener.onItemClick(
                Objects.requireNonNull(getItem(position)),
                position,
                holder.binding.userItem));
        holder.binding.userItemDeleteButton.setOnClickListener(view -> listener.onItemClick(
                Objects.requireNonNull(getItem(position)),
                position,
                holder.binding.userItemDeleteButton));
        if (!isUserProfile)
            holder.binding.userItemDeleteButton.setVisibility(View.GONE);
    }


    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.listener = onItemClickListener;
    }


    public void remove (int position) {
        users.remove(position);
        notifyItemRemoved(position);
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return users.size();
    }


    static class UserHolder extends RecyclerView.ViewHolder {
        private ItemUserBinding binding;
        UserHolder(ItemUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}