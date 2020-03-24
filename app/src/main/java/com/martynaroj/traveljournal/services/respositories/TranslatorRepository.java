package com.martynaroj.traveljournal.services.respositories;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.services.models.translatorAPI.DetectLangResult;
import com.martynaroj.traveljournal.services.models.translatorAPI.LangsResult;
import com.martynaroj.traveljournal.services.models.translatorAPI.TranslationResult;
import com.martynaroj.traveljournal.services.retrofit.Rest;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TranslatorRepository {

    private Context context;

    public TranslatorRepository(Context context) {
        this.context = context;
    }

    public MutableLiveData<LangsResult> getLangsResult() {
        MutableLiveData<LangsResult> langsResultData = new MutableLiveData<>();
        Rest.getTranslatorService().getTranslatorLangs(
                context.getString(R.string.yandex_translate_api_key),
                Constants.LANGUAGE_EN
        ).enqueue(new Callback<LangsResult>() {
            @Override
            public void onResponse(@NonNull Call<LangsResult> call,
                                   @NonNull Response<LangsResult> response) {
                if (response.isSuccessful()) {
                    langsResultData.setValue(response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<LangsResult> call, @NonNull Throwable t) {
                langsResultData.setValue(null);
            }
        });
        return langsResultData;
    }


    public MutableLiveData<DetectLangResult> detectLang(String text) {
        MutableLiveData<DetectLangResult> detectLangResultMutableLiveData = new MutableLiveData<>();
        Rest.getTranslatorService().detectLang(
                context.getString(R.string.yandex_translate_api_key),
                text
        ).enqueue(new Callback<DetectLangResult>() {
            @Override
            public void onResponse(@NonNull Call<DetectLangResult> call,
                                   @NonNull Response<DetectLangResult> response) {
                if (response.isSuccessful()) {
                    detectLangResultMutableLiveData.setValue(response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<DetectLangResult> call, @NonNull Throwable t) {
                detectLangResultMutableLiveData.setValue(null);
            }
        });
        return detectLangResultMutableLiveData;
    }


    public MutableLiveData<TranslationResult> getTranslation(String text, String lang) {
        MutableLiveData<TranslationResult> translationResultLiveData = new MutableLiveData<>();
        Rest.getTranslatorService().getTranslation(
                context.getString(R.string.yandex_translate_api_key),
                text,
                lang
        ).enqueue(new Callback<TranslationResult>() {
            @Override
            public void onResponse(@NonNull Call<TranslationResult> call,
                                   @NonNull Response<TranslationResult> response) {
                if (response.isSuccessful()) {
                    translationResultLiveData.setValue(response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<TranslationResult> call, @NonNull Throwable t) {
                translationResultLiveData.setValue(null);
            }
        });
        return translationResultLiveData;
    }

}