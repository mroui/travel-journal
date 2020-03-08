package com.martynaroj.traveljournal.view.fragments;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.FragmentExploreMapBinding;
import com.martynaroj.traveljournal.services.models.Address;
import com.martynaroj.traveljournal.services.models.Marker;
import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.view.adapters.MarkerInfoAdapter;
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

public class ExploreMapFragment extends BaseFragment implements View.OnClickListener, OnMapReadyCallback, IOnBackPressed {

    private FragmentExploreMapBinding binding;
    private UserViewModel userViewModel;
    private AddressViewModel addressViewModel;
    private User user;
    private LatLng currentPlace;
    private MarkerOptions currentMarkerOptions;
    private com.google.android.gms.maps.model.Marker currentMarker;
    private com.google.android.gms.maps.model.Marker clickedMarker;
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


    //INIT DATA-------------------------------------------------------------------------------------


    private void initViewModels() {
        if (getActivity() != null) {
            userViewModel = new ViewModelProvider(getActivity()).get(UserViewModel.class);
            addressViewModel = new ViewModelProvider(getActivity()).get(AddressViewModel.class);
        }
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
                    addMarkersOnMap();
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


    //LISTENERS-------------------------------------------------------------------------------------


    private void setListeners() {
        binding.exploreMapArrowButton.setOnClickListener(this);
        binding.exploreMapAddPlaceButton.setOnClickListener(this);
        binding.exploreMapRemovePlaceButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        dismissTutorialSnackbar();
        switch (view.getId()) {
            case R.id.explore_map_arrow_button:
                if (getParentFragmentManager().getBackStackEntryCount() > 0)
                    getParentFragmentManager().popBackStack();
                break;
            case R.id.explore_map_add_place_button:
                showAddMarkerDialog();
                break;
            case R.id.explore_map_remove_place_button:
                showRemoveMarkerDialog();
                break;
        }
    }


    private void setMapListener() {
        map.setOnMapClickListener(p -> {
            map.clear();
            addMarkersOnMap();
            currentPlace = new LatLng(p.latitude, p.longitude);
            currentMarkerOptions = new MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                    .position(currentPlace);
            currentMarker = map.addMarker(currentMarkerOptions);
            binding.exploreMapAddPlaceButton.setEnabled(true);
            binding.exploreMapRemovePlaceButton.setEnabled(false);
        });
        map.setOnMarkerClickListener(marker -> {
            if (!marker.equals(currentMarker)) {
                if (currentMarker != null)
                    currentMarker.remove();
                if(clickedMarker != null && clickedMarker.equals(marker)) {
                    marker.hideInfoWindow();
                    clickedMarker = null;
                    binding.exploreMapAddPlaceButton.setEnabled(false);
                    binding.exploreMapRemovePlaceButton.setEnabled(false);
                } else {
                    marker.showInfoWindow();
                    clickedMarker = marker;
                    binding.exploreMapAddPlaceButton.setEnabled(false);
                    binding.exploreMapRemovePlaceButton.setEnabled(true);
                }
                return true;
            } else return false;
        });
    }


    //MAP-------------------------------------------------------------------------------------------


    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setInfoWindowAdapter(new MarkerInfoAdapter(getContext()));
        setMapListener();
        showTutorialSnackbar();
    }


    private void setAddressOnMap(Address address) {
        if (map != null) {
            LatLng place = new LatLng(address.getLatitude(), address.getLongitude());
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(place, 8.0f));
        }
    }


    private void addMarkersOnMap() {
        if(user != null && user.getMarkers() != null && !user.getMarkers().isEmpty() && map != null) {
            for (Marker marker : user.getMarkers()) {
                MarkerOptions options = new MarkerOptions()
                        .position(new LatLng(marker.getLatitude(), marker.getLongitude()))
                        .title(marker.getDescription());
                map.addMarker(options);
            }
        }
    }


    private void refreshMap() {
        currentMarker = null;
        map.clear();
        addMarkersOnMap();
    }


    //DIALOGS---------------------------------------------------------------------------------------


    private void showAddMarkerDialog() {
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
                    addMarker(description);
                });
                dialog.show();
            } else {
                showSnackBar(getResources().getString(R.string.messages_not_logged_user), Snackbar.LENGTH_LONG);
            }
        }
    }


    private void showRemoveMarkerDialog() {
        if (getContext() != null) {
            Dialog dialog = new Dialog(getContext());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(true);
            dialog.setContentView(R.layout.dialog_custom);

            TextView title = dialog.findViewById(R.id.dialog_custom_title);
            TextView message = dialog.findViewById(R.id.dialog_custom_desc);
            MaterialButton buttonPositive = dialog.findViewById(R.id.dialog_custom_buttom_positive);
            MaterialButton buttonNegative = dialog.findViewById(R.id.dialog_custom_button_negative);

            title.setText(getResources().getString(R.string.dialog_remove_marker_title));
            message.setText(getResources().getString(R.string.dialog_remove_marker_desc));
            buttonPositive.setText(getResources().getString(R.string.dialog_button_remove));
            buttonPositive.setRippleColor(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.yellow_bg_lighter)));
            TextViewCompat.setTextAppearance(buttonPositive, R.style.ExploreMapButton);
            buttonPositive.setOnClickListener(v -> {
                dialog.dismiss();
                if (clickedMarker != null)
                    removeMarker();
            });
            buttonNegative.setText(getResources().getString(R.string.dialog_button_cancel));
            buttonNegative.setRippleColor(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.yellow_bg_lighter)));
            TextViewCompat.setTextAppearance(buttonNegative, R.style.ExploreMapButton);
            buttonNegative.setOnClickListener(v -> dialog.dismiss());

            dialog.show();
        }
    }


    //DATABASE--------------------------------------------------------------------------------------


    private void addMarker(String description) {
        List<Marker> newMarkersList = user.getMarkers() != null
                ? new ArrayList<>(user.getMarkers()) : new ArrayList<>();
        newMarkersList.add(new Marker(description, currentPlace.latitude, currentPlace.longitude));
        updateUser(new HashMap<String, Object>(){{put(Constants.DB_MARKERS, newMarkersList);}}, true);
    }


    private void removeMarker() {
        List<Marker> filtered = new ArrayList<>();
        for (Marker obj : user.getMarkers())
            if (!obj.equals(new Marker(
                    clickedMarker.getTitle(),
                    clickedMarker.getPosition().latitude,
                    clickedMarker.getPosition().longitude)))
                filtered.add(obj);
        updateUser(new HashMap<String, Object>(){{put(Constants.DB_MARKERS, filtered);}}, false);
    }


    private void updateUser(Map<String, Object> changes, boolean adding) {
        startProgressBar();
        userViewModel.updateUser(user, changes);
        userViewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                this.user = user;
                String message = adding
                        ? getResources().getString(R.string.messages_add_marker_success)
                        : getResources().getString(R.string.messages_remove_marker_success);
                showSnackBar(message, Snackbar.LENGTH_SHORT);
                refreshMap();
            } else {
                showSnackBar(getResources().getString(R.string.messages_error_failed_update), Snackbar.LENGTH_LONG);
            }
            stopProgressBar();
        });
    }


    //OTHERS----------------------------------------------------------------------------------------


    private void disableButtons() {
        binding.exploreMapAddPlaceButton.setEnabled(false);
        binding.exploreMapRemovePlaceButton.setEnabled(false);
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


    private void showTutorialSnackbar() {
        tutorialSnackbar = Snackbar.make(binding.getRoot(),
                getResources().getString(R.string.messages_explore_map_tutorial),
                Snackbar.LENGTH_INDEFINITE);
        tutorialSnackbar.setAction(getResources().getString(R.string.dialog_button_ok),
                view -> tutorialSnackbar.dismiss());
        tutorialSnackbar.show();
    }


    private void dismissTutorialSnackbar() {
        if (tutorialSnackbar.isShown())
            tutorialSnackbar.dismiss();
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
