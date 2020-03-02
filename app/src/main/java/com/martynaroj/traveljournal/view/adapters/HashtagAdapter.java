package com.martynaroj.traveljournal.view.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.ItemHashtagBinding;

import java.util.ArrayList;
import java.util.List;

public class HashtagAdapter extends ArrayAdapter<String> {

    private List<String> hashtags;

    public HashtagAdapter(@NonNull Context context, @NonNull List<String> tags) {
        super(context, 0, tags);
        hashtags = new ArrayList<>(tags);
    }


    @NonNull
    @Override
    public Filter getFilter() {
        return hashtagFilter;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = ItemHashtagBinding.inflate(LayoutInflater.from(getContext()), parent, false).getRoot();
        }
        String item = getItem(position);
        if (item != null) {
            ((TextView) convertView.findViewById(R.id.list_item_name)).setText(item);
        }
        return convertView;
    }


    private final Filter hashtagFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            List<String> suggestions = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                suggestions.addAll(hashtags);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (String item : hashtags) {
                    if (item.toLowerCase().contains(filterPattern)) {
                        suggestions.add(item);
                    }
                }
            }
            results.values = suggestions;
            results.count = suggestions.size();
            return results;
        }


        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            clear();
            addAll((List) results.values);
            notifyDataSetChanged();
        }


        @Override
        public CharSequence convertResultToString(Object resultValue) {
            return "#" + resultValue;
        }

    };
}