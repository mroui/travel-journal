package com.martynaroj.traveljournal.view.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.martynaroj.traveljournal.databinding.ItemTravelBinding;
import com.martynaroj.traveljournal.services.models.Itinerary;
import com.martynaroj.traveljournal.services.models.Travel;
import com.martynaroj.traveljournal.view.interfaces.OnItemClickListener;
import com.martynaroj.traveljournal.view.interfaces.OnItemLongClickListener;

import java.util.List;

public class TravelAdapter extends RecyclerView.Adapter<TravelAdapter.TravelHolder> {

    private Context context;
    private List<Itinerary> itineraries;
    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;


    public TravelAdapter(Context context, List<Itinerary> itineraries) {
        this.context = context;
        this.itineraries = itineraries;
    }


    @NonNull
    @Override
    public TravelHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTravelBinding binding = ItemTravelBinding.inflate(LayoutInflater.from(context), parent, false);
        return new TravelHolder(binding);
    }


    private Itinerary getItem(int position) {
        return this.itineraries.get(position);
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull TravelHolder holder, int position) {
        final Itinerary itinerary = itineraries.get(position);
        holder.binding.travelItemName.setText(itinerary.getName());
        holder.binding.travelItemAddress.setText(itinerary.getDestination().replace("&", ", "));
        holder.binding.travelItemDays.setText(Travel.whatDay(itinerary.getDatetimeFrom(), itinerary.getDatetimeTo()) + " days");
        holder.binding.travelItemDate.setText(itinerary.getDateString(itinerary.getCreatedDate()));
        holder.binding.travelItemPopularity.setText(itinerary.getPopularity()+"");
        holder.binding.travelItem.setOnClickListener(view -> {
            onItemClickListener.onItemClick(itinerary, position, view);
        });
        holder.binding.travelItem.setOnLongClickListener(view -> {
            onItemLongClickListener.onItemLongClick(itinerary, position, view);
            return true;
        });
    }


    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }


    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }


    public void changeList(List<Itinerary> itineraries) {
        if (itineraries != null) {
            this.itineraries = itineraries;
            notifyItemRangeChanged(0, this.itineraries.size());
            notifyDataSetChanged();
        }
    }


    public List<Itinerary> getList() {
        return itineraries;
    }


    public void remove(int position) {
        itineraries.remove(position);
        notifyItemRemoved(position);
        notifyDataSetChanged();
    }


    public void edit(Itinerary itinerary, int position) {
        itineraries.set(position, itinerary);
        notifyItemChanged(position);
        notifyItemRangeChanged(position, itineraries.size());
    }


    @Override
    public int getItemCount() {
        return itineraries.size();
    }


    static class TravelHolder extends RecyclerView.ViewHolder {
        private ItemTravelBinding binding;

        TravelHolder(ItemTravelBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}