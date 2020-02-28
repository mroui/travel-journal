package com.martynaroj.traveljournal.view.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.asksira.loopingviewpager.LoopingPagerAdapter;
import com.martynaroj.traveljournal.databinding.HomeExploreItemBinding;
import com.martynaroj.traveljournal.services.models.Travel;

import java.util.List;

public class ExploreTravelsAdapter extends LoopingPagerAdapter<Travel> {

    private HomeExploreItemBinding binding;
    private List<Travel> travels;


    public ExploreTravelsAdapter(Context context, List<Travel> list, boolean isInfinite) {
        super(context, list, isInfinite);
        travels = list;
    }


    @Override
    protected View inflateView(int viewType, ViewGroup container, int listPosition) {
        binding = HomeExploreItemBinding.inflate(LayoutInflater.from(context), container, false);
        return binding.getRoot();
    }


    @Override
    public int getCount() {
        return travels.size();
    }


    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    @Override
    protected void bindView(View convertView, int position, int viewType) {
        binding.homeExploreItemImage.setImageResource(travels.get(position).getImage());
        binding.homeExploreItemTitle.setText(travels.get(position).getTitle());
        binding.homeExploreItemDesc.setText(travels.get(position).getDesc());
        convertView.setOnClickListener(v -> Toast.makeText(context, travels.get(position).getTitle(), Toast.LENGTH_SHORT).show());
    }


    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

}
