package com.martynaroj.traveljournal.view.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.snackbar.Snackbar;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.FragmentMapBinding;
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;
import com.martynaroj.traveljournal.viewmodels.AddressViewModel;

import java.util.Arrays;
import java.util.Objects;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MapFragment extends BaseFragment implements View.OnClickListener, OnMapReadyCallback {

    private FragmentMapBinding binding;
    private GoogleMap map;
    private FindCurrentPlaceRequest request;
    private PlacesClient placesClient;
    private AddressViewModel addressViewModel;
    private Place currentPlace;

    public static MapFragment newInstance() {
        return new MapFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMapBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initViewModels();
        initGoogleMap();
        initGooglePlaces();
        setListeners();

        return view;
    }


    private void initViewModels() {
        if (getActivity() != null) {
            addressViewModel = new ViewModelProvider(getActivity()).get(AddressViewModel.class);
        }
    }


    private void initGoogleMap() {
        ((SupportMapFragment) Objects.requireNonNull(getChildFragmentManager()
                .findFragmentById(R.id.map_google_map)))
                .getMapAsync(this);
    }


    private void initGooglePlaces() {
        if (getContext() != null) {
            Places.initialize(getContext(), getString(R.string.google_api_key));
            placesClient = Places.createClient(getContext());
            request = FindCurrentPlaceRequest.newInstance(Arrays.asList(Place.Field.ID, Place.Field.NAME,
                    Place.Field.ADDRESS, Place.Field.LAT_LNG));
        }
    }


    private void initLocation() {
        if (getContext() != null && ContextCompat.checkSelfPermission(getContext(), ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            detectLocation();
        } else if (getActivity() != null) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Constants.RC_ACCESS_FINE_LOCATION);
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        initLocation();
    }


    private void detectLocation() {
        map.setMyLocationEnabled(true);
        startProgressBar();
        addressViewModel.detectAddress(placesClient, request);
        addressViewModel.getDetectedAddress().observe(getViewLifecycleOwner(), response -> {
            if (response != null) {
                currentPlace = response.getPlaceLikelihoods().get(0).getPlace();
                if (currentPlace.getLatLng() != null) {
                    zoomMap(currentPlace.getLatLng());
                }
                stopProgressBar();
            } else
                showSnackBar(getResources().getString(R.string.messages_error_localize), Snackbar.LENGTH_LONG);
        });
    }


    private void zoomMap(LatLng latLng) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 8.0f));
    }


    private void setListeners() {
        binding.mapArrowButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.map_arrow_button:
                if (getParentFragmentManager().getBackStackEntryCount() > 0)
                    getParentFragmentManager().popBackStack();
                break;
        }
    }


    private void startProgressBar() {
        getProgressBarInteractions().startProgressBar(binding.getRoot(),
                binding.mapProgressbarLayout, binding.mapProgressbar);
    }


    private void stopProgressBar() {
        getProgressBarInteractions().stopProgressBar(binding.getRoot(),
                binding.mapProgressbarLayout, binding.mapProgressbar);
    }


    private void showSnackBar(String message, int duration) {
        getSnackBarInteractions().showSnackBar(binding.getRoot(), getActivity(), message, duration);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Constants.RC_ACCESS_FINE_LOCATION && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            detectLocation();
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
