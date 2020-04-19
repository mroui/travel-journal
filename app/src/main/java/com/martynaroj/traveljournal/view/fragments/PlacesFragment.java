package com.martynaroj.traveljournal.view.fragments;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.DialogAddNoteBinding;
import com.martynaroj.traveljournal.databinding.DialogCustomBinding;
import com.martynaroj.traveljournal.databinding.DialogEditNoteBinding;
import com.martynaroj.traveljournal.databinding.DialogOptionsBinding;
import com.martynaroj.traveljournal.databinding.FragmentPlacesBinding;
import com.martynaroj.traveljournal.services.models.Day;
import com.martynaroj.traveljournal.services.models.Place;
import com.martynaroj.traveljournal.services.models.Travel;
import com.martynaroj.traveljournal.services.others.GooglePlaces;
import com.martynaroj.traveljournal.view.adapters.PlaceAdapter;
import com.martynaroj.traveljournal.view.others.classes.DialogHandler;
import com.martynaroj.traveljournal.view.others.classes.EmojiHandler;
import com.martynaroj.traveljournal.view.others.classes.RippleDrawable;
import com.martynaroj.traveljournal.view.others.enums.Emoji;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class PlacesFragment extends NotesFragment {

    private FragmentPlacesBinding binding;
    private Travel travel;
    private List<Place> places;
    private PlaceAdapter adapter;

    private AutocompleteSupportFragment autocompleteFragment;
    private com.google.android.libraries.places.api.model.Place place;
    private DialogAddNoteBinding dialogAddBinding;
    private EmojiHandler emojiHandler;


    public static PlacesFragment newInstance(Travel travel, Day day, List<Day> days) {
        PlacesFragment fragment = new PlacesFragment();
        Bundle args = new Bundle();
        args.putSerializable(Constants.BUNDLE_TRAVEL, travel);
        args.putSerializable(Constants.BUNDLE_DAY, day);
        args.putSerializable(Constants.BUNDLE_DAYS, (Serializable) days);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            travel = (Travel) getArguments().getSerializable(Constants.BUNDLE_TRAVEL);
        }
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPlacesBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initViewModels();
        initContentData();
        setListeners();
        observeUserChanges(view);

        return view;
    }


    //INIT DATA-------------------------------------------------------------------------------------


    private void initContentData() {
        places = getAllDaysPlacesList();
        initListAdapter();
        setBindingData();
    }


    private void initListAdapter() {
        if (getContext() != null) {
            adapter = new PlaceAdapter(getContext(), places);
            binding.placesListRecyclerView.setAdapter(adapter);
            setOnItemLongClickListener();
        }
    }


    private void setBindingData() {
        binding.setIsListEmpty(places.size() == 0);
    }


    private void initGooglePlaces() {
        if (getActivity() != null) {
            GooglePlaces.init(getContext());
            autocompleteFragment = GooglePlaces.initAutoComplete(
                    getContext(),
                    R.id.dialog_add_note_place_search_fragment,
                    getActivity().getSupportFragmentManager()
            );
            setAutocompleteFragmentListeners();
        }
    }


    //LISTENERS-------------------------------------------------------------------------------------


    private void setListeners() {
        binding.placesArrowButton.setOnClickListener(this);
        binding.placesAddFloatingButton.setOnClickListener(this);
        setOnListScrollListener(binding.placesListRecyclerView, binding.placesAddFloatingButton);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.places_arrow_button:
                back();
                break;
            case R.id.places_add_floating_button:
                showAddPlaceDialog();
                break;
        }
    }


    private void setOnItemLongClickListener() {
        adapter.setOnItemLongClickListener((object, position, view) -> showOptionsDialog((Place) object, position));
    }


    private void setAutocompleteFragmentListeners() {
        if (autocompleteFragment != null && autocompleteFragment.getView() != null) {
            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull com.google.android.libraries.places.api.model.Place newPlace) {
                    dialogAddBinding.dialogAddNotePlaceSearchError.setVisibility(View.GONE);
                    if (newPlace.getLatLng() != null)
                        place = newPlace;
                }

                @Override
                public void onError(@NonNull Status status) {
                }
            });
            autocompleteFragment.getView()
                    .findViewById(R.id.places_autocomplete_clear_button)
                    .setOnClickListener(view -> {
                        autocompleteFragment.setText("");
                        place = null;
                    });
        }
    }


    //PLACES / LIST---------------------------------------------------------------------------------


    private List<Place> getAllDaysPlacesList() {
        List<Place> list = new ArrayList<>();
        if (days != null) {
            for (Day day : days)
                list.addAll(day.getPlaces());
            Collections.sort(list);
            Collections.reverse(list);
        }
        return list;
    }


    private void addPlace() {
        String description = dialogAddBinding.dialogAddNoteInput.getText() != null ?
                dialogAddBinding.dialogAddNoteInput.getText().toString() : null;
        String address = place.getName() + "&" + place.getAddress();
        adapter.add(new Place(description, address, emojiHandler.getSelectedEmoji().ordinal()));
        places = adapter.getList();
        binding.placesListRecyclerView.scrollToPosition(0);
        dayViewModel.updateDay(today.getId(), new HashMap<String, Object>() {{
            put(Constants.DB_PLACES, adapter.getTodayList());
        }});
        today.setPlaces(adapter.getTodayList());
        dayViewModel.setToday(today);
        setBindingData();
    }


    private void removePlace(Place place, int placeIndex) {
        adapter.remove(placeIndex);
        places = adapter.getList();
        Integer dayIndex = getDayIndexOfNote(place);
        if (dayIndex != null) {
            days.get(dayIndex).getPlaces().remove(place);
            updateDay(dayIndex);
        }
    }


    private void editPlaceNote(Place place, int placeIndex) {
        adapter.edit(placeIndex, place);
        places = adapter.getList();
        Integer dayIndex = getDayIndexOfNote(place);
        if (dayIndex != null) {
            days.get(dayIndex).getPlaces().set(days.get(dayIndex).getPlaces().indexOf(place), place);
            updateDay(dayIndex);
        }
    }


    private void updateDay(int index) {
        dayViewModel.updateDay(days.get(index).getId(), new HashMap<String, Object>() {{
            put(Constants.DB_PLACES, days.get(index).getPlaces());
        }});
        dayViewModel.setDays(days);
        setBindingData();
    }


    //DIALOG----------------------------------------------------------------------------------------


    private void showAddPlaceDialog() {
        if (getContext() != null) {
            Dialog dialog = DialogHandler.createDialog(getContext(), true);
            dialogAddBinding = DialogAddNoteBinding.inflate(LayoutInflater.from(getContext()));
            dialog.setContentView(dialogAddBinding.getRoot());
            DialogHandler.initContent(getContext(), dialogAddBinding.dialogAddNoteTitle, R.string.dialog_add_place_title,
                    dialogAddBinding.dialogAddNoteDesc, R.string.dialog_add_place_desc,
                    dialogAddBinding.dialogAddNoteButtonPositive, R.string.dialog_button_add,
                    dialogAddBinding.dialogAddNoteButtonNegative, R.string.dialog_button_cancel,
                    R.color.main_yellow, R.color.yellow_bg_lighter);

            dialogAddBinding.dialogAddNotePhotoContainer.setVisibility(View.GONE);
            dialogAddBinding.dialogAddNoteInputLayout.setBoxStrokeColor(getResources().getColor(R.color.main_yellow));
            initGooglePlaces();
            emojiHandler = new EmojiHandler(dialogAddBinding, Emoji.NORMAL.ordinal());

            dialogAddBinding.dialogAddNoteButtonPositive.setOnClickListener(view -> {
                if (validatePlace()) {
                    addPlace();
                    dismissAddDialog(dialog);
                }
            });
            dialogAddBinding.dialogAddNoteButtonNegative.setOnClickListener(view -> dismissAddDialog(dialog));
            dialog.setOnCancelListener(dialogInterface -> {
                removeAutoCompleteFragment();
                place = null;
            });
            dialog.setOnDismissListener(dialogInterface -> {
                removeAutoCompleteFragment();
                place = null;
            });

            dialog.show();
        }
    }


    private void showOptionsDialog(Place place, int index) {
        if (getContext() != null) {
            Dialog dialog = DialogHandler.createDialog(getContext(), true);
            DialogOptionsBinding binding = DialogOptionsBinding.inflate(LayoutInflater.from(getContext()));
            dialog.setContentView(binding.getRoot());

            binding.dialogOptionsEdit.setTextColor(getResources().getColor(R.color.main_yellow));
            binding.dialogOptionsRemove.setTextColor(getResources().getColor(R.color.main_yellow));
            RippleDrawable.setRippleEffectButton(binding.dialogOptionsEdit,
                    Color.TRANSPARENT, getResources().getColor(R.color.yellow_bg_lighter));
            RippleDrawable.setRippleEffectButton(binding.dialogOptionsRemove,
                    Color.TRANSPARENT, getResources().getColor(R.color.yellow_bg_lighter));

            binding.dialogOptionsEdit.setOnClickListener(view -> {
                showEditPlaceNoteDialog(place, index);
                dialog.dismiss();
            });
            binding.dialogOptionsRemove.setOnClickListener(view -> {
                showRemovePlaceDialog(place, index);
                dialog.dismiss();
            });
            dialog.show();
        }
    }


    private void showEditPlaceNoteDialog(Place place, int index) {
        if (getContext() != null) {
            Dialog dialog = DialogHandler.createDialog(getContext(), true);
            DialogEditNoteBinding binding = DialogEditNoteBinding.inflate(LayoutInflater.from(getContext()));
            dialog.setContentView(binding.getRoot());

            binding.dialogEditNoteTitle.setText(getResources().getString(R.string.dialog_edit_place_title));
            binding.dialogEditNoteDesc.setText(getResources().getString(R.string.dialog_edit_place_desc));
            binding.dialogEditNoteInput.setText(place.getDescription());
            binding.dialogEditNoteInput.setSelection(place.getDescription().length());

            binding.dialogEditNoteInputLayout.setBoxStrokeColor(getResources().getColor(R.color.main_yellow));
            binding.dialogEditNoteButtonPositive.setTextColor(getResources().getColor(R.color.main_yellow));
            binding.dialogEditNoteButtonNegative.setTextColor(getResources().getColor(R.color.main_yellow));
            RippleDrawable.setRippleEffectButton(binding.dialogEditNoteButtonPositive,
                    Color.TRANSPARENT, getResources().getColor(R.color.yellow_bg_lighter));
            RippleDrawable.setRippleEffectButton(binding.dialogEditNoteButtonNegative,
                    Color.TRANSPARENT, getResources().getColor(R.color.yellow_bg_lighter));

            binding.dialogEditNoteButtonPositive.setOnClickListener(view -> {
                String desc = binding.dialogEditNoteInput.getText() != null ?
                        binding.dialogEditNoteInput.getText().toString() : "";
                place.setDescription(desc);
                editPlaceNote(place, index);
                dialog.dismiss();
            });
            binding.dialogEditNoteButtonNegative.setOnClickListener(view -> dialog.dismiss());
            dialog.show();
        }
    }


    private void showRemovePlaceDialog(Place place, int index) {
        if (getContext() != null) {
            Dialog dialog = DialogHandler.createDialog(getContext(), true);
            DialogCustomBinding binding = DialogCustomBinding.inflate(LayoutInflater.from(getContext()));
            dialog.setContentView(binding.getRoot());
            DialogHandler.initContent(
                    getContext(), binding.dialogCustomTitle, R.string.dialog_remove_place_title,
                    binding.dialogCustomDesc, R.string.dialog_remove_place_desc,
                    binding.dialogCustomButtonPositive, R.string.dialog_button_yes,
                    binding.dialogCustomButtonNegative, R.string.dialog_button_no,
                    R.color.main_yellow, R.color.yellow_bg_lighter
            );
            binding.dialogCustomButtonPositive.setOnClickListener(v -> {
                removePlace(place, index);
                dialog.dismiss();
            });
            binding.dialogCustomButtonNegative.setOnClickListener(v -> dialog.dismiss());
            dialog.show();
        }
    }


    //OTHERS----------------------------------------------------------------------------------------


    private boolean validatePlace() {
        if (place != null)
            return true;
        else
            dialogAddBinding.dialogAddNotePlaceSearchError.setVisibility(View.VISIBLE);
        return false;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        dialogAddBinding = null;
    }

}
