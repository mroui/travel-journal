package com.martynaroj.traveljournal.view.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.FragmentExploreMoreTravelsBinding;
import com.martynaroj.traveljournal.services.models.Itinerary;
import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;

public class ExploreMoreTravelsFragment extends SearchTravelsFragment implements View.OnClickListener {

    private FragmentExploreMoreTravelsBinding binding;


    public static ExploreMoreTravelsFragment newInstance(User user) {
        ExploreMoreTravelsFragment fragment = new ExploreMoreTravelsFragment();
        Bundle args = new Bundle();
        args.putSerializable(Constants.BUNDLE_USER, user);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user = (User) getArguments().getSerializable(Constants.BUNDLE_USER);
        }
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentExploreMoreTravelsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initViewModels();
        initContentData();
        setListeners();
        observeUserChanges();

        return view;
    }


    //INIT DATA-------------------------------------------------------------------------------------


    private void initContentData() {
        initSortingSpinner(binding.exploreMoreTravelsSortSpinner);
        loadList(true);
    }


    private void setIsListEmpty() {
        if (list != null)
            binding.setIsListEmpty(list.isEmpty());
        else
            binding.setIsListEmpty(true);
    }


    //LISTENERS-------------------------------------------------------------------------------------


    private void setListeners() {
        binding.exploreMoreTravelsArrowButton.setOnClickListener(this);
        binding.exploreMoreTravelsSortSpinner.setOnItemSelectedListener((view, position, id, item) -> {
            noChangeOrderOption = false;
            setQueryOrderDirection(binding.exploreMoreTravelsSortSpinner.getSelectedIndex());
            loadList(true);
        });
        setOnScrollListener();
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.explore_more_travels_arrow_button) {
            back();
        }
    }


    private void setOnScrollListener() {
        binding.exploreMoreTravelsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
                    isScrolling = true;
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemsCount = layoutManager.getChildCount();
                int totalItems = list.size();
                int scrollOutItems = layoutManager.findFirstVisibleItemPosition();

                if (isScrolling && (visibleItemsCount + scrollOutItems == totalItems)) {
                    isScrolling = false;
                    loadList(false);
                }
            }
        });
    }


    //LIST------------------------------------------------------------------------------------------


    private void loadList(boolean newAdapter) {
        if (newAdapter) {
            initListAdapter(binding.exploreMoreTravelsRecyclerView);
            startProgressBar();
        }
        itineraryViewModel.getDocumentsListStartAt(user, lastDocument, 5, queryOrderBy, queryDirection);
        itineraryViewModel.getDocumentsData().observe(getViewLifecycleOwner(), documentSnapshots -> {
            if (documentSnapshots != null && !documentSnapshots.isEmpty()) {
                for (DocumentSnapshot documentSnapshot : documentSnapshots) {
                    Itinerary i = documentSnapshot.toObject(Itinerary.class);
                    list.add(i);
                    adapter.notifyItemInserted(list.size() - 1);
                }
                lastDocument = documentSnapshots.get(documentSnapshots.size() - 1);
            }
            setIsListEmpty();
            stopProgressBar();
        });
    }


    //OTHERS----------------------------------------------------------------------------------------


    private void startProgressBar() {
        getProgressBarInteractions().startProgressBar(binding.getRoot(), binding.exploreMoreTravelsProgressbarLayout,
                binding.exploreMoreTravelsProgressbar);
    }


    private void stopProgressBar() {
        getProgressBarInteractions().stopProgressBar(binding.getRoot(), binding.exploreMoreTravelsProgressbarLayout,
                binding.exploreMoreTravelsProgressbar);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
