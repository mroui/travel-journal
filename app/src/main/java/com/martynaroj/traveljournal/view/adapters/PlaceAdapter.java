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
import com.martynaroj.traveljournal.databinding.ItemPlaceBinding;
import com.martynaroj.traveljournal.services.models.Place;
import com.martynaroj.traveljournal.view.interfaces.OnItemLongClickListener;
import com.martynaroj.traveljournal.view.others.enums.Emoji;

import java.util.ArrayList;
import java.util.List;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.PlaceHolder> {

    private Context context;
    private List<Place> places;
    private OnItemLongClickListener listener;


    public PlaceAdapter(Context context, List<Place> places) {
        this.context = context;
        this.places = places;
    }


    @NonNull
    @Override
    public PlaceAdapter.PlaceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPlaceBinding binding = ItemPlaceBinding.inflate(LayoutInflater.from(context), parent, false);
        return new PlaceAdapter.PlaceHolder(binding);
    }


    private Place getItem(int position) {
        return places.get(position);
    }


    @Override
    public void onBindViewHolder(@NonNull PlaceAdapter.PlaceHolder holder, int position) {
        final Place place = getItem(position);
        holder.binding.placeItemDate.setText(place.getDateTimeString());
        String[] nameAddress = place.getAddress().split("&");
        holder.binding.placeItemName.setText(nameAddress[0]);
        holder.binding.placeItemAddress.setText(nameAddress[1]);
        Glide.with(context).load(loadRateIcon(place)).fitCenter().into(holder.binding.placeItemRate);
        if (place.getDescription().isEmpty())
            holder.binding.placeItemDesc.setVisibility(View.GONE);
        else
            holder.binding.placeItemDesc.setText(place.getDescription());
        holder.binding.placeItem.setOnLongClickListener(view -> {
            listener.onItemLongClick(place, position, view);
            return true;
        });
    }


    private int loadRateIcon(Place place) {
        switch (Emoji.values()[place.getRate()]) {
            case HAPPY:
                return R.drawable.ic_emoji_happy_color;
            case NORMAL:
                return R.drawable.ic_emoji_normal_color;
            case SAD:
                return R.drawable.ic_emoji_sad_color;
            case LUCKY:
                return R.drawable.ic_emoji_lucky_color;
            case SHOCKED:
                return R.drawable.ic_emoji_shocked_color;
            case BORED:
                return R.drawable.ic_emoji_bored_color;
        }
        return R.drawable.ic_emoji_normal_color;
    }


    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.listener = onItemLongClickListener;
    }


    public void add(Place place) {
        places.add(0, place);
        notifyItemInserted(0);
        notifyItemRangeChanged(0, places.size());
    }


    public void remove(int position) {
        places.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, places.size());
    }


    public void edit(int position, Place place) {
        places.set(position, place);
        notifyItemChanged(position);
        notifyItemRangeChanged(position, places.size());
    }


    public List<Place> getList() {
        return places;
    }


    public List<Place> getTodayList() {
        List<Place> todayList = new ArrayList<>();
        for (Place p : places)
            if (DateUtils.isToday(p.getDate()))
                todayList.add(p);
        return todayList;
    }


    @Override
    public int getItemCount() {
        return places.size();
    }


    static class PlaceHolder extends RecyclerView.ViewHolder {
        private ItemPlaceBinding binding;

        PlaceHolder(ItemPlaceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}