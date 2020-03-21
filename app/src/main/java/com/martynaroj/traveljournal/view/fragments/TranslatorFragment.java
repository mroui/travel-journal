package com.martynaroj.traveljournal.view.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.FragmentTranslatorBinding;
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;
import com.martynaroj.traveljournal.viewmodels.TranslatorViewModel;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TranslatorFragment extends BaseFragment implements View.OnClickListener {

    private FragmentTranslatorBinding binding;
    private TranslatorViewModel translatorViewModel;

    private List<String> languagesFromTo;
    private Map<String, String> languageNames;
    private Map<String, List<String>> possibilities;

    private ArrayAdapter<String> adapterFrom, adapterTo;

    public static TranslatorFragment newInstance() {
        return new TranslatorFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTranslatorBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initViewModels();
        initContentData();
        setListeners();

        return view;
    }


    //INIT DATA-------------------------------------------------------------------------------------


    private void initViewModels() {
        if (getActivity() != null) {
            translatorViewModel = new ViewModelProvider(getActivity()).get(TranslatorViewModel.class);
        }
    }


    private void initContentData() {
        languagesFromTo = new ArrayList<>();
        languageNames = new HashMap<>();
        possibilities = new HashMap<>();
        getTranslatorLangs();
        enableSwapButton(false);
    }


    private void initPossibleLanguages() {
        for (String fromTo : languagesFromTo) {
            String from = fromTo.substring(0, 2);
            String to = fromTo.substring(3, 5);
            if (!possibilities.containsKey(from)) {
                possibilities.put(from, new ArrayList<>(Collections.singletonList(to)));
            } else {
                List<String> list = possibilities.get(from);
                assert list != null;
                list.add(to);
                possibilities.put(from, list);
            }
        }
    }



    private void fillSpinners() {
        if (getContext() != null) {

            List<String> names = new ArrayList<>();
            for (String key : possibilities.keySet()) {
                names.add(languageNames.get(key));
            }
            Collections.sort(names);
            names.add(0, Constants.DETECT_LANGUAGE);

            adapterFrom = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, names);
            binding.translatorLanguageFromSpinner.setAdapter(adapterFrom);
            binding.translatorLanguageFromSpinner.setSelectedIndex(0);
            adapterTo = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, names.subList(1, names.size()));
            binding.translatorLanguageToSpinner.setAdapter(adapterTo);
            binding.translatorLanguageToSpinner.setSelectedIndex(Constants.LANGUAGE_EN_INDEX);
        }
    }

    //LISTENERS-------------------------------------------------------------------------------------


    private void setListeners() {
        binding.translatorArrowButton.setOnClickListener(this);
        binding.translatorLanguageFromSpinner.setOnItemSelectedListener((view, position, id, item) -> {
            if (adapterFrom != null && Objects.equals(adapterFrom.getItem(position), Constants.DETECT_LANGUAGE)) {
                enableSwapButton(false);
            } else {
                enableSwapButton(true);
            }
        });
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.translator_arrow_button:
                if (getParentFragmentManager().getBackStackEntryCount() > 0)
                    getParentFragmentManager().popBackStack();
                break;
            case R.id.translator_language_swap_icon:
                swapLanguages();
                break;
        }
    }


    //TRANSLATOR------------------------------------------------------------------------------------


    private void getTranslatorLangs() {
        startProgressBar();
        translatorViewModel.getLangs();
        translatorViewModel.getLangsResultData().observe(getViewLifecycleOwner(), langsResult -> {
            if (langsResult != null) {
                languageNames = langsResult.getLangs();
                languagesFromTo = langsResult.getDirs();
                initPossibleLanguages();
                fillSpinners();
            } else {
                showSnackBar(getResources().getString(R.string.messages_error_languages), Snackbar.LENGTH_LONG);
            }
            stopProgressBar();
        });
    }


    private void detectLang(String text) {
        translatorViewModel.detectLang(text);
        translatorViewModel.getDetectLangResultData().observe(getViewLifecycleOwner(), detectLangResult -> {
            if (detectLangResult != null) {
                //todo + check result code
            } else {
                //todo
                //showSnackBar(getResources().getString(R.string.messages_error_localize), Snackbar.LENGTH_LONG);
            }
            stopProgressBar();
        });
    }


    private void translate(String text, String language) {
        translatorViewModel.detectLang(text);
        translatorViewModel.getDetectLangResultData().observe(getViewLifecycleOwner(), detectLangResult -> {
            if (detectLangResult != null) {
                //todo + check result code + from + to
            } else {
                //todo
                //showSnackBar(getResources().getString(R.string.messages_error_localize), Snackbar.LENGTH_LONG);
            }
            stopProgressBar();
        });
    }


    //OTHERS----------------------------------------------------------------------------------------


    private void enableSwapButton(boolean enable) {
        if (enable) {
            binding.translatorLanguageSwapIcon.setOnClickListener(this);
            binding.translatorLanguageSwapIcon.setClickable(true);
            binding.translatorLanguageSwapIcon.setImageResource(R.drawable.ic_swap);
        } else {
            binding.translatorLanguageSwapIcon.setOnClickListener(null);
            binding.translatorLanguageSwapIcon.setClickable(false);
            binding.translatorLanguageSwapIcon.setImageResource(R.drawable.ic_swap_gray);
        }
    }


    private void swapLanguages() {
        int indexTo = binding.translatorLanguageToSpinner.getSelectedIndex();
        int indexFrom = binding.translatorLanguageFromSpinner.getSelectedIndex();
        binding.translatorLanguageToSpinner.setSelectedIndex(indexFrom-1);
        binding.translatorLanguageFromSpinner.setSelectedIndex(indexTo+1);
    }


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
