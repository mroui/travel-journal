package com.martynaroj.traveljournal.view.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.FragmentCurrencyBinding;
import com.martynaroj.traveljournal.view.base.BaseFragment;

public class CurrencyFragment extends BaseFragment implements View.OnClickListener {

    private FragmentCurrencyBinding binding;

    public static CurrencyFragment newInstance() {
        return new CurrencyFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCurrencyBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        setListeners();

        return view;
    }


    //INIT DATA-------------------------------------------------------------------------------------


    //


    //LISTENERS-------------------------------------------------------------------------------------


    private void setListeners() {
        binding.currencyArrowButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.currency_arrow_button:
                if (getParentFragmentManager().getBackStackEntryCount() > 0)
                    getParentFragmentManager().popBackStack();
                break;
        }
    }


    //CURRENCY--------------------------------------------------------------------------------------


    //


    //OTHERS----------------------------------------------------------------------------------------


    private void startProgressBar() {
        getProgressBarInteractions().startProgressBar(binding.getRoot(),
                binding.currencyProgressbarLayout, binding.currencyProgressbar);
    }


    private void stopProgressBar() {
        getProgressBarInteractions().stopProgressBar(binding.getRoot(),
                binding.currencyProgressbarLayout, binding.currencyProgressbar);
    }


    private void showSnackBar(String message, int duration) {
        getSnackBarInteractions().showSnackBar(binding.getRoot(), getActivity(), message, duration);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
