package com.martynaroj.traveljournal.view.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.FragmentPackingListBinding;
import com.martynaroj.traveljournal.services.models.Travel;
import com.martynaroj.traveljournal.view.base.BaseFragment;

public class PackingListFragment extends BaseFragment implements View.OnClickListener {

    private FragmentPackingListBinding binding;
    private Travel travel;

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

        setListeners();

        return view;
    }


    //INIT DATA-------------------------------------------------------------------------------------


    //


    //LISTENERS-------------------------------------------------------------------------------------


    private void setListeners() {
        binding.packingListArrowButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.packing_list_arrow_button:
                back();
                break;
        }
    }


    //OTHERS----------------------------------------------------------------------------------------


    private void back() {
        if (getParentFragmentManager().getBackStackEntryCount() > 0)
            getParentFragmentManager().popBackStack();
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
