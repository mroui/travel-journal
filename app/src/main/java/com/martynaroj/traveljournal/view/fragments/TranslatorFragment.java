package com.martynaroj.traveljournal.view.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.FragmentTranslatorBinding;
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;
import com.martynaroj.traveljournal.viewmodels.TranslatorViewModel;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class TranslatorFragment extends BaseFragment implements View.OnClickListener {

    private FragmentTranslatorBinding binding;
    private TranslatorViewModel translatorViewModel;

    public static TranslatorFragment newInstance() {
        return new TranslatorFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTranslatorBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initViewModels();
        setListeners();

        return view;
    }


    //INIT DATA-------------------------------------------------------------------------------------


    private void initViewModels() {
        if (getActivity() != null) {
            translatorViewModel = new ViewModelProvider(getActivity()).get(TranslatorViewModel.class);
        }
    }


    //LISTENERS-------------------------------------------------------------------------------------


    private void setListeners() {
        binding.translatorArrowButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.translator_arrow_button:
                if (getParentFragmentManager().getBackStackEntryCount() > 0)
                    getParentFragmentManager().popBackStack();
                break;
        }
    }


    //TRANSLATOR------------------------------------------------------------------------------------


    private void getTranslatorLangs() {
        translatorViewModel.getLangs();
        translatorViewModel.getLangsResultData().observe(getViewLifecycleOwner(), langsResult -> {
            if (langsResult != null) {
                //todo
                // in detect lang & translation check result code
                //
            } else {
                //todo
                //showSnackBar(getResources().getString(R.string.messages_error_localize), Snackbar.LENGTH_LONG);
            }
            stopProgressBar();
        });
    }


    //OTHERS----------------------------------------------------------------------------------------


    private void encodeURL(String text) {
        try {
            String query = URLEncoder.encode(text, Constants.UTF_8);
        } catch (UnsupportedEncodingException e) {
            //todo
            //showSnackBar("problem");
        }
    }


    private void startProgressBar() {
        getProgressBarInteractions().startProgressBar(binding.getRoot(),
                binding.translatorProgressbarLayout, binding.translatorProgressbar);
    }


    private void stopProgressBar() {
        getProgressBarInteractions().stopProgressBar(binding.getRoot(),
                binding.translatorProgressbarLayout, binding.translatorProgressbar);
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
