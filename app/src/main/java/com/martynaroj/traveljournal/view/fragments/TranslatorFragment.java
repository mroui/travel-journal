package com.martynaroj.traveljournal.view.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.FragmentTranslatorBinding;
import com.martynaroj.traveljournal.services.models.translatorAPI.TranslationResult;
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;
import com.martynaroj.traveljournal.viewmodels.TranslatorViewModel;
import com.martynaroj.traveljournal.viewmodels.UserViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TranslatorFragment extends BaseFragment implements View.OnClickListener {

    private FragmentTranslatorBinding binding;
    private TranslatorViewModel translatorViewModel;
    private UserViewModel userViewModel;

    private List<String> languagesFromTo;
    private Map<String, String> languageNames;
    private Map<String, List<String>> possibilities;

    private ArrayAdapter<String> adapterFrom;

    private String languageResult;
    private String text;
    private boolean changes;

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

        observeUserChanges();

        return view;
    }


    //INIT DATA-------------------------------------------------------------------------------------


    private void initViewModels() {
        if (getActivity() != null) {
            translatorViewModel = new ViewModelProvider(getActivity()).get(TranslatorViewModel.class);
            userViewModel = new ViewModelProvider(getActivity()).get(UserViewModel.class);
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
            ArrayAdapter<String> adapterTo = new ArrayAdapter<>(getContext(),
                    android.R.layout.simple_spinner_dropdown_item, names.subList(1, names.size()));
            binding.translatorLanguageToSpinner.setAdapter(adapterTo);
            binding.translatorLanguageToSpinner.setSelectedIndex(Constants.LANGUAGE_EN_INDEX);
        }
    }


    private void observeUserChanges() {
        userViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user == null)
                back();
        });
    }


    //LISTENERS-------------------------------------------------------------------------------------


    private void setListeners() {
        binding.translatorArrowButton.setOnClickListener(this);
        binding.translatorLanguageFromSpinner.setOnItemSelectedListener((view, position, id, item) -> {
            if (adapterFrom != null && Objects.equals(adapterFrom.getItem(position), Constants.DETECT_LANGUAGE))
                enableSwapButton(false);
            else
                enableSwapButton(true);
            changes = true;
        });
        binding.translatorLanguageToSpinner.setOnItemSelectedListener((view, position, id, item) -> changes = true);
        binding.translatorTranslateButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.translator_arrow_button:
                back();
                break;
            case R.id.translator_language_swap_icon:
                swapLanguages();
                changes = true;
                break;
            case R.id.translator_translate_button:
                translate();
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
            } else
                showSnackBar(getResources().getString(R.string.messages_error_languages), Snackbar.LENGTH_LONG);
            stopProgressBar();
        });
    }


    private void translate() {
        if (getDataToTranslate()) {
            startProgressBar();
            translatorViewModel.getTranslation(text, languageResult);
            translatorViewModel.getTranslationResultData().observe(getViewLifecycleOwner(), translationResult -> {
                if (translationResult != null)
                    resultTranslationHandling(translationResult);
                else
                    showSnackBar(getResources().getString(R.string.messages_error_translation), Snackbar.LENGTH_LONG);
                stopProgressBar();
            });
        }
    }


    private void resultTranslationHandling(TranslationResult translationResult) {
        switch (translationResult.getCode()) {
            case 200:
                binding.translatorTextToInput.setText(translationResult.getText().get(0));
                break;
            case 401:
            case 402:
                showSnackBar(
                        getResources().getString(R.string.messages_error_translation),
                        Snackbar.LENGTH_LONG
                );
                break;
            case 404:
                showSnackBar(
                        getResources().getString(R.string.messages_error_exceed_translation_daily_limit),
                        Snackbar.LENGTH_LONG
                );
                break;
            case 422:
                showSnackBar(
                        getResources().getString(R.string.messages_error_translation_failed),
                        Snackbar.LENGTH_LONG
                );
                break;
            case 501:
                showSnackBar(
                        getResources().getString(R.string.messages_error_translation_not_supported),
                        Snackbar.LENGTH_LONG
                );
                break;
        }
    }


    private boolean getDataToTranslate() {
        Editable editable = binding.translatorTextFromInput.getText();
        String newText = editable != null ? editable.toString() : "";
        if (!newText.equals("")) {
            if (!newText.equals(text) || changes) {
                changes = false;
                text = newText;
                String languageFrom = (String) binding.translatorLanguageFromSpinner.getItems()
                        .get(binding.translatorLanguageFromSpinner.getSelectedIndex());
                String languageTo = (String) binding.translatorLanguageToSpinner.getItems()
                        .get(binding.translatorLanguageToSpinner.getSelectedIndex());
                if (languageFrom.equals(Constants.DETECT_LANGUAGE))
                    languageFrom = null;
                for (String shortName : languageNames.keySet()) {
                    if (languageFrom != null && languageFrom.equals(languageNames.get(shortName)))
                        languageFrom = shortName;
                    if (languageTo.equals(languageNames.get(shortName)))
                        languageTo = shortName;
                }
                languageResult = "";
                if (languageFrom != null)
                    languageResult = languageFrom + "-";
                languageResult += languageTo;
                return true;
            } else
                return false;
        } else
            return false;
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
        binding.translatorLanguageToSpinner.setSelectedIndex(indexFrom - 1);
        binding.translatorLanguageFromSpinner.setSelectedIndex(indexTo + 1);

        Editable from = binding.translatorTextFromInput.getText();
        Editable to = binding.translatorTextToInput.getText();
        if (from != null && to != null && !from.toString().equals("") && !to.toString().equals("")) {
            binding.translatorTextFromInput.setText(to.toString());
            binding.translatorTextToInput.setText(from.toString());
        }
    }


    private void back() {
        if (getParentFragmentManager().getBackStackEntryCount() > 0)
            getParentFragmentManager().popBackStack();
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
