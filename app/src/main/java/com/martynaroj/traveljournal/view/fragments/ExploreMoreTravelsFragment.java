package com.martynaroj.traveljournal.view.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.FragmentExploreMoreTravelsBinding;
import com.martynaroj.traveljournal.services.models.Itinerary;
import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.view.adapters.TravelAdapter;
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.view.others.enums.Sort;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;
import com.martynaroj.traveljournal.viewmodels.ItineraryViewModel;
import com.martynaroj.traveljournal.viewmodels.UserViewModel;

import java.util.ArrayList;
import java.util.List;

public class ExploreMoreTravelsFragment extends BaseFragment implements View.OnClickListener{

    private FragmentExploreMoreTravelsBinding binding;
    private UserViewModel userViewModel;
    private ItineraryViewModel itineraryViewModel;

    private User user;
    private List<Itinerary> list;
    private DocumentSnapshot lastDocument;


    public static ExploreMoreTravelsFragment newInstance(User user) {
        ExploreMoreTravelsFragment fragment = new ExploreMoreTravelsFragment();
        Bundle args = new Bundle();
        args.putSerializable(Constants.BUNDLE_USER, user);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user = (User) getArguments().getSerializable(Constants.BUNDLE_USER);
        }
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentExploreMoreTravelsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initViewModels();
        initContentData();
        setListeners();
        observeUserChanges();

        return view;
    }


    //INIT DATA-------------------------------------------------------------------------------------


    private void initViewModels() {
        if (getActivity() != null) {
            userViewModel = new ViewModelProvider(getActivity()).get(UserViewModel.class);
            itineraryViewModel = new ViewModelProvider(getActivity()).get(ItineraryViewModel.class);
        }
    }


    private void initContentData() {
        list = new ArrayList<>();
        initSortingSpinner();
        loadList(false);
    }


    private void initSortingSpinner() {
        List<String> options = new ArrayList<>();
        for (Sort e : Sort.values())
            options.add(e.getValue());
        binding.exploreMoreTravelsSortSpinner.setItems(options);
    }


    private void setIsListEmpty() {
        if (list != null)
            binding.setIsListEmpty(list.isEmpty());
        else
            binding.setIsListEmpty(true);
    }


    private void observeUserChanges() {
        userViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            this.user = user;
            if (user == null) {
                showSnackBar(getResources().getString(R.string.messages_not_logged_user), Snackbar.LENGTH_LONG);
                back();
            }
        });
    }


    //LISTENERS-------------------------------------------------------------------------------------


    private void setListeners() {
        binding.exploreMoreTravelsArrowButton.setOnClickListener(this);
        binding.exploreMoreTravelsSortSpinner.setOnItemSelectedListener((view, position, id, item) ->
                loadList(true)
        );
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.explore_more_travels_arrow_button) {
            back();
        }
    }


    //LIST------------------------------------------------------------------------------------------


    private void loadList(boolean clear) {
        if (clear) list.clear();
        startProgressBar();
        itineraryViewModel.getDocumentsListStartAt(user, lastDocument, 10, getSelectedOrder(), getSelectedDirection());
        itineraryViewModel.getDocumentsData().observe(getViewLifecycleOwner(), documentSnapshots -> {
            if (documentSnapshots != null && !documentSnapshots.isEmpty()) {
                for (DocumentSnapshot documentSnapshot : documentSnapshots)
                    list.add(documentSnapshot.toObject(Itinerary.class));
                lastDocument = documentSnapshots.get(documentSnapshots.size()-1);
            }
            setListAdapter();
            stopProgressBar();
        });
    }


    private void setListAdapter() {
        if (list != null) {
            TravelAdapter adapter = new TravelAdapter(getContext(), list);
            binding.exploreMoreTravelsRecyclerView.setAdapter(adapter);
            adapter.setOnItemClickListener((object, position, view) ->
                    changeFragment(TravelFragment.newInstance((Itinerary)object, user))
            );
        }
        setIsListEmpty();
    }


    private Query.Direction getSelectedDirection() {
        Query.Direction direction = Query.Direction.DESCENDING;
        if (Sort.values()[binding.exploreMoreTravelsSortSpinner.getSelectedIndex()] == Sort.DATE_OLDEST
            || Sort.values()[binding.exploreMoreTravelsSortSpinner.getSelectedIndex()] == Sort.DURATION_SHORTEST)
                direction = Query.Direction.ASCENDING;
        return direction;
    }


    private String getSelectedOrder() {
        String result = "";
        switch (Sort.values()[binding.exploreMoreTravelsSortSpinner.getSelectedIndex()]) {
            case POPULARITY:
                result = Constants.DB_POPULARITY;
                break;
            case DATE_LATEST:
            case DATE_OLDEST:
                result = Constants.DB_CREATED_DATE;
                break;
            case DURATION_LONGEST:
            case DURATION_SHORTEST:
                result = Constants.DB_DAYS_AMOUNT;
                break;
        }
        return result;
    }


    //OTHERS----------------------------------------------------------------------------------------


    private void showSnackBar(String message, int duration) {
        getSnackBarInteractions().showSnackBar(binding.getRoot(), getActivity(), message, duration);
    }


    private void startProgressBar() {
        getProgressBarInteractions().startProgressBar(binding.getRoot(), binding.exploreMoreTravelsProgressbarLayout,
                binding.exploreMoreTravelsProgressbar);
    }


    private void stopProgressBar() {
        getProgressBarInteractions().stopProgressBar(binding.getRoot(), binding.exploreMoreTravelsProgressbarLayout,
                binding.exploreMoreTravelsProgressbar);
    }


    private void changeFragment(BaseFragment next) {
        getNavigationInteractions().changeFragment(getParentFragment(), next, true);
    }


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
