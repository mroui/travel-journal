package com.martynaroj.traveljournal.view.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.FragmentSearchTravelsBinding;
import com.martynaroj.traveljournal.services.models.Itinerary;
import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.view.adapters.TravelAdapter;
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.view.others.enums.Sort;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;
import com.martynaroj.traveljournal.viewmodels.ItineraryViewModel;
import com.martynaroj.traveljournal.viewmodels.UserViewModel;

import java.util.ArrayList;
import java.util.List;

public class SearchTravelsFragment extends BaseFragment implements View.OnClickListener {

    private FragmentSearchTravelsBinding binding;
    private UserViewModel userViewModel;
    private ItineraryViewModel itineraryViewModel;

    private User user;
    private List<Itinerary> list;


    public static SearchTravelsFragment newInstance(User user) {
        SearchTravelsFragment fragment = new SearchTravelsFragment();
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
        binding = FragmentSearchTravelsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initViewModels();
        initContentData();
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


    private void initContentData() {
        initSortingSpinner();
    }


    private void initSortingSpinner() {
        List<String> options = new ArrayList<>();
        for (Sort e : Sort.values())
            options.add(e.getValue());
        binding.searchTravelsSortSpinner.setItems(options);
    }


    private void setIsListEmpty() {
        if (list != null)
            binding.setIsListEmpty(list.isEmpty());
        else
            binding.setIsListEmpty(true);
    }


    private void observeUserChanges() {
        userViewModel.getUser().observe(getViewLifecycleOwner(), user -> this.user = user);
    }


    //LISTENERS-------------------------------------------------------------------------------------


    private void setListeners() {
        binding.searchTravelsArrowButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.search_travels_arrow_button:
                back();
                break;
        }
    }


    //ITINERARY LIST--------------------------------------------------------------------------------


    //


    //OTHERS----------------------------------------------------------------------------------------


    private void showSnackBar(String message, int duration) {
        getSnackBarInteractions().showSnackBar(binding.getRoot(), getActivity(), message, duration);
    }


    private void startProgressBar() {
        getProgressBarInteractions().startProgressBar(binding.getRoot(), binding.searchTravelsProgressbarLayout,
                binding.searchTravelsProgressbar);
    }


    private void stopProgressBar() {
        getProgressBarInteractions().stopProgressBar(binding.getRoot(), binding.searchTravelsProgressbarLayout,
                binding.searchTravelsProgressbar);
    }


    private void changeFragment(BaseFragment next) {
        getNavigationInteractions().changeFragment(getParentFragment(), next, true);
    }


    private void back() {
        if (getParentFragmentManager().getBackStackEntryCount() > 0)
            getParentFragmentManager().popBackStack();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
