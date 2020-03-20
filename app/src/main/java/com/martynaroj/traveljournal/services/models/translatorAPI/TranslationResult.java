package com.martynaroj.traveljournal.services.models.translatorAPI;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.google.gson.annotations.SerializedName;
import com.martynaroj.traveljournal.BR;

import java.io.Serializable;
import java.util.List;

public class TranslationResult extends BaseObservable implements Serializable {

    @SerializedName("code")
    private Integer code;

    @SerializedName("lang")
    private String lang;

    @SerializedName("text")
    private List<String> text = null;


    @Bindable
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
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

    @Bindable
    public List<String> getText() {
        return text;
    }

    public void setText(List<String> text) {
        this.text = text;
        notifyPropertyChanged(BR.text);
    }

}
