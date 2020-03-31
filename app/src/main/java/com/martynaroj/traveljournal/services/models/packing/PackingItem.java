package com.martynaroj.traveljournal.services.models.packing;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.martynaroj.traveljournal.BR;

import java.io.Serializable;

public class PackingItem extends BaseObservable implements Serializable {

    private String name;
    private boolean checked;

    public PackingItem() {
    }


    public PackingItem(String name) {
        this.name = name;
    }

    @Bindable
    public String getName() {
        return name;
    }

    @Bindable
    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
        notifyPropertyChanged(BR.checked);
    }

}
