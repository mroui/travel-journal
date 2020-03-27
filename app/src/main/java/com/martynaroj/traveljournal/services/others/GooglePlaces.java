package com.martynaroj.traveljournal.services.others;

import android.content.Context;
import android.view.View;
import android.widget.EditText;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentManager;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.martynaroj.traveljournal.R;

import java.util.Arrays;

public class GooglePlaces {


    public static void init(Context context) {
        Places.initialize(context, context.getString(R.string.google_api_key));
    }


    public static PlacesClient initClient(Context context) {
        return Places.createClient(context);
    }


    public static FindCurrentPlaceRequest initRequest() {
        return FindCurrentPlaceRequest.newInstance(Arrays.asList(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG)
        );
    }


    public static AutocompleteSupportFragment initAutoComplete(Context context,
                                                               int mapViewId,
                                                               FragmentManager fragmentManager) {
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment) fragmentManager
                .findFragmentById(mapViewId);
        if (autocompleteFragment != null && autocompleteFragment.getView() != null) {
            ((EditText) autocompleteFragment.getView().findViewById(R.id.places_autocomplete_search_input))
                    .setTextSize(14.0f);
            ((EditText) autocompleteFragment.getView().findViewById(R.id.places_autocomplete_search_input))
                    .setTypeface(ResourcesCompat.getFont(context, R.font.raleway_medium));
            autocompleteFragment.getView().findViewById(R.id.places_autocomplete_search_button)
                    .setVisibility(View.GONE);
            autocompleteFragment.setPlaceFields(Arrays.asList(
                    Place.Field.ID,
                    Place.Field.NAME,
                    Place.Field.ADDRESS,
                    Place.Field.LAT_LNG,
                    Place.Field.OPENING_HOURS,
                    Place.Field.PHONE_NUMBER,
                    Place.Field.RATING));
        }
        return autocompleteFragment;
    }


}
