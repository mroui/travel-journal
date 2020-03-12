package com.martynaroj.traveljournal.view.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.FragmentMapBinding;
import com.martynaroj.traveljournal.view.adapters.MarkerInfoAdapter;
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;
import com.martynaroj.traveljournal.viewmodels.AddressViewModel;
import com.martynaroj.traveljournal.viewmodels.UserViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MapFragment extends BaseFragment implements View.OnClickListener, OnMapReadyCallback {

    private FragmentMapBinding binding;
    private AddressViewModel addressViewModel;
    private UserViewModel userViewModel;

    private GoogleMap map;
    private FindCurrentPlaceRequest request;
    private PlacesClient placesClient;
    private AutocompleteSupportFragment autocompleteFragment;
    private MarkerInfoAdapter markerInfoAdapter;

    private Place deviceLocation;
    private Marker temporaryMarker;
    private Marker clickedMarker;

    private boolean areSavedPlacesShown;

    private List<MarkerOptions> savedPlacesMarkersOptions;
    private List<Marker> savedPlacesMarkers;

    public static MapFragment newInstance() {
        return new MapFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMapBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initViewModels();
        initLoggedUser();
        initGoogleMap();
        initGooglePlaces();
        initMarkerInfoAdapter();
        setListeners();

        return view;
    }


    //INIT DATA-------------------------------------------------------------------------------------


    private void initViewModels() {
        if (getActivity() != null) {
            addressViewModel = new ViewModelProvider(getActivity()).get(AddressViewModel.class);
            userViewModel = new ViewModelProvider(getActivity()).get(UserViewModel.class);
        }
    }

    private void initLoggedUser() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            startProgressBar();
            userViewModel.getUserData(firebaseAuth.getCurrentUser().getUid());
            userViewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
                if (user != null) {
                    if (user.getMarkers() != null && !user.getMarkers().isEmpty()) {
                        initSavedPlacesMarkers(user.getMarkers());
                    } else {
                        binding.mapSavedPlacesButton.setEnabled(false);
                    }
                } else {
                    showSnackBar(getResources().getString(R.string.messages_error_current_user_not_available), Snackbar.LENGTH_LONG);
                }
                stopProgressBar();
            });
        }
    }

    private void initSavedPlacesMarkers(List<com.martynaroj.traveljournal.services.models.Marker> markers) {
        savedPlacesMarkersOptions = new ArrayList<>();
        savedPlacesMarkers = new ArrayList<>();

        for (com.martynaroj.traveljournal.services.models.Marker marker : markers) {
            MarkerOptions options = new MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker(marker.getColor()))
                    .position(new LatLng(marker.getLatitude(), marker.getLongitude()))
                    .title(marker.getDescription());
            savedPlacesMarkersOptions.add(options);
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

            autocompleteFragment = (AutocompleteSupportFragment) getChildFragmentManager()
                    .findFragmentById(R.id.map_search_view);
            if (autocompleteFragment != null && autocompleteFragment.getView() != null) {
                ((EditText) autocompleteFragment.getView().findViewById(R.id.places_autocomplete_search_input))
                        .setTextSize(14.0f);
                ((EditText) autocompleteFragment.getView().findViewById(R.id.places_autocomplete_search_input))
                        .setTypeface(ResourcesCompat.getFont(getContext(), R.font.raleway_medium));
                autocompleteFragment.getView().findViewById(R.id.places_autocomplete_search_button)
                        .setVisibility(View.GONE);
                autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME,
                        Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.OPENING_HOURS,
                        Place.Field.PHONE_NUMBER, Place.Field.RATING));
            }
        }
    }


    private void initMarkerInfoAdapter() {
        markerInfoAdapter = new MarkerInfoAdapter(getContext());
    }


    private void initLocation() {
        if (getContext() != null && ContextCompat.checkSelfPermission(getContext(),
                ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            detectLocation();
        } else if (getActivity() != null) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Constants.RC_ACCESS_FINE_LOCATION);
        }
    }


    //LISTENERS-------------------------------------------------------------------------------------


    private void setMapListener() {
        map.setOnMapClickListener(latLng -> {
            addMarkerOnMap(latLng);
            autocompleteFragment.setText("");
            if (areSavedPlacesShown) showSavedPlaces();
        });
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


    private void setListeners() {
        binding.mapArrowButton.setOnClickListener(this);
        binding.mapSavedPlacesButton.setOnClickListener(this);
        binding.mapNearbyPlacesButton.setOnClickListener(this);
        setAutoCompleteListeners();
    }


    private void setAutoCompleteListeners() {
        if (autocompleteFragment != null && autocompleteFragment.getView() != null) {
            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    LatLng latLng = place.getLatLng();
                    if (latLng != null) {
                        addMarkerOnMap(latLng);
                        updateMarkerData(place);
                        zoomMap(latLng);
                        if (areSavedPlacesShown) showSavedPlaces();
                        temporaryMarker = null;
                    }
                }

                @Override
                public void onError(@NonNull Status status) {
                }
            });
            autocompleteFragment.getView()
                    .findViewById(R.id.places_autocomplete_clear_button)
                    .setOnClickListener(view -> autocompleteFragment.setText(""));
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.map_arrow_button:
                if (getParentFragmentManager().getBackStackEntryCount() > 0)
                    getParentFragmentManager().popBackStack();
                break;
            case R.id.map_saved_places_button:
                checkSavedPlaces();
                break;
            case R.id.map_nearby_places_button:
                //TODO
                break;
        }
    }


    //MAP-------------------------------------------------------------------------------------------


    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setInfoWindowAdapter(markerInfoAdapter);
        initLocation();
        setMapListener();
    }


    private void addMarkerOnMap(LatLng latLng) {
        map.clear();
        MarkerOptions currentMarkerOptions = new MarkerOptions()
                .icon(BitmapDescriptorFactory.defaultMarker())
                .alpha(0.3f)
                .position(latLng);
        temporaryMarker = map.addMarker(currentMarkerOptions);
        map.animateCamera(CameraUpdateFactory.newLatLng(currentMarkerOptions.getPosition()), 250, null);
    }


    private void updateMarkerData(Place place) {
        temporaryMarker.setTitle(place.getName());
        StringBuilder snippet = new StringBuilder();
        if (place.getAddress() != null)
            snippet.append(place.getAddress()).append("\n");
        if (place.getOpeningHours() != null)
            for (String day : place.getOpeningHours().getWeekdayText()) {
                snippet.append("\t\t").append(day).append("\n");
            }
        if (place.getPhoneNumber() != null)
            snippet.append("Phone number: ").append(place.getPhoneNumber()).append("\n");
        if (place.getRating() != null)
            snippet.append("Rating: ").append(place.getRating()).append("\n");
        temporaryMarker.setSnippet(snippet.toString());
        temporaryMarker.showInfoWindow();
        clickedMarker = temporaryMarker;
    }


    private void detectLocation() {
        map.setMyLocationEnabled(true);
        startProgressBar();
        addressViewModel.detectAddress(placesClient, request);
        addressViewModel.getDetectedAddress().observe(getViewLifecycleOwner(), response -> {
            if (response != null) {
                deviceLocation = response.getPlaceLikelihoods().get(0).getPlace();
                if (deviceLocation.getLatLng() != null) {
                    zoomMap(deviceLocation.getLatLng());
                }
                stopProgressBar();
            } else
                showSnackBar(getResources().getString(R.string.messages_error_localize), Snackbar.LENGTH_LONG);
        });
    }


    private void zoomMap(LatLng latLng) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 8.0f));
    }


    private void checkSavedPlaces() {
        if (areSavedPlacesShown) {
            hideSavedPlaces();
            binding.mapSavedPlacesButton.setText(getResources().getString(R.string.map_show_saved_places));
        } else {
            showSavedPlaces();
            binding.mapSavedPlacesButton.setText(getResources().getString(R.string.map_hide_saved_places));
        }
        areSavedPlacesShown = !areSavedPlacesShown;
    }


    private void hideSavedPlaces() {
        if (savedPlacesMarkers != null && !savedPlacesMarkers.isEmpty()) {
            for (Marker marker : savedPlacesMarkers) {
                marker.remove();
            }
        }
    }


    private void showSavedPlaces() {
        if (savedPlacesMarkersOptions != null && !savedPlacesMarkersOptions.isEmpty()) {
            for (MarkerOptions options : savedPlacesMarkersOptions) {
                Marker marker = map.addMarker(options);
                savedPlacesMarkers.add(marker);
            }
        }
    }


    //OTHERS----------------------------------------------------------------------------------------


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
