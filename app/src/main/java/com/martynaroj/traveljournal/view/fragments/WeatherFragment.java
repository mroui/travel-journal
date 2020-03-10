package com.martynaroj.traveljournal.view.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.martynaroj.traveljournal.databinding.FragmentWeatherBinding;
import com.martynaroj.traveljournal.view.base.BaseFragment;

public class WeatherFragment extends BaseFragment {

    private FragmentWeatherBinding binding;

    public static WeatherFragment newInstance() {
        return new WeatherFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWeatherBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        return view;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
