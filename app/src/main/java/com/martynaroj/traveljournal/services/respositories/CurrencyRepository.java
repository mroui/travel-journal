package com.martynaroj.traveljournal.services.respositories;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.martynaroj.traveljournal.services.models.currencyAPI.CurrencyExchangeResult;
import com.martynaroj.traveljournal.services.retrofit.Rest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CurrencyRepository {

    public CurrencyRepository() {
    }

    public MutableLiveData<CurrencyExchangeResult> getCurrencyExchangeResult(String from, String to) {
        MutableLiveData<CurrencyExchangeResult> currencyExchangeResultData = new MutableLiveData<>();
        Rest.getCurrencyService().getLatestCurrencyExchange(
                from,
                to
        ).enqueue(new Callback<CurrencyExchangeResult>() {
            @Override
            public void onResponse(@NonNull Call<CurrencyExchangeResult> call,
                                   @NonNull Response<CurrencyExchangeResult> response) {
                if (response.isSuccessful()) {
                    currencyExchangeResultData.setValue(response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<CurrencyExchangeResult> call, @NonNull Throwable t) {
                currencyExchangeResultData.setValue(null);
            }
        });
        return currencyExchangeResultData;
    }

}