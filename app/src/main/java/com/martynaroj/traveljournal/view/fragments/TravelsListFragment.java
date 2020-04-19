package com.martynaroj.traveljournal.view.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.FragmentTravelsListBinding;
import com.martynaroj.traveljournal.view.base.BaseFragment;

public class TravelsListFragment extends BaseFragment implements View.OnClickListener {

    private FragmentTravelsListBinding binding;
    private UserViewModel userViewModel;
    private ItineraryViewModel itineraryViewModel;
    private TravelAdapter adapter;

    private User user, loggedUser;
    private List<Itinerary> itineraries, savedItineraries;


    public static TravelsListFragment newInstance(User loggedUser, User user) {
        TravelsListFragment fragment = new TravelsListFragment();
        Bundle args = new Bundle();
        args.putSerializable(Constants.BUNDLE_USER, user);
        args.putSerializable(Constants.BUNDLE_LOGGED_USER, loggedUser);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user = (User) getArguments().getSerializable(Constants.BUNDLE_USER);
            loggedUser = (User) getArguments().getSerializable(Constants.BUNDLE_LOGGED_USER);
        }
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTravelsListBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        setListeners();

        return view;
    }


    //INIT DATA-------------------------------------------------------------------------------------


    //


    //LISTENERS-------------------------------------------------------------------------------------


    private void setListeners() {
        binding.travelsListArrowButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.travels_list_arrow_button:
                back();
        }
    }


    //OTHERS----------------------------------------------------------------------------------------


    private void back() {
        if (getParentFragmentManager().getBackStackEntryCount() > 0)
            getParentFragmentManager().popBackStack();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
