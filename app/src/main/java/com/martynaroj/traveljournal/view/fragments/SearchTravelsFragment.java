package com.martynaroj.traveljournal.view.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.FragmentSearchTravelsBinding;
import com.martynaroj.traveljournal.services.models.Itinerary;
import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.view.adapters.TravelAdapter;
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.view.others.classes.SearchViewListener;
import com.martynaroj.traveljournal.view.others.enums.Criterion;
import com.martynaroj.traveljournal.view.others.enums.Sort;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;
import com.martynaroj.traveljournal.viewmodels.ItineraryViewModel;
import com.martynaroj.traveljournal.viewmodels.UserViewModel;

import java.util.ArrayList;
import java.util.List;

public class SearchTravelsFragment extends BaseFragment implements View.OnClickListener {

    private FragmentSearchTravelsBinding binding;
    private UserViewModel userViewModel;
    private ItineraryViewModel itineraryViewModel;

    private User user;
    private List<Itinerary> list;

    private TravelAdapter adapter;
    private LinearLayoutManager layoutManager;
    private DocumentSnapshot lastDocument;
    private boolean isScrolling;

    private String queryOrderBy;
    private Query.Direction queryDirection;
    private boolean noChangeOrderOption;

    private boolean isSearching;
    private boolean noChangeSearchResult;


    public static SearchTravelsFragment newInstance(User user) {
        SearchTravelsFragment fragment = new SearchTravelsFragment();
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
        binding = FragmentSearchTravelsBinding.inflate(inflater, container, false);
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
        isSearching = false;
        initSortingSpinner();
        reloadList();
    }


    private void initListAdapter() {
        isScrolling = false;
        layoutManager = new LinearLayoutManager(getContext());
        list = new ArrayList<>();
        lastDocument = null;
        adapter = new TravelAdapter(getContext(), list);
        binding.searchTravelsRecyclerView.setAdapter(adapter);
        binding.searchTravelsRecyclerView.setLayoutManager(layoutManager);
        adapter.setOnItemClickListener((object, position, view) -> {
            noChangeOrderOption = true;
            noChangeSearchResult = true;
            changeFragment(TravelFragment.newInstance((Itinerary) object, user));
        });
    }


    private void initSortingSpinner() {
        List<String> options = new ArrayList<>();
        for (Sort e : Sort.values())
            options.add(e.getValue());
        binding.searchTravelsSortSpinner.setItems(options);
    }


    private void setIsListEmpty() {
        if (list != null)
            binding.setIsListEmpty(list.isEmpty());
        else
            binding.setIsListEmpty(true);
    }


    private void observeUserChanges() {
        userViewModel.getUser().observe(getViewLifecycleOwner(), user -> this.user = user);
    }


    //LISTENERS-------------------------------------------------------------------------------------


    private void setListeners() {
        binding.searchTravelsArrowButton.setOnClickListener(this);
        binding.searchTravelsSortSpinner.setOnItemSelectedListener((view, position, id, item) -> {
            noChangeOrderOption = false;
            reloadList();
        });
        setOnScrollListener();
        setOnSearchListeners();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.search_travels_arrow_button:
                back();
                break;
        }
    }


    private void setOnScrollListener() {
        binding.searchTravelsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
                    isScrolling = true;
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemsCount = layoutManager.getChildCount();
                int totalItems = list.size();
                int scrollOutItems = layoutManager.findFirstVisibleItemPosition();

                if (isScrolling && (visibleItemsCount + scrollOutItems == totalItems)) {
                    isScrolling = false;
                    loadList(false);
                }
            }
        });
    }


    private void setOnSearchListeners() {
        binding.searchTravelsSearchView.setOnQueryTextListener(new SearchViewListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                noChangeSearchResult = false;
                isSearching = true;
                resetSorting();
                reloadList();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if(noChangeSearchResult) {
                    noChangeSearchResult = false;
                    return false;
                }
                return true;
            }
        });
        binding.searchTravelsSearchView.findViewById(R.id.search_close_btn).setOnClickListener(v -> {
            binding.searchTravelsSearchView.setQuery("", true);
            isSearching = false;
            resetSorting();
            reloadList();
        });
    }




    //LIST------------------------------------------------------------------------------------------


    private void loadList(boolean newAdapter) {
        if (newAdapter) {
            initListAdapter();
            startProgressBar();
        }

        if (isSearching || !binding.searchTravelsSearchView.getQuery().toString().trim().isEmpty()) {
            Criterion.KEYWORDS.setValue(binding.searchTravelsSearchView.getQuery().toString());
            itineraryViewModel.getDocumentsListStartAt(user, lastDocument, 5, queryOrderBy,
                    queryDirection, Criterion.KEYWORDS);
        } else if (!noChangeSearchResult)
            itineraryViewModel.getDocumentsListStartAt(user, lastDocument, 5, queryOrderBy, queryDirection);

        itineraryViewModel.getDocumentsData().observe(getViewLifecycleOwner(), documentSnapshots -> {
            if (documentSnapshots != null && !documentSnapshots.isEmpty()) {
                for (DocumentSnapshot documentSnapshot : documentSnapshots) {
                    Itinerary i = documentSnapshot.toObject(Itinerary.class);
                    list.add(i);
                    adapter.notifyItemInserted(list.size() - 1);
                }
                lastDocument = documentSnapshots.get(documentSnapshots.size() - 1);
            }
            setIsListEmpty();
            stopProgressBar();
        });
    }


    private void reloadList() {
        setQueryOrderDirection();
        loadList(true);
    }


    //SORTING---------------------------------------------------------------------------------------


    private void setQueryOrderDirection() {
        if (!noChangeOrderOption) {
            switch (Sort.values()[binding.searchTravelsSortSpinner.getSelectedIndex()]) {
                case POPULARITY:
                    queryOrderBy = Constants.DB_POPULARITY;
                    queryDirection = Query.Direction.DESCENDING;
                    break;
                case DATE_LATEST:
                    queryOrderBy = Constants.DB_CREATED_DATE;
                    queryDirection = Query.Direction.DESCENDING;
                    break;
                case DATE_OLDEST:
                    queryOrderBy = Constants.DB_CREATED_DATE;
                    queryDirection = Query.Direction.ASCENDING;
                    break;
                case DURATION_LONGEST:
                    queryOrderBy = Constants.DB_DAYS_AMOUNT;
                    queryDirection = Query.Direction.DESCENDING;
                    break;
                case DURATION_SHORTEST:
                    queryOrderBy = Constants.DB_CREATED_DATE;
                    queryDirection = Query.Direction.ASCENDING;
                    break;
            }
        }
    }


    private void resetSorting() {
        binding.searchTravelsSortSpinner.setSelectedIndex(0);
        setQueryOrderDirection();
    }


    //OTHERS----------------------------------------------------------------------------------------


    private void startProgressBar() {
        getProgressBarInteractions().startProgressBar(binding.getRoot(), binding.searchTravelsProgressbarLayout,
                binding.searchTravelsProgressbar);
    }


    private void stopProgressBar() {
        getProgressBarInteractions().stopProgressBar(binding.getRoot(), binding.searchTravelsProgressbarLayout,
                binding.searchTravelsProgressbar);
    }


    private void changeFragment(BaseFragment next) {
        //todo hide keyboard
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
