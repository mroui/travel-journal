package com.martynaroj.traveljournal.view.fragments;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ExpandableListView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.DialogAddPackingItemBinding;
import com.martynaroj.traveljournal.databinding.DialogCustomBinding;
import com.martynaroj.traveljournal.databinding.FragmentPackingListBinding;
import com.martynaroj.traveljournal.services.models.Day;
import com.martynaroj.traveljournal.services.models.Itinerary;
import com.martynaroj.traveljournal.services.models.Travel;
import com.martynaroj.traveljournal.view.adapters.PackingAdapter;
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.view.interfaces.IOnBackPressed;
import com.martynaroj.traveljournal.view.others.classes.DialogHandler;
import com.martynaroj.traveljournal.view.others.classes.FormHandler;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;
import com.martynaroj.traveljournal.viewmodels.TravelViewModel;
import com.martynaroj.traveljournal.viewmodels.UserViewModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PackingListFragment extends BaseFragment implements View.OnClickListener, IOnBackPressed {

    private FragmentPackingListBinding binding;
    private UserViewModel userViewModel;
    private TravelViewModel travelViewModel;
    private Travel travel;
    private PackingAdapter adapter;
    private boolean isMenuOpen;

    private PackingListFragment(Travel travel) {
        this.travel = travel;
    }


    static PackingListFragment newInstance(Travel travel) {
        return new PackingListFragment(travel);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPackingListBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initViewModels();

        observeUserChanges();
        observeTravelChanges();

        initListData();

        setListeners();

        return view;
    }


    //INIT DATA-------------------------------------------------------------------------------------


    private void initViewModels() {
        if (getActivity() != null) {
            userViewModel = new ViewModelProvider(getActivity()).get(UserViewModel.class);
            travelViewModel = new ViewModelProvider(getActivity()).get(TravelViewModel.class);
        }
    }


    private void observeUserChanges() {
        userViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user == null) {
                back();
            } else if (user.getActiveTravelId() != null && !user.getActiveTravelId().equals(""))
                loadTravel(user.getActiveTravelId());
            else
                travelViewModel.setTravel(null);
        });
    }


    private void observeTravelChanges() {
        travelViewModel.getTravel().observe(getViewLifecycleOwner(), travel -> {
            if (travel == null) {
                showSnackBar(getResources().getString(R.string.messages_error_failed_load_travel),
                        Snackbar.LENGTH_LONG);
                back();
            }
            this.travel = travel;
            stopProgressBar();
        });
    }


    private void loadTravel(String id) {
        startProgressBar();
        travelViewModel.getTravelData(id);
        travelViewModel.getTravelLiveData().observe(getViewLifecycleOwner(), travel ->
                travelViewModel.setTravel(travel)
        );
    }


    private void initListData() {
        if (travel != null) {
            Map<Day.PackingCategory, List<Itinerary.PackingItem>> items = new HashMap<>();
            for (Day.PackingCategory category : travel.getPackingList())
                items.put(category, category.getItems());
            adapter = new PackingAdapter(getContext(), travel.getPackingList(), items);
            binding.packingListExpandableList.setAdapter(adapter);
        }
    }


    //LISTENERS-------------------------------------------------------------------------------------


    private void setListeners() {
        binding.packingListArrowButton.setOnClickListener(this);
        binding.packingListMenuButton.setOnClickListener(this);
        binding.packingListFinishButton.setOnClickListener(this);
        binding.packingListAddButton.setOnClickListener(this);
        setOnListLongClickListener();
        setOnListScrollListener();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.packing_list_arrow_button:
                back();
                break;
            case R.id.packing_list_menu_button:
                onClickMenu();
                break;
            case R.id.packing_list_finish_button:
                showFinishDialog();
                break;
            case R.id.packing_list_add_button:
                showAddDialog();
                break;
        }
    }


    private void setOnListLongClickListener() {
        binding.packingListExpandableList.setOnItemLongClickListener((parent, view, index, id) -> {
            if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                int groupIndex = ExpandableListView.getPackedPositionGroup(id);
                int itemIndex = ExpandableListView.getPackedPositionChild(id);
                showRemoveDialog(false, groupIndex, itemIndex, adapter.getChild(groupIndex, itemIndex).getName());
                return true;
            } else if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                int groupIndex = ExpandableListView.getPackedPositionGroup(id);
                showRemoveDialog(true, groupIndex, 0, adapter.getGroup(groupIndex).getName());
                return true;
            }
            return false;
        });
    }


    private void setOnListScrollListener() {
        binding.packingListExpandableList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE) {
                    binding.packingListMenuButton.show();
                    binding.packingListAddButton.show();
                    binding.packingListFinishButton.show();
                } else {
                    if (isMenuOpen)
                        closeMenu();
                    binding.packingListMenuButton.hide();
                    binding.packingListAddButton.hide();
                    binding.packingListFinishButton.hide();
                }
            }

            @Override
            public void onScroll(AbsListView view, int i, int i1, int i2) {
            }
        });
    }


    //MENU------------------------------------------------------------------------------------------


    private void onClickMenu() {
        if (isMenuOpen)
            closeMenu();
        else
            openMenu();
    }


    private void openMenu() {
        isMenuOpen = true;
        binding.packingListAddButton.animate()
                .translationY(-getResources().getDimension(R.dimen.packing_menu_button_1_height));
        binding.packingListFinishButton.animate()
                .translationY(-getResources().getDimension(R.dimen.packing_menu_button_2_height));
    }


    private void closeMenu() {
        isMenuOpen = false;
        binding.packingListAddButton.animate().translationY(0);
        binding.packingListFinishButton.animate().translationY(0);
    }


    //PACKING---------------------------------------------------------------------------------------


    private void finishPacking() {
        travel.setPacking(false);
        travelViewModel.updateTravel(travel.getId(), new HashMap<String, Object>() {{
            put(Constants.DB_IS_PACKING, false);
        }});
        travelViewModel.setTravel(travel);
        if (isMenuOpen)
            closeMenu();
        back();
    }


    private void updatePackingList() {
        travel.setPackingList(adapter.getList());
        travelViewModel.setTravel(travel);
        travelViewModel.updateTravel(travel.getId(), new HashMap<String, Object>() {{
            put(Constants.DB_PACKING_LIST, adapter.getList());
        }});
    }


    private void addItem(TextInputEditText nameInput, MaterialAutoCompleteTextView categoryInput) {
        String itemName = nameInput.getText() != null ? nameInput.getText().toString() : "";
        String categoryName = categoryInput.getText() != null ? categoryInput.getText().toString() : "";
        Day.PackingCategory newCategory = new Day.PackingCategory(categoryName);
        Itinerary.PackingItem newItem = new Itinerary.PackingItem(itemName);
        if (!adapter.getGroupNamesList().contains(categoryName))
            adapter.addGroup(newCategory);
        adapter.addItem(newCategory, newItem);
        updatePackingList();
    }


    //DIALOGS---------------------------------------------------------------------------------------


    @SuppressLint("SetTextI18n")
    private void showRemoveDialog(boolean isGroup, int groupIndex, int itemIndex, String name) {
        if (getContext() != null) {
            Dialog dialog = DialogHandler.createDialog(getContext(), true);
            DialogCustomBinding binding = DialogCustomBinding.inflate(LayoutInflater.from(getContext()));
            dialog.setContentView(binding.getRoot());
            DialogHandler.initContent(getContext(), binding.dialogCustomTitle, R.string.dialog_packing_list_remove_title,
                    binding.dialogCustomDesc, R.string.dialog_packing_list_remove_desc,
                    binding.dialogCustomButtonPositive, R.string.dialog_button_yes,
                    binding.dialogCustomButtonNegative, R.string.dialog_button_no,
                    R.color.main_yellow, R.color.yellow_bg_lighter);
            binding.dialogCustomDesc.setText(
                    getResources().getString(R.string.dialog_packing_list_remove_desc) + " " + name + "?"
            );
            binding.dialogCustomButtonPositive.setOnClickListener(v -> {
                adapter.removeItem(isGroup, groupIndex, itemIndex);
                updatePackingList();
                dialog.dismiss();
            });
            binding.dialogCustomButtonNegative.setOnClickListener(v -> dialog.dismiss());
            dialog.show();
        }
    }


    @SuppressLint("SetTextI18n")
    private void showFinishDialog() {
        if (getContext() != null) {
            Dialog dialog = DialogHandler.createDialog(getContext(), true);
            DialogCustomBinding binding = DialogCustomBinding.inflate(LayoutInflater.from(getContext()));
            dialog.setContentView(binding.getRoot());
            DialogHandler.initContent(getContext(), binding.dialogCustomTitle, R.string.dialog_packing_list_finish_title,
                    binding.dialogCustomDesc, R.string.dialog_packing_list_finish_desc,
                    binding.dialogCustomButtonPositive, R.string.dialog_button_yes,
                    binding.dialogCustomButtonNegative, R.string.dialog_button_no,
                    R.color.main_blue, R.color.blue_bg_light);
            binding.dialogCustomButtonPositive.setOnClickListener(v -> {
                finishPacking();
                dialog.dismiss();
            });
            binding.dialogCustomButtonNegative.setOnClickListener(v -> dialog.dismiss());
            dialog.show();
        }
    }

    private void showAddDialog() {
        if (getContext() != null && getActivity() != null) {
            Dialog dialog = DialogHandler.createDialog(getContext(), true);
            DialogAddPackingItemBinding binding = DialogAddPackingItemBinding.inflate(LayoutInflater.from(getContext()));
            dialog.setContentView(binding.getRoot());
            binding.dialogAddPackingItemCategoryInput.setThreshold(1);
            binding.dialogAddPackingItemCategoryInput.setAdapter(new ArrayAdapter<>(
                    getContext(),
                    android.R.layout.simple_spinner_dropdown_item,
                    adapter.getGroupNamesList())
            );
            binding.dialogAddPackingItemAddButton.setOnClickListener(v -> {
                if (validateInput(binding.dialogAddPackingItemNameInput, binding.dialogAddPackingItemNameInputLayout)
                        && validateInput(binding.dialogAddPackingItemCategoryInput, binding.dialogAddPackingItemCategoryInputLayout)) {
                    addItem(binding.dialogAddPackingItemNameInput, binding.dialogAddPackingItemCategoryInput);
                    dialog.dismiss();
                }
            });
            binding.dialogAddPackingItemCancelButton.setOnClickListener(v -> dialog.dismiss());
            dialog.show();
        }
    }


    //OTHERS----------------------------------------------------------------------------------------


    private boolean validateInput(TextInputEditText input, TextInputLayout layout) {
        return new FormHandler(getContext()).validateInput(input, layout);
    }


    private boolean validateInput(AutoCompleteTextView input, TextInputLayout layout) {
        return new FormHandler(getContext()).validateInput(input, layout);
    }


    private void back() {
        updatePackingList();
        if (getParentFragmentManager().getBackStackEntryCount() > 0)
            getParentFragmentManager().popBackStack();
    }


    @Override
    public boolean onBackPressed() {
        if (isMenuOpen) {
            closeMenu();
            return true;
        } else {
            updatePackingList();
            return false;
        }
    }


    private void startProgressBar() {
        getProgressBarInteractions().startProgressBar(binding.getRoot(),
                binding.packingListProgressbarLayout, binding.packingListProgressbar);
    }


    private void stopProgressBar() {
        getProgressBarInteractions().stopProgressBar(binding.getRoot(),
                binding.packingListProgressbarLayout, binding.packingListProgressbar);
    }


    private void showSnackBar(String message, int duration) {
        getSnackBarInteractions().showSnackBar(binding.getRoot(), getActivity(), message, duration);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

}
