package com.martynaroj.traveljournal.view.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.martynaroj.traveljournal.databinding.FragmentAlarmBinding;
import com.martynaroj.traveljournal.view.base.BaseFragment;

public class AlarmFragment extends BaseFragment {

    private FragmentAlarmBinding binding;

    public static AlarmFragment newInstance() {
        return new AlarmFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAlarmBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        return view;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
