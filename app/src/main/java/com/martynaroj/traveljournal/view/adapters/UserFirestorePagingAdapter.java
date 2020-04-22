package com.martynaroj.traveljournal.view.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.ItemUserBinding;
import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.view.interfaces.OnItemClickListener;
import com.martynaroj.traveljournal.view.others.enums.Privacy;

import java.util.Objects;

public class UserFirestorePagingAdapter extends FirestorePagingAdapter<User, UserFirestorePagingAdapter.UserViewHolder> {

    private OnItemClickListener listener;
    private Context context;

    public UserFirestorePagingAdapter(FirestorePagingOptions<User> options, Context context) {
        super(options);
        this.context = context;
    }


    @Override
    protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull User model) {
        holder.binding.userItemUsername.setText(model.getUsername());
        if (model.getPrivacyEmail() == Privacy.PUBLIC.ordinal()) {
            holder.binding.userItemEmail.setText(model.getEmail());
        } else {
            holder.binding.userItemEmail.setVisibility(View.GONE);
        }
        Glide.with(context)
                .load(model.getPhoto())
                .placeholder(R.drawable.default_avatar)
                .into(holder.binding.userItemImage);
        holder.binding.userItem.setOnClickListener(view -> listener.onItemClick(
                Objects.requireNonNull(getItem(position)).toObject(User.class),
                position,
                holder.binding.userItem));
        holder.binding.userItemDeleteButton.setVisibility(View.GONE);
    }


    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUserBinding binding = ItemUserBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new UserViewHolder(binding);
    }


    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.listener = onItemClickListener;
    }


    static class UserViewHolder extends RecyclerView.ViewHolder {
        private ItemUserBinding binding;

        UserViewHolder(ItemUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}
