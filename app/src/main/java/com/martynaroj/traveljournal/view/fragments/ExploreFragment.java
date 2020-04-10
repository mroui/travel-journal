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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.snackbar.Snackbar;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.FragmentExploreBinding;
import com.martynaroj.traveljournal.services.models.Address;
import com.martynaroj.traveljournal.services.models.placesAPI.Place;
import com.martynaroj.traveljournal.services.others.GooglePlaces;
import com.martynaroj.traveljournal.view.adapters.MarkerInfoAdapter;
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.view.others.classes.RequestPermissionsHandler;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;
import com.martynaroj.traveljournal.viewmodels.AddressViewModel;
import com.martynaroj.traveljournal.viewmodels.PlaceViewModel;
import com.martynaroj.traveljournal.viewmodels.UserViewModel;

import java.util.List;
import java.util.Objects;

public class ExploreFragment extends BaseFragment implements View.OnClickListener, OnMapReadyCallback {

    private FragmentExploreBinding binding;
    private UserViewModel userViewModel;
    private AddressViewModel addressViewModel;
    private PlaceViewModel placeViewModel;
    private Address destination;

    private GoogleMap map;
    private FindCurrentPlaceRequest request;
    private PlacesClient placesClient;
    private MarkerInfoAdapter markerInfoAdapter;
    private Marker clickedMarker;


    public static ExploreFragment newInstance(Address destination) {
        ExploreFragment fragment = new ExploreFragment();
        Bundle args = new Bundle();
        args.putSerializable(Constants.BUNDLE_DESTINATION, destination);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            destination = (Address) getArguments().getSerializable(Constants.BUNDLE_DESTINATION);
        }
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentExploreBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initViewModels();
        initGoogleMap();
        initGooglePlaces();
        initMarkerInfoAdapter();
        setListeners();

        observeUserChanges();

        return view;
    }


    //INIT DATA-------------------------------------------------------------------------------------


    private void initViewModels() {
        if (getActivity() != null) {
            userViewModel = new ViewModelProvider(getActivity()).get(UserViewModel.class);
            addressViewModel = new ViewModelProvider(getActivity()).get(AddressViewModel.class);
            placeViewModel = new ViewModelProvider(getActivity()).get(PlaceViewModel.class);
        }
    }


    private void initGoogleMap() {
        ((SupportMapFragment) Objects.requireNonNull(getChildFragmentManager()
                .findFragmentById(R.id.explore_google_map)))
                .getMapAsync(this);
    }


    private void initGooglePlaces() {
        if (getContext() != null) {
            GooglePlaces.init(getContext());
            placesClient = GooglePlaces.initClient(getContext());
            request = GooglePlaces.initRequest();
        }
    }


    private void initMarkerInfoAdapter() {
        markerInfoAdapter = new MarkerInfoAdapter(getContext());
    }


    private void initLocation() {
        if (RequestPermissionsHandler.isFineLocationGranted(getContext()))
            detectLocation();
        else
            RequestPermissionsHandler.requestFineLocation(this);
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
        binding.exploreArrowButton.setOnClickListener(this);
        binding.exploreNearbyPlacesButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.explore_arrow_button:
                back();
                break;
            case R.id.explore_nearby_places_button:
                searchNearbyPlaces();
                binding.exploreNearbyPlacesButton.setEnabled(false);
                break;
        }
    }


    private void setMapListener() {
        map.setOnMarkerClickListener(marker -> {
            if (clickedMarker != null && clickedMarker.equals(marker)) {
                marker.hideInfoWindow();
                clickedMarker = null;
                return true;
            } else {
                marker.showInfoWindow();
                clickedMarker = marker;
            }
            return false;
        });
    }


    //MAP-------------------------------------------------------------------------------------------


    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setInfoWindowAdapter(markerInfoAdapter);
        initLocation();
        setMapListener();
    }


    private void zoomMap(LatLng latLng, float value) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, value));
    }


    private void detectLocation() {
        map.setMyLocationEnabled(true);
        startProgressBar();
        addressViewModel.detectAddress(placesClient, request);
        addressViewModel.getDetectedAddress().observe(getViewLifecycleOwner(), response -> {
            if (response != null) {
                com.google.android.libraries.places.api.model.Place place = response
                        .getPlaceLikelihoods().get(0).getPlace();
                if (place.getLatLng() != null) {
                    destination = new Address(
                            place.getName(),
                            place.getAddress(),
                            place.getLatLng().latitude,
                            place.getLatLng().longitude
                    );
                    zoomMap(place.getLatLng(), 15.0f);
                }
            } else
                showSnackBar(getResources().getString(R.string.messages_error_localize), Snackbar.LENGTH_LONG);
            stopProgressBar();
        });
    }


    private Marker addMarkerOnMap(LatLng latLng) {
        MarkerOptions currentMarkerOptions = new MarkerOptions()
                .icon(BitmapDescriptorFactory.defaultMarker())
                .position(latLng);
        return map.addMarker(currentMarkerOptions);
    }


    private void addMarkersOnMap(List<Place> places) {
        for (Place place : places) {
            LatLng latLng = new LatLng(place.getGeometry().getLocation().getLat(),
                    place.getGeometry().getLocation().getLng());
            Marker marker = addMarkerOnMap(latLng);
            updateMarkerPlaceData(marker, place.getName(), place.getVicinity(), place.getRating());
        }
    }


    private void updateMarkerPlaceData(Marker marker, String name, String address, Double rating) {
        marker.setTitle(name);
        StringBuilder snippet = new StringBuilder();
        if (address != null)
            snippet.append(address).append("\n");
        if (rating != null)
            snippet.append("Rating: ").append(rating).append("\n");
        marker.setSnippet(snippet.toString());
    }


    private void searchNearbyPlaces() {
        startProgressBar();
        LatLng latLng = new LatLng(destination.getLatitude(), destination.getLongitude());
        placeViewModel.getPlaces(latLng, null);
        placeViewModel.getPlacesResultData().observe(getViewLifecycleOwner(), placesResult -> {
            if (placesResult != null) {
                if (placesResult.getPlaces() != null && !placesResult.getPlaces().isEmpty())
                    addMarkersOnMap(placesResult.getPlaces());
                else
                    showSnackBar(getResources().getString(R.string.messages_no_places_results), Snackbar.LENGTH_LONG);
                zoomMap(latLng, 15.0f);
            } else
                showSnackBar(getResources().getString(R.string.messages_error_localize), Snackbar.LENGTH_LONG);
            stopProgressBar();
        });
    }


    //OTHERS----------------------------------------------------------------------------------------


    private void back() {
        if (getParentFragmentManager().getBackStackEntryCount() > 0)
            getParentFragmentManager().popBackStack();
    }


    private void startProgressBar() {
        getProgressBarInteractions().startProgressBar(binding.getRoot(),
                binding.exploreProgressbarLayout, binding.exploreProgressbar);
    }


    private void stopProgressBar() {
        getProgressBarInteractions().stopProgressBar(binding.getRoot(),
                binding.exploreProgressbarLayout, binding.exploreProgressbar);
    }


    private void showSnackBar(String message, int duration) {
        getSnackBarInteractions().showSnackBar(binding.getRoot(), getActivity(), message, duration);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (RequestPermissionsHandler.isOnResultGranted(requestCode, grantResults))
            detectLocation();
        else
            zoomMap(new LatLng(destination.getLatitude(), destination.getLongitude()), 15.0f);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
