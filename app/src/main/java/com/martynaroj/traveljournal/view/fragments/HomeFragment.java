package com.martynaroj.traveljournal.view.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.firestore.Query;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.FragmentHomeBinding;
import com.martynaroj.traveljournal.services.models.Itinerary;
import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.view.adapters.ViewFlipperTravelsAdapter;
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;
import com.martynaroj.traveljournal.viewmodels.ItineraryViewModel;
import com.martynaroj.traveljournal.viewmodels.UserViewModel;

import java.util.List;

public class HomeFragment extends BaseFragment implements View.OnClickListener {

    private FragmentHomeBinding binding;
    private UserViewModel userViewModel;
    private ItineraryViewModel itineraryViewModel;
    private User user;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initViewModels();
        setListeners();
        observeUserChanges();

        return view;
    }


    //INIT DATA-------------------------------------------------------------------------------------


    private void initViewModels() {
        if (getActivity() != null) {
            userViewModel = new ViewModelProvider(getActivity()).get(UserViewModel.class);
            itineraryViewModel = new ViewModelProvider(getActivity()).get(ItineraryViewModel.class);
        }
    }


    private void loadItineraries() {
        startProgressBar();
        itineraryViewModel.getItinerariesOrderBy(user, 5, Constants.DB_POPULARITY, Query.Direction.DESCENDING);
        itineraryViewModel.getItinerariesList().observe(getViewLifecycleOwner(), list -> {
            if (list != null)
                initExploreTravelsAdapter(list);
            stopProgressBar();
        });
    }


    private void initExploreTravelsAdapter(List<Itinerary> itineraries) {
        ViewFlipperTravelsAdapter adapter = new ViewFlipperTravelsAdapter(getContext(), itineraries, true);
        binding.homeExploreViewpager.setAdapter(adapter);
        binding.homeExploreViewpager.setPadding(75, 0, 75, 0);
        adapter.setOnItemClickListener((object, position, view) -> {
            changeFragment(TravelFragment.newInstance((Itinerary) object, user));
        });
    }


    private void observeUserChanges() {
        userViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            this.user = user;
            loadItineraries();
        });
    }


    //LISTENERS-------------------------------------------------------------------------------------


    private void setListeners() {
        binding.homeSearchFriendsButton.setOnClickListener(this);
        binding.homeExploreMapButton.setOnClickListener(this);
        binding.homeExploreMoreButton.setOnClickListener(this);
        binding.homeSearchTravelsButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.home_search_friends_button:
                changeFragment(SearchFriendsFragment.newInstance());
                break;
            case R.id.home_explore_map_button:
                changeFragment(PlanToVisitFragment.newInstance());
                break;
            case R.id.home_explore_more_button:
                changeFragment(ExploreMoreTravelsFragment.newInstance(user));
                break;
            case R.id.home_search_travels_button:
                changeFragment(SearchTravelsFragment.newInstance(user));
                break;
        }
    }


    //OTHERS----------------------------------------------------------------------------------------


    private void changeFragment(Fragment next) {
        getNavigationInteractions().changeFragment(this, next, true);
    }


    private void showSnackBar(String message, int duration) {
        getSnackBarInteractions().showSnackBar(binding.getRoot(), getActivity(), message, duration);
    }


    private void startProgressBar() {
        getProgressBarInteractions().startProgressBar(binding.getRoot(),
                binding.homeProgressbarLayout, binding.homeProgressbar);
    }


    private void stopProgressBar() {
        getProgressBarInteractions().stopProgressBar(binding.getRoot(),
                binding.homeProgressbarLayout, binding.homeProgressbar);
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
