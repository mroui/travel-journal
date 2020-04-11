package com.martynaroj.traveljournal.view.adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.ItemPhotoBinding;
import com.martynaroj.traveljournal.services.models.Photo;
import com.martynaroj.traveljournal.view.interfaces.OnItemLongClickListener;

import java.util.ArrayList;
import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoHolder> {

    private Context context;
    private List<Photo> photos;
    private OnItemLongClickListener listener;


    public PhotoAdapter(Context context, List<Photo> photos) {
        this.context = context;
        this.photos = photos;
    }


    @NonNull
    @Override
    public PhotoAdapter.PhotoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPhotoBinding binding = ItemPhotoBinding.inflate(LayoutInflater.from(context), parent, false);
        return new PhotoAdapter.PhotoHolder(binding);
    }


    private Photo getItem(int position) {
        return photos.get(position);
    }


    @Override
    public void onBindViewHolder(@NonNull PhotoAdapter.PhotoHolder holder, int position) {
        final Photo photo = getItem(position);
        holder.binding.photoItemDate.setText(photo.getDateTimeString());
        if (photo.getDescription().isEmpty())
            holder.binding.photoItemDesc.setVisibility(View.GONE);
        else
            holder.binding.photoItemDesc.setText(photo.getDescription());
        Glide.with(context).load(photo.getSrc()).fitCenter()
                .placeholder(R.drawable.no_image).centerCrop()
                .into(holder.binding.photoItemPhoto);
        holder.binding.photoItem.setOnLongClickListener(view -> {
            listener.onItemLongClick(photo, position, view);
            return true;
        });
    }


    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.listener = onItemLongClickListener;
    }


    public void add(Photo photo) {
        photos.add(0, photo);
        notifyItemInserted(0);
        notifyItemRangeChanged(0, photos.size());
    }


    public void remove(int position) {
        photos.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, photos.size());
    }


    public void edit(int position, Photo photo) {
        photos.set(position, photo);
        notifyItemChanged(position);
        notifyItemRangeChanged(position, photos.size());
    }


    public List<Photo> getList() {
        return photos;
    }


    public List<Photo> getTodayList() {
        List<Photo> todayList = new ArrayList<>();
        for (Photo p : photos)
            if (DateUtils.isToday(p.getDate()))
                todayList.add(p);
        return todayList;
    }


    @Override
    public int getItemCount() {
        return photos.size();
    }


    static class PhotoHolder extends RecyclerView.ViewHolder {
        private ItemPhotoBinding binding;

        PhotoHolder(ItemPhotoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}