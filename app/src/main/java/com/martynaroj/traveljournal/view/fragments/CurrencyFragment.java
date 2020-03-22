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
import com.martynaroj.traveljournal.databinding.FragmentCurrencyBinding;
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.view.others.classes.InputTextWatcher;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;
import com.martynaroj.traveljournal.viewmodels.CurrencyViewModel;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CurrencyFragment extends BaseFragment implements View.OnClickListener {

    private FragmentCurrencyBinding binding;
    private CurrencyViewModel currencyViewModel;

    private List<String> currencies;
    private int selectedFrom, selectedTo;
    private String converted;
    private boolean changes;

    public static CurrencyFragment newInstance() {
        return new CurrencyFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCurrencyBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initViewModels();
        initContentData();
        setListeners();

        return view;
    }


    //INIT DATA-------------------------------------------------------------------------------------


    private void initViewModels() {
        if (getActivity() != null) {
            currencyViewModel = new ViewModelProvider(getActivity()).get(CurrencyViewModel.class);
        }
    }


    private void initContentData() {
        getCurrencyData("", "", true);
    }


    //LISTENERS-------------------------------------------------------------------------------------


    private void setListeners() {
        binding.currencyArrowButton.setOnClickListener(this);
        binding.currencySwapIcon.setOnClickListener(this);
        binding.currencyConvertButton.setOnClickListener(this);
        binding.currencyToSpinner.setOnItemSelectedListener((view, position, id, item) -> {
            if (selectedFrom == position)
                swapCurrencies(position, selectedTo);
            else
                selectedTo = position;
            changes = true;
        });
        binding.currencyFromSpinner.setOnItemSelectedListener((view, position, id, item) -> {
            if (selectedTo == position)
                swapCurrencies(selectedFrom, position);
            else
                selectedFrom = position;
            changes = true;
        });
        binding.currencyAmountInput.addTextChangedListener(new InputTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (binding.currencyAmountInput.hasFocus() && s != null) {
                    String text = s.toString();
                    if (text.contains(".")) {
                        if ((text.substring(text.indexOf(".")).length() > 3
                                && text.length() <= Constants.MAX_CURRENCY_LENGTH)
                                || text.substring(text.length() - 1).equals(".")
                                && text.length() >= Constants.MAX_CURRENCY_LENGTH) {
                            text = text.substring(0, text.length() - 1);
                            binding.currencyAmountInput.setText(text);
                            binding.currencyAmountInput.setSelection(text.length());
                        }
                    }
                    changes = true;
                }
            }
        });
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.currency_arrow_button:
                if (getParentFragmentManager().getBackStackEntryCount() > 0)
                    getParentFragmentManager().popBackStack();
                break;
            case R.id.currency_swap_icon:
                swapCurrencies(
                        binding.currencyFromSpinner.getSelectedIndex(),
                        binding.currencyToSpinner.getSelectedIndex()
                );
                break;
            case R.id.currency_convert_button:
                if (binding.currencyAmountInput.getText() != null
                    && !binding.currencyAmountInput.getText().toString().equals("")
                    && Double.parseDouble(binding.currencyAmountInput.getText().toString()) != 0
                        &&
                    (converted == null
                    || (binding.currencyAmountInput.getText() != null
                    && !converted.equals(binding.currencyAmountInput.getText().toString())))
                        && changes) {

                    String from = currencies.get(binding.currencyFromSpinner.getSelectedIndex());
                    String to = currencies.get(binding.currencyToSpinner.getSelectedIndex());
                    getCurrencyData(from, to, false);
                }
        }
    }


    //CURRENCY--------------------------------------------------------------------------------------


    private void swapCurrencies(int from, int to) {
        binding.currencyToSpinner.setSelectedIndex(from);
        binding.currencyFromSpinner.setSelectedIndex(to);
        selectedTo = from;
        selectedFrom = to;
        changes = true;
    }


    private void fillSpinners(Map<String, Double> rates) {
        if (getContext() != null) {
            currencies = new ArrayList<>(rates.keySet());
            currencies.add(Constants.CURRENCY_EUR);
            Collections.sort(currencies);
            ArrayAdapter<String> currenciesAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, currencies);
            binding.currencyFromSpinner.setAdapter(currenciesAdapter);
            binding.currencyToSpinner.setAdapter(currenciesAdapter);
            binding.currencyFromSpinner.setSelectedIndex(Constants.CURRENCY_EUR_INDEX);
            selectedFrom = Constants.CURRENCY_EUR_INDEX;
            binding.currencyToSpinner.setSelectedIndex(0);
            selectedTo = 0;
        }
    }


    private void getCurrencyData(String from, String to, boolean possibleRates) {
        startProgressBar();
        currencyViewModel.getCurrencyExchange(from, to);
        currencyViewModel.getCurrencyExchangeResultData().observe(getViewLifecycleOwner(), currencyExchangeResult -> {
            if (currencyExchangeResult != null) {
                if (possibleRates) {
                    fillSpinners(currencyExchangeResult.getRates());
                } else if (currencyExchangeResult.getRates().get(to) != null) {
                    converted = Objects.requireNonNull(this.binding.currencyAmountInput.getText()).toString();
                    binding.currencyConvertResultInput.setText(calculateAmount(currencyExchangeResult.getRates().get(to)));
                } else {
                    showSnackBar(getResources().getString(R.string.messages_error_failed_convertion), Snackbar.LENGTH_LONG);
                }
            } else if (!possibleRates) {
                showSnackBar(getResources().getString(R.string.messages_error_failed_convertion), Snackbar.LENGTH_LONG);
            } else {
                showSnackBar(getResources().getString(R.string.messages_error_failed_currencies), Snackbar.LENGTH_LONG);
            }
            stopProgressBar();
        });
        changes = false;
    }


    private String calculateAmount(Double rate) {
        Double result = rate * Double.parseDouble(Objects.requireNonNull(binding.currencyAmountInput.getText()).toString());
        return new DecimalFormat("#.##").format(result);
    }


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
