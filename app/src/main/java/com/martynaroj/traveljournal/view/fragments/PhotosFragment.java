package com.martynaroj.traveljournal.view.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.FragmentPhotosBinding;
import com.martynaroj.traveljournal.services.models.Day;
import com.martynaroj.traveljournal.services.models.Photo;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PhotosFragment extends NotesFragment {

    private FragmentPhotosBinding binding;
    private List<Photo> photos;


    public static PhotosFragment newInstance(Day day, List<Day> days) {
        PhotosFragment fragment = new PhotosFragment();
        Bundle args = new Bundle();
        args.putSerializable(Constants.BUNDLE_DAY, day);
        args.putSerializable(Constants.BUNDLE_DAYS, (Serializable) days);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPhotosBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initViewModels();
        initContentData();
        setListeners();
        observeUserChanges();

        return view;
    }


    //INIT DATA-------------------------------------------------------------------------------------


    private void initContentData() {
        photos = getAllDaysPhotosList();
    }


    //LISTENERS-------------------------------------------------------------------------------------


    private void setListeners() {
        binding.photosArrowButton.setOnClickListener(this);
        setOnListScrollListener();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.photos_arrow_button:
                back();
                break;
        }
    }


    private void setOnListScrollListener() {
        binding.photosListRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE)
                    binding.photosAddFloatingButton.show();
                else
                    binding.photosAddFloatingButton.hide();
            }
        });
    }


    //PHOTOS / LIST----------------------------------------------------------------------------------


    private List<Photo> getAllDaysPhotosList() {
        List<Photo> list = new ArrayList<>();
        for (Day day : days)
            list.addAll(day.getPhotos());
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
