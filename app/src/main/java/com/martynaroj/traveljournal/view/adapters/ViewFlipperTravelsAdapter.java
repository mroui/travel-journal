package com.martynaroj.traveljournal.view.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.asksira.loopingviewpager.LoopingPagerAdapter;
import com.bumptech.glide.Glide;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.ItemViewFlipperTravelBinding;
import com.martynaroj.traveljournal.services.models.Itinerary;
import com.martynaroj.traveljournal.view.interfaces.OnItemClickListener;

import java.util.List;

public class ViewFlipperTravelsAdapter extends LoopingPagerAdapter<Itinerary> {

    private ItemViewFlipperTravelBinding binding;
    private List<Itinerary> itineraries;
    private OnItemClickListener listener;


    public ViewFlipperTravelsAdapter(Context context, List<Itinerary> list, boolean isInfinite) {
        super(context, list, isInfinite);
        itineraries = list;
    }


    public void setOnItemClickListener(OnItemClickListener onItemLongClickListener) {
        this.listener = onItemLongClickListener;
    }


    @Override
    protected View inflateView(int viewType, ViewGroup container, int listPosition) {
        binding = ItemViewFlipperTravelBinding.inflate(LayoutInflater.from(context), container, false);
        return binding.getRoot();
    }


    @SuppressLint("SetTextI18n")
    @Override
    protected void bindView(View convertView, int position, int viewType) {
        Itinerary itinerary = itineraries.get(position);
        if (itinerary != null) {
            Glide.with(context).load(itinerary.getImage()).fitCenter()
                    .placeholder(R.drawable.no_image).centerCrop()
                    .into(binding.travelViewFlipperItemImage);
            binding.travelViewFlipperItemName.setText(itinerary.getName());
            binding.travelViewFlipperItemAddress.setText(itinerary.getDestination().replace("&", ", "));
            binding.travelViewFlipperItemDate.setText(itinerary.getDateString(itinerary.getCreatedDate()));
            binding.travelViewFlipperItemPopularity.setText(itinerary.getPopularity() + "");
            binding.travelViewFlipperItem.setOnClickListener(v ->
                    listener.onItemClick(itinerary, position, binding.travelViewFlipperItem)
            );
        }
    }

}
