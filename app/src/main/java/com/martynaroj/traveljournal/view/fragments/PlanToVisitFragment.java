package com.martynaroj.traveljournal.view.fragments;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
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

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.FragmentPlanToVisitBinding;
import com.martynaroj.traveljournal.services.models.Address;
import com.martynaroj.traveljournal.services.models.Marker;
import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.services.others.GooglePlaces;
import com.martynaroj.traveljournal.view.adapters.MarkerInfoAdapter;
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.view.interfaces.IOnBackPressed;
import com.martynaroj.traveljournal.view.others.classes.SharedPreferencesUtils;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;
import com.martynaroj.traveljournal.viewmodels.AddressViewModel;
import com.martynaroj.traveljournal.viewmodels.MarkerViewModel;
import com.martynaroj.traveljournal.viewmodels.UserViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import petrov.kristiyan.colorpicker.ColorPicker;

public class PlanToVisitFragment extends BaseFragment implements View.OnClickListener,
        OnMapReadyCallback, IOnBackPressed {

    private FragmentPlanToVisitBinding binding;

    private UserViewModel userViewModel;
    private AddressViewModel addressViewModel;
    private MarkerViewModel markerViewModel;

    private User user;
    private List<Marker> markers;

    private LatLng currentPlace;
    private com.google.android.gms.maps.model.Marker currentMarker;
    private com.google.android.gms.maps.model.Marker clickedMarker;
    private GoogleMap map;

    private Snackbar tutorialSnackbar;
    private AutocompleteSupportFragment autocompleteFragment;


    public static PlanToVisitFragment newInstance() {
        return new PlanToVisitFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPlanToVisitBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initViewModels();
        initLoggedUser();
        initGoogleMap();
        initGooglePlaces();

        setListeners();
        disableButtons();

        checkTutorialSnackbar();

        return view;
    }


    //INIT DATA-------------------------------------------------------------------------------------


    private void initViewModels() {
        if (getActivity() != null) {
            userViewModel = new ViewModelProvider(getActivity()).get(UserViewModel.class);
            addressViewModel = new ViewModelProvider(getActivity()).get(AddressViewModel.class);
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
                    this.user = user;
                    initLocation();
                    initMarkers();
                } else
                    showSnackBar(getResources().getString(R.string.messages_error_current_user_not_available), Snackbar.LENGTH_LONG);
                stopProgressBar();
            });
        }
    }


    private void initMarkers() {
        markers = new ArrayList<>();
        if (user.getMarkers() != null && !user.getMarkers().isEmpty()) {
            startProgressBar();
            markerViewModel.getMarkersListData(user.getMarkers());
            markerViewModel.getMarkersList().observe(getViewLifecycleOwner(), markers -> {
                if (markers != null) {
                    this.markers = markers;
                    addMarkersOnMap();
                } else
                    showSnackBar(getResources().getString(R.string.messages_error_failed_load_markers), Snackbar.LENGTH_LONG);
                stopProgressBar();
            });
        }
    }


    private void initGoogleMap() {
        ((SupportMapFragment) Objects.requireNonNull(getChildFragmentManager()
                .findFragmentById(R.id.explore_map_google_map)))
                .getMapAsync(this);
    }


    private void initLocation() {
        if (user.getLocation() != null && !user.getLocation().isEmpty()) {
            startProgressBar();
            addressViewModel.getAddress(user.getLocation());
            addressViewModel.getAddressData().observe(getViewLifecycleOwner(), address -> {
                if (address != null)
                    setAddressOnMap(address);
                stopProgressBar();
            });
        }
    }


    private void initGooglePlaces() {
        if (getContext() != null) {
            GooglePlaces.init(getContext());
            autocompleteFragment = GooglePlaces.initAutoComplete(
                    getContext(),
                    R.id.explore_map_search_view,
                    getChildFragmentManager()
            );
        }
    }


    //LISTENERS-------------------------------------------------------------------------------------


    private void setListeners() {
        binding.exploreMapArrowButton.setOnClickListener(this);
        binding.exploreMapAddPlaceButton.setOnClickListener(this);
        binding.exploreMapRemovePlaceButton.setOnClickListener(this);
        if (autocompleteFragment != null && autocompleteFragment.getView() != null) {
            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    LatLng latLng = place.getLatLng();
                    if (latLng != null) {
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 8.0f));
                        if (!markers.contains(new Marker(latLng.latitude, latLng.longitude)))
                            addTemporaryMarkerOnMap(latLng);
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
        dismissTutorialSnackbar();
        switch (view.getId()) {
            case R.id.explore_map_arrow_button:
                back();
                break;
            case R.id.explore_map_add_place_button:
                showColorPickerDialog();
                break;
            case R.id.explore_map_remove_place_button:
                showRemoveMarkerDialog();
                break;
        }
    }


    private void setMapListener() {
        map.setOnMapClickListener(latLng -> {
            addTemporaryMarkerOnMap(latLng);
            autocompleteFragment.setText("");
        });
        map.setOnMarkerClickListener(marker -> {
            if (!marker.equals(currentMarker)) {
                autocompleteFragment.setText("");
                if (currentMarker != null)
                    currentMarker.remove();
                if (clickedMarker != null && clickedMarker.equals(marker)) {
                    marker.hideInfoWindow();
                    clickedMarker = null;
                    binding.exploreMapAddPlaceButton.setEnabled(false);
                    binding.exploreMapRemovePlaceButton.setEnabled(false);
                    return true;
                } else {
                    marker.showInfoWindow();
                    clickedMarker = marker;
                    binding.exploreMapAddPlaceButton.setEnabled(false);
                    binding.exploreMapRemovePlaceButton.setEnabled(true);
                }
            }
            return false;
        });
    }


    @Override
    public boolean onBackPressed() {
        back();
        dismissTutorialSnackbar();
        return true;
    }


    //MAP-------------------------------------------------------------------------------------------


    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setZoomControlsEnabled(true);
        map.setInfoWindowAdapter(new MarkerInfoAdapter(getContext()));
        setMapListener();
    }


    private void setAddressOnMap(Address address) {
        if (map != null) {
            LatLng place = new LatLng(address.getLatitude(), address.getLongitude());
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(place, 8.0f));
        }
    }


    private void addMarkersOnMap() {
        if (markers != null && !markers.isEmpty() && map != null) {
            for (Marker marker : markers) {
                MarkerOptions options = new MarkerOptions()
                        .icon(BitmapDescriptorFactory.defaultMarker(marker.getColor()))
                        .position(new LatLng(marker.getLatitude(), marker.getLongitude()))
                        .title(marker.getDescription());
                map.addMarker(options);
            }
        }
    }


    private void addTemporaryMarkerOnMap(LatLng place) {
        refreshMap();
        currentPlace = new LatLng(place.latitude, place.longitude);
        MarkerOptions currentMarkerOptions = new MarkerOptions()
                .icon(BitmapDescriptorFactory.defaultMarker())
                .alpha(0.3f)
                .position(currentPlace);
        currentMarker = map.addMarker(currentMarkerOptions);
        map.animateCamera(CameraUpdateFactory.newLatLng(currentMarkerOptions.getPosition()), 250, null);
        binding.exploreMapAddPlaceButton.setEnabled(true);
        binding.exploreMapRemovePlaceButton.setEnabled(false);
    }


    private void refreshMap() {
        currentMarker = null;
        map.clear();
        addMarkersOnMap();
    }


    //DIALOGS---------------------------------------------------------------------------------------


    private void showAddMarkerDialog(int color) {
        if (getContext() != null) {
            if (user != null) {
                Dialog dialog = new Dialog(getContext());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(true);
                dialog.setContentView(R.layout.dialog_add_marker);
                dialog.findViewById(R.id.dialog_add_marker_color).setBackgroundColor(color);
                dialog.findViewById(R.id.dialog_add_marker_cancel_button).setOnClickListener(v -> dialog.dismiss());
                dialog.findViewById(R.id.dialog_add_marker_add_button).setOnClickListener(v -> {
                    String description = Objects.requireNonNull(((TextInputEditText) dialog
                            .findViewById(R.id.dialog_add_marker_input)).getText()).toString();
                    dialog.dismiss();
                    addMarker(description, convertStringToHsv(Integer.toHexString(color)));
                });
                dialog.show();
            } else
                showSnackBar(getResources().getString(R.string.messages_not_logged_user), Snackbar.LENGTH_LONG);
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
            buttonPositive.setRippleColor(ColorStateList
                    .valueOf(ContextCompat.getColor(getContext(), R.color.yellow_bg_lighter)));
            TextViewCompat.setTextAppearance(buttonPositive, R.style.ExploreMapButton);
            buttonPositive.setOnClickListener(v -> {
                dialog.dismiss();
                if (clickedMarker != null)
                    removeMarker();
            });
            buttonNegative.setText(getResources().getString(R.string.dialog_button_cancel));
            buttonNegative.setRippleColor(ColorStateList
                    .valueOf(ContextCompat.getColor(getContext(), R.color.yellow_bg_lighter)));
            TextViewCompat.setTextAppearance(buttonNegative, R.style.ExploreMapButton);
            buttonNegative.setOnClickListener(v -> dialog.dismiss());

            dialog.show();
        }
    }


    private void showColorPickerDialog() {
        if (getActivity() != null) {
            new ColorPicker(getActivity())
                    .setColors(new ArrayList<>(Arrays.asList(Constants.MARKER_COLORS)))
                    .setTitle(getResources().getString(R.string.dialog_choose_marker_color))
                    .setColumns(5)
                    .setOnFastChooseColorListener(new ColorPicker.OnFastChooseColorListener() {
                        @Override
                        public void setOnFastChooseColorListener(int position, int color) {
                            showAddMarkerDialog(color);
                        }

                        @Override
                        public void onCancel() {
                        }
                    })
                    .show();
        }
    }


    //DATABASE--------------------------------------------------------------------------------------


    private void addMarker(String description, float color) {
        Marker marker = new Marker(description, color, currentPlace.latitude, currentPlace.longitude);
        startProgressBar();
        markerViewModel.addMarker(marker);
        markerViewModel.getMarkerResponse().observe(getViewLifecycleOwner(), response -> {
            if (!response.contains(getResources().getString(R.string.messages_error))) {
                marker.setId(response);
                addMarkerToUser(marker);
            } else
                showSnackBar(response, Snackbar.LENGTH_LONG);
            stopProgressBar();
        });
    }


    private void addMarkerToUser(Marker marker) {
        List<String> newMarkersList = user.getMarkers() != null
                ? new ArrayList<>(user.getMarkers()) : new ArrayList<>();
        newMarkersList.add(marker.getId());
        updateUser(new HashMap<String, Object>() {{
            put(Constants.DB_MARKERS, newMarkersList);
        }}, marker, true);
    }


    private void removeMarker() {
        List<String> filtered = new ArrayList<>();
        Marker markerToRemove = new Marker(
                clickedMarker.getPosition().latitude,
                clickedMarker.getPosition().longitude);
        for (Marker marker : markers)
            if (!marker.equals(markerToRemove))
                filtered.add(marker.getId());
            else
                markerToRemove = marker;
        markerViewModel.removeMarker(markerToRemove.getId());
        updateUser(new HashMap<String, Object>() {{
            put(Constants.DB_MARKERS, filtered);
        }}, markerToRemove, false);
    }


    private void updateUser(Map<String, Object> changes, Marker marker, boolean adding) {
        startProgressBar();
        userViewModel.updateUser(user, changes);
        userViewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                this.user = user;
                String message;
                if (adding) {
                    message = getResources().getString(R.string.messages_add_marker_success);
                    markers.add(marker);
                } else {
                    message = getResources().getString(R.string.messages_remove_marker_success);
                    markers.remove(marker);
                }
                showSnackBar(message, Snackbar.LENGTH_SHORT);
                refreshMap();
            } else
                showSnackBar(getResources().getString(R.string.messages_error_failed_update), Snackbar.LENGTH_LONG);
            stopProgressBar();
        });
    }


    //OTHERS----------------------------------------------------------------------------------------


    private float convertStringToHsv(String color) {
        float[] hsv = new float[3];
        Color.colorToHSV(Color.parseColor("#" + color.substring(2)), hsv);
        return hsv[0];
    }


    private void disableButtons() {
        binding.exploreMapAddPlaceButton.setEnabled(false);
        binding.exploreMapRemovePlaceButton.setEnabled(false);
    }


    private void checkTutorialSnackbar() {
        tutorialSnackbar = Snackbar.make(binding.getRoot(),
                getResources().getString(R.string.messages_explore_map_tutorial),
                Snackbar.LENGTH_INDEFINITE);
        tutorialSnackbar.setAction(getResources().getString(R.string.dialog_button_ok),
                view -> {
                    tutorialSnackbar.dismiss();
                    saveToPreferences();
                });
        tutorialSnackbar.show();
    }


    private void dismissTutorialSnackbar() {
        if (tutorialSnackbar != null && tutorialSnackbar.isShown())
            tutorialSnackbar.dismiss();
    }


    private void saveToPreferences() {
        if (getContext() != null) {
            SharedPreferencesUtils.setBoolean(getContext(), Constants.PLAN_TO_VISIT_TUTORIAL, true);
        }
    }


    private void back() {
        if (getParentFragmentManager().getBackStackEntryCount() > 0)
            getParentFragmentManager().popBackStack();
    }


    private void startProgressBar() {
        getProgressBarInteractions().startProgressBar(binding.getRoot(), binding.exploreMapProgressbarLayout,
                binding.exploreMapProgressbar);
    }


    private void stopProgressBar() {
        getProgressBarInteractions().stopProgressBar(binding.getRoot(), binding.exploreMapProgressbarLayout,
                binding.exploreMapProgressbar);
        disableButtons();
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
