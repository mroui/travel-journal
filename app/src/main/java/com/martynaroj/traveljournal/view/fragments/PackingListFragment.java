package com.martynaroj.traveljournal.view.fragments;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.ExpandableListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.FragmentPackingListBinding;
import com.martynaroj.traveljournal.services.models.Travel;
import com.martynaroj.traveljournal.services.models.packing.PackingCategory;
import com.martynaroj.traveljournal.services.models.packing.PackingItem;
import com.martynaroj.traveljournal.view.adapters.PackingAdapter;
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.view.interfaces.IOnBackPressed;
import com.martynaroj.traveljournal.view.others.classes.RippleDrawable;
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
            Map<PackingCategory, List<PackingItem>> items = new HashMap<>();
            for (PackingCategory category : travel.getPackingList())
                items.put(category, category.getItems());
            adapter = new PackingAdapter(getContext(), travel.getPackingList(), items);
            binding.packingListExpandableList.setAdapter(adapter);
        }
    }


    //LISTENERS-------------------------------------------------------------------------------------


    private void setListeners() {
        binding.packingListArrowButton.setOnClickListener(this);
        binding.packingListMenuButton.setOnClickListener(this);
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


    //DIALOGS---------------------------------------------------------------------------------------


    @SuppressLint("SetTextI18n")
    private void showRemoveDialog(boolean isGroup, int groupIndex, int itemIndex, String name) {
        if (getContext() != null && getActivity() != null) {
            Dialog dialog = new Dialog(getContext());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(true);
            dialog.setContentView(R.layout.dialog_custom);

            TextView title = dialog.findViewById(R.id.dialog_custom_title);
            TextView message = dialog.findViewById(R.id.dialog_custom_desc);
            MaterialButton buttonPositive = dialog.findViewById(R.id.dialog_custom_buttom_positive);
            MaterialButton buttonNegative = dialog.findViewById(R.id.dialog_custom_button_negative);

            title.setText(getResources().getString(R.string.dialog_packing_list_remove_title));
            message.setText(getResources().getString(R.string.dialog_packing_list_remove_desc) + " " + name + "?");
            buttonPositive.setText(getResources().getString(R.string.dialog_button_yes));
            RippleDrawable.setRippleEffectButton(
                    buttonPositive,
                    Color.TRANSPARENT,
                    getResources().getColor(R.color.yellow_bg_lighter)
            );
            buttonPositive.setTextColor(getResources().getColor(R.color.yellow_active));
            buttonPositive.setOnClickListener(v -> {
                adapter.removeItem(isGroup, groupIndex, itemIndex);
                dialog.dismiss();
            });
            buttonNegative.setText(getResources().getString(R.string.dialog_button_no));
            RippleDrawable.setRippleEffectButton(
                    buttonNegative,
                    Color.TRANSPARENT,
                    getResources().getColor(R.color.yellow_bg_lighter)
            );
            buttonNegative.setTextColor(getResources().getColor(R.color.yellow_active));
            buttonNegative.setOnClickListener(v -> dialog.dismiss());

            dialog.show();
        }
    }


    //OTHERS----------------------------------------------------------------------------------------


    private void back() {
        if (getParentFragmentManager().getBackStackEntryCount() > 0)
            getParentFragmentManager().popBackStack();
    }


    @Override
    public boolean onBackPressed() {
        if (isMenuOpen) {
            closeMenu();
            return true;
        } else
            return false;
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
