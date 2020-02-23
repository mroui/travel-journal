package com.martynaroj.traveljournal.view.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.martynaroj.traveljournal.databinding.HomeExploreItemBinding;
import com.martynaroj.traveljournal.services.models.Travel;

import java.util.List;

public class ExploreTravelsAdapter extends PagerAdapter {

    private HomeExploreItemBinding binding;
    private List<Travel> travels;
    private Context context;


    public ExploreTravelsAdapter(List<Travel> travels, Context context) {
        this.travels = travels;
        this.context = context;
    }


    @Override
    public int getCount() {
        return travels.size();
    }


    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }


    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        binding = HomeExploreItemBinding.inflate(layoutInflater, container, false);
        View view = binding.getRoot();

        binding.homeExploreItemImage.setImageResource(travels.get(position).getImage());
        binding.homeExploreItemTitle.setText(travels.get(position).getTitle());
        binding.homeExploreItemDesc.setText(travels.get(position).getDesc());
        container.addView(view, 0);

        return view;
    }


    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

}
