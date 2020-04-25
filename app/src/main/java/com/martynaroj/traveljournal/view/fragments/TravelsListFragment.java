package com.martynaroj.traveljournal.view.fragments;

import android.app.Dialog;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.DialogCustomBinding;
import com.martynaroj.traveljournal.databinding.DialogEditPrivacyBinding;
import com.martynaroj.traveljournal.databinding.DialogOptionsBinding;
import com.martynaroj.traveljournal.databinding.FragmentTravelsListBinding;
import com.martynaroj.traveljournal.services.models.Itinerary;
import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.view.adapters.TravelAdapter;
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.view.others.classes.DialogHandler;
import com.martynaroj.traveljournal.view.others.classes.FileUriUtils;
import com.martynaroj.traveljournal.view.others.classes.RippleDrawable;
import com.martynaroj.traveljournal.view.others.enums.Privacy;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;
import com.martynaroj.traveljournal.viewmodels.ItineraryViewModel;
import com.martynaroj.traveljournal.viewmodels.StorageViewModel;
import com.martynaroj.traveljournal.viewmodels.UserViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TravelsListFragment extends BaseFragment implements View.OnClickListener {

    private FragmentTravelsListBinding binding;
    private UserViewModel userViewModel;
    private ItineraryViewModel itineraryViewModel;
    private StorageViewModel storageViewModel;

    private User user, loggedUser;
    private List<Itinerary> itineraries, savedItineraries;
    private TravelAdapter adapter;
    private boolean mineTravelsTab;


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
            storageViewModel = new ViewModelProvider(getActivity()).get(StorageViewModel.class);
        }
    }


    private void initContentData() {
        loadUserTravels();
        if (user.equals(loggedUser))
            binding.travelsListButtonsContainer.setVisibility(View.VISIBLE);
        else
            binding.travelsListButtonsContainer.setVisibility(View.GONE);
        mineTravelsTab = true;
    }


    private void observeUserChanges() {
        userViewModel.getUser().observe(getViewLifecycleOwner(), user -> this.loggedUser = user);
    }


    private void initListAdapter() {
        if (getContext() != null && itineraries != null) {
            adapter = new TravelAdapter(getContext(), itineraries);
            binding.travelsListRecyclerView.setAdapter(adapter);
            setOnAdapterListeners();
        }
    }


    private void setBindingData(List<Itinerary> list) {
        if (list != null)
            binding.setIsListEmpty(list.size() == 0);
        else
            binding.setIsListEmpty(true);
    }


    //LISTENERS-------------------------------------------------------------------------------------


    private void setListeners() {
        binding.travelsListArrowButton.setOnClickListener(this);
        binding.travelsListMyTravelsButton.setOnClickListener(this);
        binding.travelsListSavedTravelsButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.travels_list_arrow_button:
                back();
                break;
            case R.id.travels_list_my_travels_button:
                showTravels(itineraries, true);
                break;
            case R.id.travels_list_saved_travels_button:
                showTravels(savedItineraries, false);
                break;
        }
    }


    private void setOnAdapterListeners() {
        adapter.setOnItemClickListener((object, position, view) ->
                changeFragment(TravelFragment.newInstance((Itinerary) object, loggedUser))
        );
        adapter.setOnItemLongClickListener((object, position, view) -> showOptionsDialog((Itinerary)object, position));
    }


    //ITINERARY LIST--------------------------------------------------------------------------------


    private void loadUserTravels() {
        startProgressBar();
        itineraryViewModel.getItinerariesListData(user.getTravels());
        itineraryViewModel.getItinerariesList().observe(getViewLifecycleOwner(), itineraries -> {
            if (itineraries != null) {
                this.itineraries = getProperList(itineraries);
                initListAdapter();
                setBindingData(this.itineraries);
            }
            if (user.equals(loggedUser))
                loadSavedTravels();
            else
                stopProgressBar();
        });
    }


    private List<Itinerary> getProperList(List<Itinerary> itineraries) {
        if (user.equals(loggedUser)) {
            return itineraries;
        } else {
            List<Itinerary> list = new ArrayList<>();
            for (Itinerary i : itineraries) {
                switch (Privacy.values()[i.getPrivacy()]) {
                    case PUBLIC:
                        list.add(i);
                        break;
                    case FRIENDS:
                        if (loggedUser != null && user.getFriends().contains(loggedUser.getUid()))
                            list.add(i);
                        break;
                    case ONLY_ME:
                        break;
                }
            }
            return list;
        }
    }


    private void loadSavedTravels() {
        itineraryViewModel.getItinerariesListData(user.getSavedTravels());
        itineraryViewModel.getItinerariesList().observe(getViewLifecycleOwner(), itineraries -> {
            this.savedItineraries = itineraries;
            stopProgressBar();
        });
    }


    private void showTravels(List<Itinerary> list, boolean mine) {
        switchStyleButtons(mine);
        mineTravelsTab = mine;
        adapter.changeList(list);
        setBindingData(list);
    }


    private void switchStyleButtons(boolean mine) {
        int blue = getResources().getColor(R.color.main_blue);
        int white = getResources().getColor(R.color.white);
        if (mine) {
            binding.travelsListMyTravelsButton.setBackgroundColor(blue);
            binding.travelsListMyTravelsButton.setTextColor(white);
            binding.travelsListSavedTravelsButton.setBackgroundColor(white);
            binding.travelsListSavedTravelsButton.setTextColor(blue);
        } else {
            binding.travelsListMyTravelsButton.setBackgroundColor(white);
            binding.travelsListMyTravelsButton.setTextColor(blue);
            binding.travelsListSavedTravelsButton.setBackgroundColor(blue);
            binding.travelsListSavedTravelsButton.setTextColor(white);
        }
    }


    //UPDATES---------------------------------------------------------------------------------------


    private void removeTravel(Itinerary itinerary, int index) {
        if (mineTravelsTab) {
            adapter.remove(index);
            itineraries = adapter.getList();
            setBindingData(itineraries);
            updateUser(true, user, Constants.DB_TRAVELS, itineraries);
            updateUsersSavedTravels(itinerary, true);
            removeItinerary(itinerary);
        } else {
            adapter.remove(index);
            savedItineraries = adapter.getList();
            setBindingData(savedItineraries);
            updateUser(false, user, Constants.DB_SAVED_TRAVELS, savedItineraries);
            itineraryViewModel.updateItinerary(itinerary, new HashMap<String, Object>(){{
                put(Constants.DB_POPULARITY, itinerary.getPopularity()-1);
            }});
        }
    }


    private void removeItinerary(Itinerary itinerary) {
        itineraryViewModel.removeItinerary(itinerary.getId());
        String mainPath = user.getUid() + "/" + Constants.STORAGE_TRAVELS + "/" + itinerary.getId() + "/";
        if (itinerary.getImage() != null)
            storageViewModel.removeFileFromStorage(mainPath + FileUriUtils.getFileName(getContext(), Uri.parse(itinerary.getImage())));
        storageViewModel.removeFileFromStorage(mainPath + FileUriUtils.getFileName(getContext(), Uri.parse(itinerary.getFile())));
    }


    private void updateUser(boolean reload, User user, String key, Object value) {
        userViewModel.updateUser(reload, user, new HashMap<String, Object>() {{
            put(key, value);
        }});
        updateUserLists();
    }


    private void updateUserLists() {
        List<String> itineraries = new ArrayList<>();
        for(Itinerary itinerary : this.itineraries)
            itineraries.add(itinerary.getId());
        user.setTravels(itineraries);
        itineraries = new ArrayList<>();
        for(Itinerary itinerary : this.savedItineraries)
            itineraries.add(itinerary.getId());
        user.setSavedTravels(itineraries);
        userViewModel.setUser(user);
    }


    private void updateUsersSavedTravels(Itinerary itinerary, boolean all) {
        userViewModel.getUsersWhereArrayContains(Constants.DB_SAVED_TRAVELS, itinerary.getId());
        userViewModel.getUsersList().observe(getViewLifecycleOwner(), users -> {
            if (users != null)
                for (User user : users) {
                    if (all || !user.getFriends().contains(this.loggedUser.getUid())) {
                        user.getSavedTravels().remove(itinerary.getId());
                        itinerary.subtractPopularity();
                        itineraryViewModel.updateItinerary(itinerary, new HashMap<String, Object>(){{
                            put(Constants.DB_POPULARITY, itinerary.getPopularity());
                        }});
                        updateUser(false, user, Constants.DB_SAVED_TRAVELS, user.getSavedTravels());
                    }
                }
        });
    }


    private void updatePrivacy(Itinerary itinerary, int index, int privacy) {
        itineraryViewModel.updateItinerary(itinerary, new HashMap<String, Object>() {{
            put(Constants.DB_PRIVACY, privacy);
        }});
        itinerary.setPrivacy(privacy);
        adapter.edit(itinerary, index);
        itineraries = adapter.getList();
        updateUserLists();
        switch (Privacy.values()[privacy]) {
            case ONLY_ME:
                updateUsersSavedTravels(itinerary, true);
                break;
            case FRIENDS:
                updateUsersSavedTravels(itinerary, false);
                break;
            case PUBLIC:
                break;
        }
        showSnackBar(getResources().getString(R.string.messages_edit_travel_privacy_success), Snackbar.LENGTH_SHORT);
    }


    //DIALOG----------------------------------------------------------------------------------------


    private void showOptionsDialog(Itinerary itinerary, int index) {
        if (getContext() != null && user.equals(loggedUser)) {
            Dialog dialog = DialogHandler.createDialog(getContext(), true);
            DialogOptionsBinding binding = DialogOptionsBinding.inflate(LayoutInflater.from(getContext()));
            dialog.setContentView(binding.getRoot());

            binding.dialogOptionsEdit.setTextColor(getResources().getColor(R.color.main_blue));
            binding.dialogOptionsEdit.setText(getResources().getString(R.string.travels_list_edit_privacy));
            binding.dialogOptionsRemove.setTextColor(getResources().getColor(R.color.main_blue));
            RippleDrawable.setRippleEffectButton(binding.dialogOptionsEdit,
                    Color.TRANSPARENT, getResources().getColor(R.color.blue_bg_light));
            RippleDrawable.setRippleEffectButton(binding.dialogOptionsRemove,
                    Color.TRANSPARENT, getResources().getColor(R.color.blue_bg_light));

            if (mineTravelsTab)
                binding.dialogOptionsEdit.setOnClickListener(view -> {
                    showEditPrivacyDialog(itinerary, index);
                    dialog.dismiss();
                });
            else
                binding.dialogOptionsEdit.setVisibility(View.GONE);

            binding.dialogOptionsRemove.setOnClickListener(view -> {
                showRemoveDialog(itinerary, index);
                dialog.dismiss();
            });
            dialog.show();
        }
    }


    private void showEditPrivacyDialog(Itinerary itinerary, int index) {
        if (getContext() != null) {
            Dialog dialog = DialogHandler.createDialog(getContext(), true);
            DialogEditPrivacyBinding dialogBinding = DialogEditPrivacyBinding.inflate(LayoutInflater.from(getContext()));
            dialog.setContentView(dialogBinding.getRoot());
            dialogBinding.dialogEditPrivacySpinner.setItems(Constants.PUBLIC, Constants.FRIENDS, Constants.ONLY_ME);
            dialogBinding.dialogEditPrivacySpinner.setSelectedIndex(itinerary.getPrivacy());
            dialogBinding.dialogEditPrivacyButtonNegative.setOnClickListener(view -> dialog.dismiss());
            dialogBinding.dialogEditPrivacyButtonPositive.setOnClickListener(view -> {
                updatePrivacy(itinerary, index, dialogBinding.dialogEditPrivacySpinner.getSelectedIndex());
                dialog.dismiss();
            });
            dialog.show();
        }
    }


    private void showRemoveDialog(Itinerary itinerary, int index) {
        if (getContext() != null) {
            Dialog dialog = DialogHandler.createDialog(getContext(), true);
            DialogCustomBinding binding = DialogCustomBinding.inflate(LayoutInflater.from(getContext()));
            dialog.setContentView(binding.getRoot());

            if (mineTravelsTab)
                DialogHandler.initContent(
                        getContext(), binding.dialogCustomTitle, R.string.dialog_remove_my_travel_title,
                        binding.dialogCustomDesc, R.string.dialog_remove_my_travel_desc,
                        binding.dialogCustomButtonPositive, R.string.dialog_button_yes,
                        binding.dialogCustomButtonNegative, R.string.dialog_button_no,
                        R.color.main_blue, R.color.blue_bg_light
                );
            else
                DialogHandler.initContent(
                        getContext(), binding.dialogCustomTitle, R.string.dialog_remove_saved_travel_title,
                        binding.dialogCustomDesc, R.string.dialog_remove_saved_travel_desc,
                        binding.dialogCustomButtonPositive, R.string.dialog_button_yes,
                        binding.dialogCustomButtonNegative, R.string.dialog_button_no,
                        R.color.main_blue, R.color.blue_bg_light
                );

            binding.dialogCustomButtonPositive.setOnClickListener(v -> {
                removeTravel(itinerary, index);
                dialog.dismiss();
            });
            binding.dialogCustomButtonNegative.setOnClickListener(v -> dialog.dismiss());
            dialog.show();
        }
    }


    //OTHERS----------------------------------------------------------------------------------------


    private void showSnackBar(String message, int duration) {
        getSnackBarInteractions().showSnackBar(binding.getRoot(), getActivity(), message, duration);
    }


    private void startProgressBar() {
        getProgressBarInteractions().startProgressBar(binding.getRoot(), binding.travelsListProgressbarLayout,
                binding.travelsListProgressbar);
    }


    private void stopProgressBar() {
        getProgressBarInteractions().stopProgressBar(binding.getRoot(), binding.travelsListProgressbarLayout,
                binding.travelsListProgressbar);
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
