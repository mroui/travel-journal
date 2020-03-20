package com.martynaroj.traveljournal.services.models.translatorAPI;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.google.gson.annotations.SerializedName;
import com.martynaroj.traveljournal.BR;

import java.io.Serializable;

public class DetectLangResult extends BaseObservable implements Serializable {

    @SerializedName("code")
    private String code;

    @SerializedName("lang")
    private String lang;


    @Bindable
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
        notifyPropertyChanged(BR.code);
    }

    @Bindable
    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
        notifyPropertyChanged(BR.lang);
    }

}