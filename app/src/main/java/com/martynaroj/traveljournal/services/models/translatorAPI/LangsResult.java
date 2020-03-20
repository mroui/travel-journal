package com.martynaroj.traveljournal.services.models.translatorAPI;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.google.gson.annotations.SerializedName;
import com.martynaroj.traveljournal.BR;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class LangsResult extends BaseObservable implements Serializable {

    @SerializedName("dirs")
    private List<String> dirs = null;

    @SerializedName("langs")
    private Map<String, String> langs = null;


    @Bindable
    public List<String> getDirs() {
        return dirs;
    }

    public void setDirs(List<String> dirs) {
        this.dirs = dirs;
        notifyPropertyChanged(BR.dirs);
    }

    @Bindable
    public Map<String, String> getLangs() {
        return langs;
    }

    public void setLangs(Map<String, String> langs) {
        this.langs = langs;
        notifyPropertyChanged(BR.langs);
    }

}