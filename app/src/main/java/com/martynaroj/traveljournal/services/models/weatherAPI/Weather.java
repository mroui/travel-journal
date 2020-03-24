package com.martynaroj.traveljournal.services.models.weatherAPI;

import android.widget.ImageView;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.google.gson.annotations.SerializedName;
import com.martynaroj.traveljournal.BR;

import java.io.Serializable;

public class Weather extends BaseObservable implements Serializable {

    @SerializedName("description")
    private String description;

    @SerializedName("icon")
    private String icon;


    @Bindable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        notifyPropertyChanged(BR.description);
    }

    @Bindable
    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
        notifyPropertyChanged(BR.icon);
    }

    @BindingAdapter("iconUrl")
    public static void loadIcon(ImageView v, String imgUrl) {
        Glide.with(v.getContext())
                .load("https://openweathermap.org/img/wn/" + imgUrl + "@2x.png")
                .into(v);
    }

}