package com.martynaroj.traveljournal.view.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.FragmentExploreMapBinding;
import com.martynaroj.traveljournal.services.models.Address;
import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.view.interfaces.IOnBackPressed;
import com.martynaroj.traveljournal.viewmodels.AddressViewModel;
import com.martynaroj.traveljournal.viewmodels.UserViewModel;

import java.util.Objects;

public class ExploreMapFragment extends BaseFragment implements View.OnClickListener, OnMapReadyCallback, IOnBackPressed {

    private FragmentExploreMapBinding binding;
    private UserViewModel userViewModel;
    private AddressViewModel addressViewModel;
    private User user;
    private LatLng currentPlace;
    private MarkerOptions currentMarker;
    private GoogleMap map;
    private Snackbar tutorialSnackbar;


    public static ExploreMapFragment newInstance() {
        return new ExploreMapFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentExploreMapBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initViewModels();
        setListeners();
        initLoggedUser();
        initGoogleMap();

        return view;
    }


    private void initViewModels() {
        if (getActivity() != null) {
            userViewModel = new ViewModelProvider(getActivity()).get(UserViewModel.class);
            addressViewModel = new ViewModelProvider(getActivity()).get(AddressViewModel.class);
        }
    }


    private void setListeners() {
        binding.exploreMapArrowButton.setOnClickListener(this);
        binding.exploreMapAddPlaceButton.setOnClickListener(this);
        binding.exploreMapRemovePlaceButton.setOnClickListener(this);
    }


    private void initLoggedUser() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            startProgressBar();
            userViewModel.getUserData(firebaseAuth.getCurrentUser().getUid());
            userViewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
                if (user != null) {
                    this.user = user;
                    initLocation();
                } else {
                    showSnackBar(getResources().getString(R.string.messages_error_current_user_not_available), Snackbar.LENGTH_LONG);
                    stopProgressBar();
                }
            });
        }
    }


    private void initGoogleMap() {
        ((SupportMapFragment) Objects.requireNonNull(getChildFragmentManager().findFragmentById(R.id.explore_map_google_map)))
                .getMapAsync(this);
    }


    private void initLocation() {
        if(user.getLocation() != null && !user.getLocation().isEmpty()) {
            startProgressBar();
            addressViewModel.getAddress(user.getLocation());
            addressViewModel.getAddressData().observe(getViewLifecycleOwner(), address -> {
                if (address != null) {
                    setAddressOnMap(address);
                }
                stopProgressBar();
            });
        } else
            stopProgressBar();
    }


    private void setAddressOnMap(Address address) {
        if (map != null) {
            LatLng place = new LatLng(address.getLatitude(), address.getLongitude());
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(place, 8.0f));
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        setMapListener();
        showTutorialSnackbar();
    }


    private void showTutorialSnackbar() {
        tutorialSnackbar = Snackbar.make(binding.getRoot(),
                getResources().getString(R.string.messages_explore_map_tutorial),
                Snackbar.LENGTH_INDEFINITE);
        tutorialSnackbar.setAction(getResources().getString(R.string.dialog_button_ok),
                view -> tutorialSnackbar.dismiss());
        tutorialSnackbar.show();
    }


    private void setMapListener() {
        map.setOnMapClickListener(p -> {
            map.clear();
            currentPlace = new LatLng(p.latitude, p.longitude);
            currentMarker = new MarkerOptions().position(currentPlace);
            map.addMarker(currentMarker);
        });
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.explore_map_arrow_button:
                if (getParentFragmentManager().getBackStackEntryCount() > 0)
                    getParentFragmentManager().popBackStack();
                tutorialSnackbar.dismiss();
                break;
            case R.id.explore_map_add_place_button:
                break;
            case R.id.explore_map_remove_place_button:
                break;
        }
    }


    @Override
    public boolean onBackPressed() {
        if (getParentFragmentManager().getBackStackEntryCount() > 0)
            getParentFragmentManager().popBackStack();
        tutorialSnackbar.dismiss();
        return true;
    }


    private void startProgressBar() {
        getProgressBarInteractions().startProgressBar(binding.getRoot(), binding.exploreMapProgressbarLayout, binding.exploreMapProgressbar);
    }


    private void stopProgressBar() {
        getProgressBarInteractions().stopProgressBar(binding.getRoot(), binding.exploreMapProgressbarLayout, binding.exploreMapProgressbar);
        disableButtons();
    }


    private void disableButtons() {
        binding.exploreMapRemovePlaceButton.setEnabled(false);
        binding.exploreMapAddPlaceButton.setEnabled(false);
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
