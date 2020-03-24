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

    static TravelsListFragment newInstance() {
        return new TravelsListFragment();
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
                if (getParentFragmentManager().getBackStackEntryCount() > 0)
                    getParentFragmentManager().popBackStack();
        }
    }


    //OTHERS----------------------------------------------------------------------------------------


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
