package com.martynaroj.traveljournal.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.martynaroj.traveljournal.services.models.currencyAPI.CurrencyExchangeResult;
import com.martynaroj.traveljournal.services.respositories.CurrencyRepository;

public class CurrencyViewModel extends AndroidViewModel {

    private LiveData<CurrencyExchangeResult> currencyExchangeResultLiveData;
    private CurrencyRepository currencyRepository;

    public CurrencyViewModel(@NonNull Application application) {
        super(application);
        currencyRepository = new CurrencyRepository();
    }

    public void getCurrencyExchange(String from, String to) {
        currencyExchangeResultLiveData = currencyRepository.getCurrencyExchangeResult(from, to);
    }

    public LiveData<CurrencyExchangeResult> getCurrencyExchangeResultData() {
        return currencyExchangeResultLiveData;
    }

}