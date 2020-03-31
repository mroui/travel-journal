package com.martynaroj.traveljournal.services.models.packing;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PackingCategory extends BaseObservable implements Serializable {

    private String name;
    private List<PackingItem> items;

    public PackingCategory() {
    }


    public PackingCategory(String name) {
        this.name = name;
        items = new ArrayList<>();
    }

    @Bindable
    public String getName() {
        return name;
    }

    @Bindable
    public List<PackingItem> getItems() {
        return items;
    }

    public void addItem(PackingItem item) {
        items.add(item);
    }

}
