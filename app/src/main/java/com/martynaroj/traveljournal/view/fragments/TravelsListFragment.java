package com.martynaroj.traveljournal.view.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.FragmentTravelsListBinding;
import com.martynaroj.traveljournal.services.models.Itinerary;
import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.view.adapters.TravelAdapter;
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;
import com.martynaroj.traveljournal.viewmodels.ItineraryViewModel;
import com.martynaroj.traveljournal.viewmodels.UserViewModel;

import java.util.List;

public class TravelsListFragment extends BaseFragment implements View.OnClickListener {

    private FragmentTravelsListBinding binding;
    private UserViewModel userViewModel;
    private ItineraryViewModel itineraryViewModel;

    private User user, loggedUser;
    private List<Itinerary> itineraries, savedItineraries;
    private TravelAdapter adapter;


    public static TravelsListFragment newInstance(User loggedUser, User user) {
        TravelsListFragment fragment = new TravelsListFragment();
        Bundle args = new Bundle();
        args.putSerializable(Constants.BUNDLE_USER, user);
        args.putSerializable(Constants.BUNDLE_LOGGED_USER, loggedUser);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user = (User) getArguments().getSerializable(Constants.BUNDLE_USER);
            loggedUser = (User) getArguments().getSerializable(Constants.BUNDLE_LOGGED_USER);
        }
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTravelsListBinding.inflate(inflater, container, false);
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
        loadUserTravels();
        if(loggedUser.equals(user)) {
            loadSavedTravels();
            binding.travelsListButtonsContainer.setVisibility(View.VISIBLE);
        } else
            binding.travelsListButtonsContainer.setVisibility(View.GONE);
    }


    private void observeUserChanges() {
        userViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            this.loggedUser = user;
            if (user == null) {
                showSnackBar(getResources().getString(R.string.messages_not_logged_user), Snackbar.LENGTH_LONG);
                back();
            }
        });
    }


    private void initListAdapter() {
        if (getContext() != null && itineraries != null) {
            adapter = new TravelAdapter(getContext(), itineraries);
            binding.travelsListRecyclerView.setAdapter(adapter);
        }
    }


    private void setBindingData(List<Itinerary> list) {
        if (list != null)
            binding.setIsListEmpty(list.size() == 0);
        else
            binding.setIsListEmpty(true);
    }


    //ITINERARY LIST--------------------------------------------------------------------------------


    private void loadUserTravels() {
        startProgressBar();
        itineraryViewModel.getItinerariesListData(user.getTravels());
        itineraryViewModel.getItinerariesList().observe(getViewLifecycleOwner(), itineraries -> {
            this.itineraries = itineraries;
            if (itineraries != null) {
                initListAdapter();
                setBindingData(itineraries);
            }
            stopProgressBar();
        });
    }


    private void loadSavedTravels() {
        startProgressBar();
        itineraryViewModel.getItinerariesListData(user.getSavedTravels());
        itineraryViewModel.getItinerariesList().observe(getViewLifecycleOwner(), itineraries -> {
            this.savedItineraries = itineraries;
            stopProgressBar();
        });
    }


    private void showTravels(List<Itinerary> list, boolean mine) {
        switchStyleButtons(mine);
        adapter.changeList(list);
        setBindingData(list);
    }


    private void switchStyleButtons(boolean mine) {
        int blue = getResources().getColor(R.color.main_blue);
        int white = getResources().getColor(R.color.white);
        if (mine) {
            binding.travelsListMyTravelsButton.setBackgroundColor(blue);
            binding.travelsListMyTravelsButton.setTextColor(white);
            binding.travelsListSavedTravelsButton.setBackgroundColor(white);
            binding.travelsListSavedTravelsButton.setTextColor(blue);
        } else {
            binding.travelsListMyTravelsButton.setBackgroundColor(white);
            binding.travelsListMyTravelsButton.setTextColor(blue);
            binding.travelsListSavedTravelsButton.setBackgroundColor(blue);
            binding.travelsListSavedTravelsButton.setTextColor(white);
        }
    }


    //LISTENERS-------------------------------------------------------------------------------------


    private void setListeners() {
        binding.travelsListArrowButton.setOnClickListener(this);
        binding.travelsListMyTravelsButton.setOnClickListener(this);
        binding.travelsListSavedTravelsButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.travels_list_arrow_button:
                back();
                break;
            case R.id.travels_list_my_travels_button:
                showTravels(itineraries, true);
                break;
            case R.id.travels_list_saved_travels_button:
                showTravels(savedItineraries, false);
                break;
        }
    }


    //OTHERS----------------------------------------------------------------------------------------


    private void showSnackBar(String message, int duration) {
        getSnackBarInteractions().showSnackBar(binding.getRoot(), getActivity(), message, duration);
    }


    private void startProgressBar() {
        getProgressBarInteractions().startProgressBar(binding.getRoot(), binding.travelsListProgressbarLayout,
                binding.travelsListProgressbar);
    }


    private void stopProgressBar() {
        getProgressBarInteractions().stopProgressBar(binding.getRoot(), binding.travelsListProgressbarLayout,
                binding.travelsListProgressbar);
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
