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

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.snackbar.Snackbar;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.FragmentWeatherBinding;
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;
import com.martynaroj.traveljournal.viewmodels.AddressViewModel;
import com.martynaroj.traveljournal.viewmodels.WeatherViewModel;

import java.util.Arrays;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class WeatherFragment extends BaseFragment implements View.OnClickListener {

    private FragmentWeatherBinding binding;

    private AddressViewModel addressViewModel;
    private WeatherViewModel weatherViewModel;

    private FindCurrentPlaceRequest request;
    private PlacesClient placesClient;
    private Place deviceLocation;

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

        return view;
    }


    //INIT DATA-------------------------------------------------------------------------------------


    private void initViewModels() {
        if (getActivity() != null) {
            addressViewModel = new ViewModelProvider(getActivity()).get(AddressViewModel.class);
            weatherViewModel = new ViewModelProvider(getActivity()).get(WeatherViewModel.class);
        }
    }


    private void initGooglePlaces() {
        if (getContext() != null) {
            Places.initialize(getContext(), getString(R.string.google_api_key));
            placesClient = Places.createClient(getContext());
            request = FindCurrentPlaceRequest.newInstance(Arrays.asList(Place.Field.ID,
                    Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG));
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


    //LISTENERS-------------------------------------------------------------------------------------


    private void setListeners() {
        binding.weatherArrowButton.setOnClickListener(this);
        binding.weatherSwitchTempUnits.setOnClickListener(this);
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


    private void changeTempUnits() {
        binding.setTempUnits(binding.weatherSwitchTempUnits.isChecked());
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
                stopProgressBar();
            }
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
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            detectLocation();
        } else {
            getCurrentWeather(Constants.LAT_LNG_LONDON);
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
