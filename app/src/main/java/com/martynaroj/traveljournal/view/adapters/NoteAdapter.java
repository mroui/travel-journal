package com.martynaroj.traveljournal.view.adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.martynaroj.traveljournal.databinding.ItemNoteBinding;
import com.martynaroj.traveljournal.services.models.Note;
import com.martynaroj.traveljournal.view.interfaces.OnItemLongClickListener;

import java.util.ArrayList;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteHolder> {

    private Context context;
    private List<Note> notes;
    private OnItemLongClickListener listener;


    public NoteAdapter(Context context, List<Note> notes) {
        this.context = context;
        this.notes = notes;
    }


    @NonNull
    @Override
    public NoteAdapter.NoteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNoteBinding binding = ItemNoteBinding.inflate(LayoutInflater.from(context), parent, false);
        return new NoteAdapter.NoteHolder(binding);
    }


    private Note getItem(int position) {
        return notes.get(position);
    }


    @Override
    public void onBindViewHolder(@NonNull NoteAdapter.NoteHolder holder, int position) {
        final Note note = getItem(position);
        holder.binding.noteItemDate.setText(note.getDateTimeString());
        holder.binding.noteItemDesc.setText(note.getDescription());
        holder.binding.noteItem.setOnLongClickListener(view -> {
            listener.onItemLongClick(note, position, view);
            return true;
        });
    }


    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.listener = onItemLongClickListener;
    }


    public void add(Note note) {
        notes.add(0, note);
        notifyItemInserted(0);
        notifyItemRangeChanged(0, notes.size());
    }


    public void remove(int position) {
        notes.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, notes.size());
    }


    public void edit(int position, Note note) {
        notes.set(position, note);
        notifyItemChanged(position);
        notifyItemRangeChanged(position, notes.size());
    }


    public List<Note> getList() {
        return notes;
    }


    public List<Note> getTodayList() {
        List<Note> todayList = new ArrayList<>();
        for (Note e : notes)
            if (DateUtils.isToday(e.getDate()))
                todayList.add(e);
        return todayList;
    }


    @Override
    public int getItemCount() {
        return notes.size();
    }


    static class NoteHolder extends RecyclerView.ViewHolder {
        private ItemNoteBinding binding;

        NoteHolder(ItemNoteBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}