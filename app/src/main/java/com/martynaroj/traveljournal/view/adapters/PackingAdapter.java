package com.martynaroj.traveljournal.view.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.services.models.Day;
import com.martynaroj.traveljournal.services.models.Itinerary;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PackingAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<Day.PackingCategory> listGroup;
    private Map<Day.PackingCategory, List<Itinerary.PackingItem>> listItem;

    public PackingAdapter(Context context, List<Day.PackingCategory> listGroup,
                          Map<Day.PackingCategory, List<Itinerary.PackingItem>> listItem) {
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
        List<Itinerary.PackingItem> children = listItem.get(listGroup.get(groupIndex));
        return children != null ? children.size() : 0;
    }


    @Override
    public Day.PackingCategory getGroup(int groupIndex) {
        return listGroup.get(groupIndex);
    }


    @Override
    public Itinerary.PackingItem getChild(int groupIndex, int itemIndex) {
        List<Itinerary.PackingItem> children = listItem.get(listGroup.get(groupIndex));
        return children != null ? children.get(itemIndex) : null;
    }


    @Override
    public long getGroupId(int groupIndex) {
        return groupIndex;
    }


    @Override
    public long getChildId(int groupIndex, int itemIndex) {
        return itemIndex;
    }


    @Override
    public boolean hasStableIds() {
        return false;
    }


    @SuppressLint({"SetTextI18n", "InflateParams"})
    @Override
    public View getGroupView(int groupIndex, boolean b, View view, ViewGroup viewGroup) {
        final Day.PackingCategory category = getGroup(groupIndex);
        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert layoutInflater != null;
            view = layoutInflater.inflate(R.layout.item_packing_group, null);
        }
        ((TextView) view.findViewById(R.id.packing_group_name)).setText(category.getName());
        ((TextView) view.findViewById(R.id.packing_group_child_count))
                .setText("(" + getUncheckedChildCount(groupIndex) + ")");
        return view;
    }


    @SuppressLint("InflateParams")
    @Override
    public View getChildView(int groupIndex, int itemIndex, boolean b, View view, ViewGroup viewGroup) {
        final Itinerary.PackingItem item = getChild(groupIndex, itemIndex);
        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert layoutInflater != null;
            view = layoutInflater.inflate(R.layout.item_packing_item, null);
        }
        ((TextView) view.findViewById(R.id.packing_item_name)).setText(item.getName());

        CheckBox checkBox = view.findViewById(R.id.packing_item_check);
        checkBox.setOnCheckedChangeListener(null);
        checkBox.setChecked(item.isChecked());
        checkBox.setOnCheckedChangeListener((compoundButton, b1) -> {
            item.setChecked(b1);
            notifyDataSetChanged();
        });
        return view;
    }


    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }


    private long getUncheckedChildCount(int groupIndex) {
        List<Itinerary.PackingItem> children = listItem.get(listGroup.get(groupIndex));
        if (children != null) {
            long size = 0;
            for (Itinerary.PackingItem item : children)
                if (!item.isChecked())
                    size++;
            return size;
        }
        return 0;
    }


    public void removeItem(boolean isGroup, int groupIndex, int itemIndex) {
        if(isGroup) {
            listItem.remove(listGroup.get(groupIndex));
            listGroup.remove(groupIndex);
        } else {
            List<Itinerary.PackingItem> children = listItem.get(listGroup.get(groupIndex));
            if (children != null)
                children.remove(itemIndex);
        }
        notifyDataSetChanged();
    }


    public List<Day.PackingCategory> getList() {
        return listGroup;
    }


    public List<String> getGroupNamesList() {
        List<String> groupsNames = new ArrayList<>();
        for(Day.PackingCategory category : getList())
            groupsNames.add(category.getName());
        return groupsNames;
    }


    public void addGroup(Day.PackingCategory newCategory) {
        listGroup.add(newCategory);
        listItem.put(newCategory, new ArrayList<>());
        notifyDataSetChanged();
    }


    public void addItem(Day.PackingCategory group, Itinerary.PackingItem item) {
        int groupIndex = getGroupNamesList().indexOf(group.getName());
        List<Itinerary.PackingItem> items = listItem.get(listGroup.get(groupIndex));
        if (items != null)
            items.add(item);
        notifyDataSetChanged();
    }

}
