package com.martynaroj.traveljournal.view.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.FragmentPlacesBinding;
import com.martynaroj.traveljournal.services.models.Day;
import com.martynaroj.traveljournal.services.models.Place;
import com.martynaroj.traveljournal.services.models.Travel;
import com.martynaroj.traveljournal.view.adapters.PhotoAdapter;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlacesFragment extends NotesFragment {

    private FragmentPlacesBinding binding;
    private Travel travel;
    private List<Place> places;
    private PhotoAdapter adapter;


    public static PlacesFragment newInstance(Travel travel, Day day, List<Day> days) {
        PlacesFragment fragment = new PlacesFragment();
        Bundle args = new Bundle();
        args.putSerializable(Constants.BUNDLE_TRAVEL, travel);
        args.putSerializable(Constants.BUNDLE_DAY, day);
        args.putSerializable(Constants.BUNDLE_DAYS, (Serializable) days);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            travel = (Travel) getArguments().getSerializable(Constants.BUNDLE_TRAVEL);
        }
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPlacesBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initViewModels();
        initContentData();
        setListeners();
        observeUserChanges(view);

        return view;
    }


    //INIT DATA-------------------------------------------------------------------------------------


    private void initContentData() {
        places = getAllDaysPlacesList();
        initListAdapter();
        setBindingData();
    }


    private void initListAdapter() {
        if (getContext() != null) {
            //
        }
    }


    private void setBindingData() {
        binding.setIsListEmpty(places.size() == 0);
    }


    //LISTENERS-------------------------------------------------------------------------------------


    private void setListeners() {
        binding.placesArrowButton.setOnClickListener(this);
        binding.placesAddFloatingButton.setOnClickListener(this);
        setOnListScrollListener(binding.placesListRecyclerView, binding.placesAddFloatingButton);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.places_arrow_button:
                back();
                break;
            case R.id.places_add_floating_button:
                break;
        }
    }


    //PHOTOS / LIST---------------------------------------------------------------------------------


    private List<Place> getAllDaysPlacesList() {
        List<Place> list = new ArrayList<>();
        for (Day day : days)
            list.addAll(day.getPlaces());
        Collections.sort(list);
        Collections.reverse(list);
        return list;
    }


    //DIALOG----------------------------------------------------------------------------------------


    //


    //OTHERS----------------------------------------------------------------------------------------


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
