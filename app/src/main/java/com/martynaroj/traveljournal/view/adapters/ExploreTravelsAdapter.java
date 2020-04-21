package com.martynaroj.traveljournal.view.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.asksira.loopingviewpager.LoopingPagerAdapter;
import com.bumptech.glide.Glide;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.ItemViewFlipperTravelBinding;
import com.martynaroj.traveljournal.services.models.Itinerary;
import com.martynaroj.traveljournal.view.interfaces.OnItemClickListener;

import java.util.List;

public class ExploreTravelsAdapter extends LoopingPagerAdapter<Itinerary> {

    private ItemViewFlipperTravelBinding binding;
    private List<Itinerary> itineraries;
    private OnItemClickListener listener;


    public ExploreTravelsAdapter(Context context, List<Itinerary> list, boolean isInfinite) {
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


    @Override
    public int getCount() {
        return itineraries.size();
    }


    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    @Override
    protected void bindView(View convertView, int position, int viewType) {
        Glide.with(context).load(itineraries.get(position).getImage()).fitCenter()
                .placeholder(R.drawable.no_image).centerCrop()
                .into(binding.travelViewFlipperItemImage);
        binding.travelViewFlipperItemTitle.setText(itineraries.get(position).getName());
        binding.travelViewFlipperItemDesc.setText(itineraries.get(position).getDestination().replace("&", ", "));
        binding.travelViewFlipperItem.setOnClickListener(v -> {
            listener.onItemClick(itineraries.get(position), position, binding.travelViewFlipperItem);
        });
    }


    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

}
