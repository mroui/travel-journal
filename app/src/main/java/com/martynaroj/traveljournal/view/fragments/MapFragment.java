package com.martynaroj.traveljournal.view.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.martynaroj.traveljournal.databinding.FragmentMapBinding;
import com.martynaroj.traveljournal.view.base.BaseFragment;

public class MapFragment extends BaseFragment {

    private FragmentMapBinding binding;

    public static MapFragment newInstance() {
        return new MapFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMapBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        return view;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
