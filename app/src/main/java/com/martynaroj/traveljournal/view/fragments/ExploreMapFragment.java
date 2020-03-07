package com.martynaroj.traveljournal.view.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.FragmentExploreMapBinding;
import com.martynaroj.traveljournal.services.models.Address;
import com.martynaroj.traveljournal.services.models.Marker;
import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.view.interfaces.IOnBackPressed;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;
import com.martynaroj.traveljournal.viewmodels.AddressViewModel;
import com.martynaroj.traveljournal.viewmodels.UserViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

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
        disableButtons();

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
                    addMarkers();
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
        addMarkers();
        showTutorialSnackbar();
    }


    private void addMarkers() {
        if(user != null && user.getMarkers() != null && !user.getMarkers().isEmpty() && map != null) {
            for (Marker marker : user.getMarkers()) {
                MarkerOptions options = new MarkerOptions()
                        .icon(BitmapDescriptorFactory.defaultMarker(new Random().nextInt(360)))
                        .position(new LatLng(marker.getLatitude(), marker.getLongitude()))
                        .snippet(marker.getDescription())
                        .title(marker.getDescription());
                map.addMarker(options);
            }
        }
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
            addMarkers();
            currentPlace = new LatLng(p.latitude, p.longitude);
            currentMarker = new MarkerOptions().position(currentPlace);
            map.addMarker(currentMarker);
            binding.exploreMapAddPlaceButton.setEnabled(true);
        });
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.explore_map_arrow_button:
                if (getParentFragmentManager().getBackStackEntryCount() > 0)
                    getParentFragmentManager().popBackStack();
            case R.id.explore_map_add_place_button:
                showAddPlaceDialog();
            case R.id.explore_map_remove_place_button:
            default:
                dismissTutorialSnackbar();
        }
    }


    private void showAddPlaceDialog() {
        if (getContext() != null) {
            if (user != null) {
                Dialog dialog = new Dialog(getContext());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(true);
                dialog.setContentView(R.layout.dialog_add_place);
                dialog.findViewById(R.id.dialog_add_place_cancel_button).setOnClickListener(v -> dialog.dismiss());
                dialog.findViewById(R.id.dialog_add_place_add_button).setOnClickListener(v -> {
                    String description = Objects.requireNonNull(((TextInputEditText) dialog
                            .findViewById(R.id.dialog_add_place_input)).getText()).toString();
                    dialog.dismiss();
                    addPlace(description);
                });
                dialog.show();
            } else {
                showSnackBar(getResources().getString(R.string.messages_not_logged_user), Snackbar.LENGTH_LONG);
            }
        }
    }


    private void addPlace(String description) {
        Map<String, Object> changes = new HashMap<>();
        List<Marker> newMarkersList = user.getMarkers() != null
                ? new ArrayList<>(user.getMarkers()) : new ArrayList<>();
        newMarkersList.add(new Marker(description, currentPlace.latitude, currentPlace.longitude));
        changes.put(Constants.DB_MARKERS, newMarkersList);
        updateUser(changes);
    }


    private void updateUser(Map<String, Object> changes) {
        startProgressBar();
        userViewModel.updateUser(user, changes);
        userViewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                this.user = user;
                showSnackBar(getResources().getString(R.string.messages_changes_saved), Snackbar.LENGTH_SHORT);
            } else {
                showSnackBar(getResources().getString(R.string.messages_error_failed_update), Snackbar.LENGTH_LONG);
            }
            stopProgressBar();
        });
    }


    private void dismissTutorialSnackbar() {
        if (tutorialSnackbar.isShown())
            tutorialSnackbar.dismiss();
    }


    @Override
    public boolean onBackPressed() {
        if (getParentFragmentManager().getBackStackEntryCount() > 0)
            getParentFragmentManager().popBackStack();
        dismissTutorialSnackbar();
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
        binding.exploreMapAddPlaceButton.setEnabled(false);
        binding.exploreMapRemovePlaceButton.setEnabled(false);
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
