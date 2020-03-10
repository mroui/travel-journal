package com.martynaroj.traveljournal.view.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.martynaroj.traveljournal.databinding.FragmentTranslateBinding;
import com.martynaroj.traveljournal.view.base.BaseFragment;

public class TranslateFragment extends BaseFragment {

    private FragmentTranslateBinding binding;

    public static TranslateFragment newInstance() {
        return new TranslateFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTranslateBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        return view;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
