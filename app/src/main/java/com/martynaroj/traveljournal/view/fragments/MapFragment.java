package com.martynaroj.traveljournal.view.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
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
import com.google.android.libraries.places.api.model.OpeningHours;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.DialogNearbyPlacesBinding;
import com.martynaroj.traveljournal.databinding.FragmentMapBinding;
import com.martynaroj.traveljournal.services.models.placesAPI.Place;
import com.martynaroj.traveljournal.services.others.GooglePlaces;
import com.martynaroj.traveljournal.view.adapters.MarkerInfoAdapter;
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.view.others.classes.DialogHandler;
import com.martynaroj.traveljournal.view.others.classes.RequestPermissionsHandler;
import com.martynaroj.traveljournal.viewmodels.AddressViewModel;
import com.martynaroj.traveljournal.viewmodels.MarkerViewModel;
import com.martynaroj.traveljournal.viewmodels.PlaceViewModel;
import com.martynaroj.traveljournal.viewmodels.UserViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MapFragment extends BaseFragment implements View.OnClickListener, OnMapReadyCallback {

    private FragmentMapBinding binding;
    private AddressViewModel addressViewModel;
    private UserViewModel userViewModel;
    private PlaceViewModel placeViewModel;
    private MarkerViewModel markerViewModel;

    private GoogleMap map;
    private FindCurrentPlaceRequest request;
    private PlacesClient placesClient;
    private AutocompleteSupportFragment autocompleteFragment;
    private MarkerInfoAdapter markerInfoAdapter;

    private com.google.android.libraries.places.api.model.Place deviceLocation;
    private Marker temporaryMarker;
    private Marker clickedMarker;

    private com.google.android.libraries.places.api.model.Place searchedPlace;

    private boolean areNearbyPlacesShown;
    private List<Place> nearbyPlaces = new ArrayList<>();

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

        observeUserChanges();

        return view;
    }


    //INIT DATA-------------------------------------------------------------------------------------


    private void initViewModels() {
        if (getActivity() != null) {
            addressViewModel = new ViewModelProvider(getActivity()).get(AddressViewModel.class);
            userViewModel = new ViewModelProvider(getActivity()).get(UserViewModel.class);
            placeViewModel = new ViewModelProvider(getActivity()).get(PlaceViewModel.class);
            markerViewModel = new ViewModelProvider(getActivity()).get(MarkerViewModel.class);
        }
    }


    private void initLoggedUser() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            startProgressBar();
            userViewModel.getUserData(firebaseAuth.getCurrentUser().getUid());
            userViewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
                if (user != null) {
                    if (user.getMarkers() != null && !user.getMarkers().isEmpty())
                        initSavedPlacesMarkers(user.getMarkers());
                    else
                        binding.mapSavedPlacesButton.setEnabled(false);
                } else
                    showSnackBar(getResources().getString(R.string.messages_error_current_user_not_available), Snackbar.LENGTH_LONG);
                stopProgressBar();
            });
        }
    }


    private void initSavedPlacesMarkers(List<String> markers) {
        startProgressBar();
        markerViewModel.getMarkersListData(markers);
        markerViewModel.getMarkersList().observe(getViewLifecycleOwner(), markerList -> {
            if (markerList != null) {
                savedPlacesMarkersOptions = new ArrayList<>();
                savedPlacesMarkers = new ArrayList<>();
                for (com.martynaroj.traveljournal.services.models.Marker marker : markerList) {
                    MarkerOptions options = new MarkerOptions()
                            .icon(BitmapDescriptorFactory.defaultMarker(marker.getColor()))
                            .position(new LatLng(marker.getLatitude(), marker.getLongitude()))
                            .title(marker.getDescription());
                    savedPlacesMarkersOptions.add(options);
                }
            } else
                showSnackBar(getResources().getString(R.string.messages_error_failed_load_markers), Snackbar.LENGTH_LONG);
            stopProgressBar();
        });
    }


    private void initGoogleMap() {
        ((SupportMapFragment) Objects.requireNonNull(getChildFragmentManager()
                .findFragmentById(R.id.map_google_map)))
                .getMapAsync(this);
    }


    private void initGooglePlaces() {
        if (getContext() != null) {
            GooglePlaces.init(getContext());
            placesClient = GooglePlaces.initClient(getContext());
            request = GooglePlaces.initRequest();
            autocompleteFragment = GooglePlaces.initAutoComplete(
                    getContext(),
                    R.id.map_search_view,
                    getChildFragmentManager()
            );
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
            if (user == null)
                back();
        });
    }


    //LISTENERS-------------------------------------------------------------------------------------


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
                public void onPlaceSelected(@NonNull com.google.android.libraries.places.api.model.Place place) {
                    LatLng latLng = place.getLatLng();
                    if (latLng != null) {
                        temporaryMarker = null;
                        clearMap();
                        temporaryMarker = addMarkerOnMap(latLng, true);
                        searchedPlace = place;
                        updateMarkerPlaceData(temporaryMarker, place.getName(), place.getAddress(), place.getOpeningHours(),
                                place.getPhoneNumber(), place.getRating());
                        zoomMap(latLng, 8.0f);
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


    private void setMapListener() {
        map.setOnMapClickListener(latLng -> {
            temporaryMarker = null;
            searchedPlace = null;
            autocompleteFragment.setText("");
            clearMap();
            temporaryMarker = addMarkerOnMap(latLng, true);
            map.animateCamera(CameraUpdateFactory.newLatLng(latLng), 250, null);
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


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.map_arrow_button:
                back();
                break;
            case R.id.map_saved_places_button:
                checkSavedPlaces();
                break;
            case R.id.map_nearby_places_button:
                showNearbyPlacesDialog();
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


    private Marker addMarkerOnMap(LatLng latLng, boolean temporary) {
        MarkerOptions currentMarkerOptions = new MarkerOptions()
                .icon(BitmapDescriptorFactory.defaultMarker())
                .position(latLng);
        if (temporary)
            currentMarkerOptions.alpha(0.3f);
        return map.addMarker(currentMarkerOptions);
    }


    private void addMarkersOnMap(List<Place> places) {
        for (Place place : places) {
            LatLng latLng = new LatLng(place.getGeometry().getLocation().getLat(),
                    place.getGeometry().getLocation().getLng());
            Marker marker = addMarkerOnMap(latLng, false);
            updateMarkerPlaceData(marker, place.getName(), place.getVicinity(), null,
                    null, place.getRating());
            zoomMap(latLng, 15.0f);
        }
    }


    private void updateMarkerPlaceData(Marker marker, String name, String address,
                                       OpeningHours openingHours, String phone, Double rating) {
        marker.setTitle(name);
        StringBuilder snippet = new StringBuilder();
        if (address != null)
            snippet.append(address).append("\n");
        if (openingHours != null)
            for (String day : openingHours.getWeekdayText()) {
                snippet.append("\t\t").append(day).append("\n");
            }
        if (phone != null)
            snippet.append("Phone number: ").append(phone).append("\n");
        if (rating != null)
            snippet.append("Rating: ").append(rating).append("\n");
        marker.setSnippet(snippet.toString());

        if (marker.equals(temporaryMarker)) {
            marker.showInfoWindow();
            clickedMarker = marker;
        }
    }


    private void detectLocation() {
        map.setMyLocationEnabled(true);
        startProgressBar();
        addressViewModel.detectAddress(placesClient, request);
        addressViewModel.getDetectedAddress().observe(getViewLifecycleOwner(), response -> {
            if (response != null) {
                deviceLocation = response.getPlaceLikelihoods().get(0).getPlace();
                if (deviceLocation.getLatLng() != null)
                    zoomMap(deviceLocation.getLatLng(), 10.0f);
                stopProgressBar();
            } else
                showSnackBar(getResources().getString(R.string.messages_error_localize), Snackbar.LENGTH_LONG);
        });
    }


    private void zoomMap(LatLng latLng, float value) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, value));
    }


    private void checkSavedPlaces() {
        if (areSavedPlacesShown) {
            hideSavedPlaces();
            binding.mapSavedPlacesButton.setText(getResources().getString(R.string.map_show_saved_places));
        } else {
            showSavedPlaces();
            map.animateCamera(CameraUpdateFactory.zoomTo(2), 500, null);
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


    private void clearMap() {
        map.clear();
        if (temporaryMarker != null) {
            temporaryMarker = addMarkerOnMap(new LatLng(temporaryMarker.getPosition().latitude,
                    temporaryMarker.getPosition().longitude), true);
            if (searchedPlace != null) {
                updateMarkerPlaceData(temporaryMarker, searchedPlace.getName(), searchedPlace.getAddress(),
                        searchedPlace.getOpeningHours(), searchedPlace.getPhoneNumber(), searchedPlace.getRating());
            }
        }
        if (areSavedPlacesShown) {
            showSavedPlaces();
        }
        if (areNearbyPlacesShown) {
            addMarkersOnMap(nearbyPlaces);
        }
    }


    private void searchNearbyPlaces(String type) {
        if (deviceLocation.getLatLng() != null) {
            placeViewModel.getPlaces(deviceLocation.getLatLng(), type);
            placeViewModel.getPlacesResultData().observe(getViewLifecycleOwner(), placesResult -> {
                if (placesResult != null) {
                    if (placesResult.getPlaces() != null && !placesResult.getPlaces().isEmpty())
                        addMarkersOnMap(placesResult.getPlaces());
                    else
                        showSnackBar(getResources().getString(R.string.messages_no_places_results), Snackbar.LENGTH_LONG);
                    zoomMap(deviceLocation.getLatLng(), 15.0f);
                } else
                    showSnackBar(getResources().getString(R.string.messages_error_localize), Snackbar.LENGTH_LONG);
            });
        } else
            showSnackBar(getResources().getString(R.string.messages_error_localize), Snackbar.LENGTH_LONG);
    }


    //DIALOG----------------------------------------------------------------------------------------


    private void showNearbyPlacesDialog() {
        if (getContext() != null) {
            List<String> types = Arrays.asList(getResources().getStringArray(R.array.places));
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                    android.R.layout.simple_list_item_single_choice, types);

            Dialog dialog = DialogHandler.createDialog(getContext(), true);
            DialogNearbyPlacesBinding binding = DialogNearbyPlacesBinding.inflate(LayoutInflater.from(getContext()));
            dialog.setContentView(binding.getRoot());
            binding.dialogNearbyPlacesList.setAdapter(adapter);
            binding.dialogNearbyPlacesList.setItemChecked(0, true);
            binding.dialogNearbyPlacesCancelButton.setOnClickListener(v -> dialog.dismiss());
            binding.dialogNearbyPlacesClearButton.setOnClickListener(v -> {
                dialog.dismiss();
                areNearbyPlacesShown = false;
                clearMap();
            });
            binding.dialogNearbyPlacesSearchButton.setOnClickListener(v -> {
                binding.dialogNearbyPlacesClearButton.callOnClick();
                String type = adapter.getItem(binding.dialogNearbyPlacesList.getCheckedItemPosition());
                if (type != null) searchNearbyPlaces(getPlaceTypeKey(type));
                areNearbyPlacesShown = true;
            });
            dialog.show();
        }
    }


    //OTHERS----------------------------------------------------------------------------------------


    private String getPlaceTypeKey(String type) {
        return type.toLowerCase().replaceAll("\\s", "_");
    }


    private void back() {
        if (getParentFragmentManager().getBackStackEntryCount() > 0)
            getParentFragmentManager().popBackStack();
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
        if (RequestPermissionsHandler.isOnResultGranted(requestCode, grantResults))
            detectLocation();
        else
            binding.mapNearbyPlacesButton.setEnabled(false);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
