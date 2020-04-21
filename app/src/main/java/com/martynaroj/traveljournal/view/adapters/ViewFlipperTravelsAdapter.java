package com.martynaroj.traveljournal.view.adapters;

import android.annotation.SuppressLint;
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


    @Override
    public int getCount() {
        return itineraries.size();
    }


    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }


    @SuppressLint("SetTextI18n")
    @Override
    protected void bindView(View convertView, int position, int viewType) {
        Glide.with(context).load(itineraries.get(position).getImage()).fitCenter()
                .placeholder(R.drawable.no_image).centerCrop()
                .into(binding.travelViewFlipperItemImage);
        binding.travelViewFlipperItemName.setText(itineraries.get(position).getName());
        binding.travelViewFlipperItemAddress.setText(itineraries.get(position).getDestination().replace("&", ", "));
        binding.travelViewFlipperItemDate.setText(itineraries.get(position).getDateString(itineraries.get(position).getCreatedDate()));
        binding.travelViewFlipperItemPopularity.setText(itineraries.get(position).getPopularity()+"");
        binding.travelViewFlipperItem.setOnClickListener(v -> {
            listener.onItemClick(itineraries.get(position), position, binding.travelViewFlipperItem);
        });
    }


    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

}
