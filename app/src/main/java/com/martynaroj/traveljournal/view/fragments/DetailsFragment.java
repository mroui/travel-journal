package com.martynaroj.traveljournal.view.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.FragmentDetailsBinding;
import com.martynaroj.traveljournal.services.models.Address;
import com.martynaroj.traveljournal.services.models.Reservation;
import com.martynaroj.traveljournal.services.models.Travel;
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;
import com.martynaroj.traveljournal.viewmodels.ReservationViewModel;
import com.martynaroj.traveljournal.viewmodels.UserViewModel;

import java.util.ArrayList;
import java.util.Arrays;

public class DetailsFragment extends BaseFragment implements View.OnClickListener {

    private FragmentDetailsBinding binding;
    private UserViewModel userViewModel;
    private ReservationViewModel reservationViewModel;

    private Travel travel;
    private Address destination;
    private Reservation accommodation;
    private Reservation transport;


    public static DetailsFragment newInstance(Travel travel, Address destination) {
        DetailsFragment fragment = new DetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable(Constants.BUNDLE_TRAVEL, travel);
        args.putSerializable(Constants.BUNDLE_DESTINATION, destination);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            travel = (Travel) getArguments().getSerializable(Constants.BUNDLE_TRAVEL);
            destination = (Address) getArguments().getSerializable(Constants.BUNDLE_DESTINATION);
        }
    }



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDetailsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initViewModels();
        loadTravelDetails();
        setListeners();
        observeUserChanges();

        return view;
    }


    //INIT DATA-------------------------------------------------------------------------------------


    private void initViewModels() {
        if (getActivity() != null) {
            userViewModel = new ViewModelProvider(getActivity()).get(UserViewModel.class);
            reservationViewModel = new ViewModelProvider(getActivity()).get(ReservationViewModel.class);
        }
    }


    private void loadTravelDetails() {
        if (travel != null) {
            reservationViewModel.getReservations(
                    new ArrayList<>(Arrays.asList(travel.getAccommodation(), travel.getTransport()))
            );
            reservationViewModel.getReservationsList().observe(getViewLifecycleOwner(), reservations -> {
                if (reservations != null && reservations.size() > 0) {
                    if (reservations.get(0).getId().equals(travel.getAccommodation())) {
                        accommodation = reservations.get(0);
                        transport = reservations.get(1);
                    } else {
                        accommodation = reservations.get(1);
                        transport = reservations.get(0);
                    }
                }
            });
        }
    }


    private void observeUserChanges() {
        userViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user == null) {
                showSnackBar(getResources().getString(R.string.messages_not_logged_user), Snackbar.LENGTH_LONG);
                back();
            }
        });
    }


    //LISTENERS-------------------------------------------------------------------------------------


    private void setListeners() {
        binding.detailsArrowButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.details_arrow_button:
                back();
                break;
        }
    }


    //OTHERS----------------------------------------------------------------------------------------


    private void back() {
        if (getParentFragmentManager().getBackStackEntryCount() > 0)
            getParentFragmentManager().popBackStack();
    }


    private void startProgressBar() {
        getProgressBarInteractions().startProgressBar(binding.getRoot(),
                binding.detailsProgressbarLayout, binding.detailsProgressbar);
    }


    private void stopProgressBar() {
        getProgressBarInteractions().stopProgressBar(binding.getRoot(),
                binding.detailsProgressbarLayout, binding.detailsProgressbar);
    }


    private void showSnackBar(String message, int duration) {
        getSnackBarInteractions().showSnackBar(binding.getRoot(), getActivity(), message, duration);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
