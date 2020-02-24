package com.martynaroj.traveljournal.view.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.FragmentHomeBinding;
import com.martynaroj.traveljournal.services.models.Travel;
import com.martynaroj.traveljournal.view.adapters.ExploreTravelsAdapter;
import com.martynaroj.traveljournal.view.base.BaseFragment;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends BaseFragment {

    private FragmentHomeBinding binding;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initExploreTravelsAdapter();

        return view;
    }


    private void initExploreTravelsAdapter() {
        List<Travel> travels = new ArrayList<>();
        travels.add(new Travel(R.drawable.default_avatar, "title1", "desc"));
        travels.add(new Travel(R.drawable.default_avatar, "title2", "desc"));
        travels.add(new Travel(R.drawable.default_avatar, "title3", "desc"));
        travels.add(new Travel(R.drawable.default_avatar, "title4", "desc"));
        travels.add(new Travel(R.drawable.default_avatar, "title5", "desc"));

        ExploreTravelsAdapter adapter = new ExploreTravelsAdapter(getContext(), travels, true);
        binding.homeExploreViewpager.setAdapter(adapter);
        binding.homeExploreViewpager.setPadding(75, 0, 75, 0);
    }


    @Override
    public void onResume() {
        super.onResume();
        binding.homeExploreViewpager.resumeAutoScroll();
    }


    @Override
    public void onPause() {
        binding.homeExploreViewpager.pauseAutoScroll();
        super.onPause();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
