package com.martynaroj.traveljournal.view.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.UserItemBinding;
import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.view.others.enums.Privacy;

public class UserAdapter extends FirestoreRecyclerAdapter<User, UserAdapter.UserViewHolder> {

    private OnItemClickListener listener;
    private Context context;

    public UserAdapter(@NonNull FirestoreRecyclerOptions<User> options, Context context) {
        super(options);
        this.context = context;
    }


    @Override
    protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull User model) {
        holder.username.setText(model.getUsername());
        if (model.getPrivacyEmail() == Privacy.PUBLIC.ordinal()) {
            holder.email.setText(model.getEmail());
        } else {
            holder.email.setVisibility(View.GONE);
        }
        Glide.with(context)
                .load(model.getPhoto())
                .placeholder(R.drawable.default_avatar)
                .into(holder.image);
    }


    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = UserItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false).getRoot();
        return new UserViewHolder(view);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.listener = onItemClickListener;
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {

        public TextView username, email;
        public ImageView image;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.user_item_username);
            image = itemView.findViewById(R.id.user_item_image);
            email = itemView.findViewById(R.id.user_item_email);
            itemView.findViewById(R.id.user_item).setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(getSnapshots().getSnapshot(position), position);
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

}
