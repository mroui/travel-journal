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

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.snackbar.Snackbar;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.FragmentWeatherBinding;
import com.martynaroj.traveljournal.services.others.GooglePlaces;
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;
import com.martynaroj.traveljournal.viewmodels.AddressViewModel;
import com.martynaroj.traveljournal.viewmodels.UserViewModel;
import com.martynaroj.traveljournal.viewmodels.WeatherViewModel;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class WeatherFragment extends BaseFragment implements View.OnClickListener {

    private FragmentWeatherBinding binding;

    private AddressViewModel addressViewModel;
    private WeatherViewModel weatherViewModel;
    private UserViewModel userViewModel;

    private FindCurrentPlaceRequest request;
    private PlacesClient placesClient;
    private Place deviceLocation;
    private AutocompleteSupportFragment autocompleteFragment;

    public static WeatherFragment newInstance() {
        return new WeatherFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWeatherBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initViewModels();
        initGooglePlaces();
        initLocation();

        setListeners();

        observeUserChanges();

        return view;
    }


    //INIT DATA-------------------------------------------------------------------------------------


    private void initViewModels() {
        if (getActivity() != null) {
            addressViewModel = new ViewModelProvider(getActivity()).get(AddressViewModel.class);
            weatherViewModel = new ViewModelProvider(getActivity()).get(WeatherViewModel.class);
            userViewModel = new ViewModelProvider(getActivity()).get(UserViewModel.class);
        }
    }


    private void initGooglePlaces() {
        if (getContext() != null) {
            GooglePlaces.init(getContext());
            placesClient = GooglePlaces.initClient(getContext());
            request = GooglePlaces.initRequest();
            autocompleteFragment = GooglePlaces.initAutoComplete(
                    getContext(),
                    R.id.weather_search_view,
                    getChildFragmentManager()
            );
        }
    }


    private void initLocation() {
        if (getContext() != null && ContextCompat.checkSelfPermission(getContext(),
                ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            detectLocation();
        } else if (getActivity() != null) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    Constants.RC_ACCESS_FINE_LOCATION);
        }
    }


    private void observeUserChanges() {
        userViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user == null)
                back();
        });
    }


    //LISTENERS-------------------------------------------------------------------------------------


    private void setListeners() {
        binding.weatherArrowButton.setOnClickListener(this);
        binding.weatherSwitchTempUnits.setOnClickListener(this);
        setAutoCompleteListeners();
    }


    private void setAutoCompleteListeners() {
        if (autocompleteFragment != null && autocompleteFragment.getView() != null) {
            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    getCurrentWeather(place.getLatLng());
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
            case R.id.weather_arrow_button:
                if (getParentFragmentManager().getBackStackEntryCount() > 0)
                    getParentFragmentManager().popBackStack();
                break;
            case R.id.weather_switch_temp_units:
                changeTempUnits();
                break;
        }
    }


    //LOCATION & WEATHER----------------------------------------------------------------------------


    private void detectLocation() {
        startProgressBar();
        addressViewModel.detectAddress(placesClient, request);
        addressViewModel.getDetectedAddress().observe(getViewLifecycleOwner(), response -> {
            if (response != null) {
                deviceLocation = response.getPlaceLikelihoods().get(0).getPlace();
                getCurrentWeather(deviceLocation.getLatLng());
            } else {
                showSnackBar(getResources().getString(R.string.messages_error_localize), Snackbar.LENGTH_LONG);
            }
            stopProgressBar();
        });
    }


    private void getCurrentWeather(LatLng latLng) {
        startProgressBar();
        weatherViewModel.getWeather(latLng);
        weatherViewModel.getWeatherResultData().observe(getViewLifecycleOwner(), weatherResult -> {
            if (weatherResult != null) {
                binding.setWeatherResult(weatherResult);
            } else {
                showSnackBar(getResources().getString(R.string.messages_error_localize), Snackbar.LENGTH_LONG);
                getCurrentWeather(Constants.LAT_LNG_LONDON);
            }
            stopProgressBar();
        });
        weatherViewModel.getWeatherForecast(latLng);
        weatherViewModel.getWeatherForecastResultData().observe(getViewLifecycleOwner(), weatherForecastResult -> {
            if (weatherForecastResult != null) {
                binding.setForecastResult(weatherForecastResult);
            }
            stopProgressBar();
        });
    }


    private void changeTempUnits() {
        binding.setTempUnits(binding.weatherSwitchTempUnits.isChecked());
    }


    //OTHERS----------------------------------------------------------------------------------------


    private void startProgressBar() {
        getProgressBarInteractions().startProgressBar(binding.getRoot(),
                binding.weatherProgressbarLayout, binding.weatherProgressbar);
    }


    private void stopProgressBar() {
        getProgressBarInteractions().stopProgressBar(binding.getRoot(),
                binding.weatherProgressbarLayout, binding.weatherProgressbar);
    }


    private void showSnackBar(String message, int duration) {
        getSnackBarInteractions().showSnackBar(binding.getRoot(), getActivity(), message, duration);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == Constants.RC_ACCESS_FINE_LOCATION && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            detectLocation();
        else
            getCurrentWeather(Constants.LAT_LNG_LONDON);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
