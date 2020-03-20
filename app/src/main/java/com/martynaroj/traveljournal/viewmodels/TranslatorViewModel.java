package com.martynaroj.traveljournal.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.martynaroj.traveljournal.services.models.translatorAPI.DetectLangResult;
import com.martynaroj.traveljournal.services.models.translatorAPI.LangsResult;
import com.martynaroj.traveljournal.services.models.translatorAPI.TranslationResult;
import com.martynaroj.traveljournal.services.respositories.TranslatorRepository;

public class TranslatorViewModel extends AndroidViewModel {

    private LiveData<LangsResult> langsResultLiveData;
    private LiveData<DetectLangResult> detectLangResultLiveData;
    private LiveData<TranslationResult> translationResultLiveData;
    private TranslatorRepository translatorRepository;

    public TranslatorViewModel(@NonNull Application application) {
        super(application);
        translatorRepository = new TranslatorRepository(application.getApplicationContext());
    }

    public void getLangs() {
        langsResultLiveData = translatorRepository.getLangsResult();
    }

    public LiveData<LangsResult> getLangsResultData() {
        return langsResultLiveData;
    }

    public void detectLang(String text) {
        detectLangResultLiveData = translatorRepository.detectLang(text);
    }

    public LiveData<DetectLangResult> getDetectLangResultData() {
        return detectLangResultLiveData;
    }

    public void getTranslation(String text, String lang) {
        translationResultLiveData = translatorRepository.getTranslation(text, lang);
    }

    public LiveData<TranslationResult> getTranslationResultData() {
        return translationResultLiveData;
    }

}