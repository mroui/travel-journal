package com.martynaroj.traveljournal.view.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

import com.martynaroj.traveljournal.databinding.ItemPackingGroupBinding;
import com.martynaroj.traveljournal.databinding.ItemPackingItemBinding;
import com.martynaroj.traveljournal.services.models.packing.PackingCategory;
import com.martynaroj.traveljournal.services.models.packing.PackingItem;

import java.util.List;
import java.util.Map;

public class PackingAdapter extends BaseExpandableListAdapter {

    private Context context;
    private ItemPackingGroupBinding bindingGroup;
    private ItemPackingItemBinding bindingItem;
    private List<PackingCategory> listGroup;
    private Map<PackingCategory, List<PackingItem>> listItem;

    public PackingAdapter(Context context, List<PackingCategory> listGroup, Map<PackingCategory, List<PackingItem>> listItem) {
        this.context = context;
        this.listGroup = listGroup;
        this.listItem = listItem;
    }


    @Override
    public int getGroupCount() {
        return listGroup.size();
    }


    @Override
    public int getChildrenCount(int groupIndex) {
        List<PackingItem> children = listItem.get(listGroup.get(groupIndex));
        return children != null ? children.size() : 0;
    }


    @Override
    public PackingCategory getGroup(int groupIndex) {
        return listGroup.get(groupIndex);
    }


    @Override
    public PackingItem getChild(int groupIndex, int itemIndex) {
        List<PackingItem> children = listItem.get(listGroup.get(groupIndex));
        return children != null ? children.get(itemIndex) : null;
    }


    @Override
    public long getGroupId(int groupIndex) {
        return groupIndex;
    }


    @Override
    public long getChildId(int groupIndex, int itemIndex) {
        return groupIndex*itemIndex;
    }


    @Override
    public boolean hasStableIds() {
        return false;
    }


    @SuppressLint("SetTextI18n")
    @Override
    public View getGroupView(int groupIndex, boolean b, View view, ViewGroup viewGroup) {
        if (view == null) {
            bindingGroup = ItemPackingGroupBinding.inflate(LayoutInflater.from(context), viewGroup, false);
            view = bindingGroup.getRoot();
        }
        bindingGroup.packingGroupName.setText(getGroup(groupIndex).getName());
        bindingGroup.packingGroupChildCount.setText("(" + getChildrenCount(groupIndex) + ")");
        return view;
    }


    @Override
    public View getChildView(int groupIndex, int itemIndex, boolean b, View view, ViewGroup viewGroup) {
        if (view == null) {
            bindingItem = ItemPackingItemBinding.inflate(LayoutInflater.from(context), viewGroup, false);
            view = bindingItem.getRoot();
        }
        bindingItem.packingItemName.setText(getChild(groupIndex, itemIndex).getName());
        return view;
    }


    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }

}
