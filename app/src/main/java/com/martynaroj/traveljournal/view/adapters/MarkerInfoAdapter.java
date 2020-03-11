package com.martynaroj.traveljournal.view.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.martynaroj.traveljournal.databinding.WindowMarkerInfoBinding;

public class MarkerInfoAdapter implements GoogleMap.InfoWindowAdapter {

    private Context context;

    public MarkerInfoAdapter(Context context){
        this.context = context;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        WindowMarkerInfoBinding binding = WindowMarkerInfoBinding.inflate(LayoutInflater.from(context));

        binding.markerTitle.setText(marker.getTitle());

        if (marker.getSnippet() != null && !marker.getSnippet().isEmpty())
            binding.markerDescription.setText(marker.getSnippet());
        else binding.markerDescription.setVisibility(View.GONE);

        View view;
        if (marker.getTitle() != null && !marker.getTitle().isEmpty())
            view = binding.getRoot();
        else view = null;

        return view;
    }
}
