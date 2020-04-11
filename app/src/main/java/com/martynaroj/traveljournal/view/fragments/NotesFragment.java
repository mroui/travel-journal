package com.martynaroj.traveljournal.view.fragments;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.DialogAddNoteBinding;
import com.martynaroj.traveljournal.databinding.DialogCustomBinding;
import com.martynaroj.traveljournal.databinding.DialogEditNoteBinding;
import com.martynaroj.traveljournal.databinding.DialogNotesOptionsBinding;
import com.martynaroj.traveljournal.databinding.FragmentNotesBinding;
import com.martynaroj.traveljournal.services.models.Day;
import com.martynaroj.traveljournal.services.models.Note;
import com.martynaroj.traveljournal.view.adapters.NoteAdapter;
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.view.others.classes.FormHandler;
import com.martynaroj.traveljournal.view.others.classes.RippleDrawable;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;
import com.martynaroj.traveljournal.viewmodels.DayViewModel;
import com.martynaroj.traveljournal.viewmodels.UserViewModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class NotesFragment extends BaseFragment implements View.OnClickListener {

    private FragmentNotesBinding binding;
    private UserViewModel userViewModel;
    private DayViewModel dayViewModel;

    private Day today;
    private List<Day> days;
    private List<Note> notes;

    private NoteAdapter adapter;


    public static NotesFragment newInstance(Day day, List<Day> days) {
        NotesFragment fragment = new NotesFragment();
        Bundle args = new Bundle();
        args.putSerializable(Constants.BUNDLE_DAY, day);
        args.putSerializable(Constants.BUNDLE_DAYS, (Serializable) days);
        fragment.setArguments(args);
        return fragment;
    }


    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            today = (Day) getArguments().getSerializable(Constants.BUNDLE_DAY);
            days = (List<Day>) getArguments().getSerializable(Constants.BUNDLE_DAYS);
        }
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNotesBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initViewModels();
        initContentData();
        setListeners();
        observeUserChanges();

        return view;
    }


    //INIT DATA-------------------------------------------------------------------------------------


    private void initViewModels() {
        if (getActivity() != null) {
            userViewModel = new ViewModelProvider(getActivity()).get(UserViewModel.class);
            dayViewModel = new ViewModelProvider(getActivity()).get(DayViewModel.class);
        }
    }


    private void initContentData() {
        notes = getAllDaysNotesList();
        initListAdapter();
        setBindingData();
    }


    private void initListAdapter() {
        if (getContext() != null) {
            adapter = new NoteAdapter(getContext(), notes);
            binding.notesListRecyclerView.setAdapter(adapter);
            setOnItemClickListener();
        }
    }


    private void setBindingData() {
        binding.setIsListEmpty(notes.size() == 0);
    }


    private void observeUserChanges() {
        userViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user == null) {
                showSnackBar(getResources().getString(R.string.messages_not_logged_user), Snackbar.LENGTH_LONG);
                back();
            }
        });
    }


    //LISTENERS-------------------------------------------------------------------------------------


    private void setListeners() {
        binding.notesArrowButton.setOnClickListener(this);
        binding.notesAddFloatingButton.setOnClickListener(this);
        setOnListScrollListener();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.notes_arrow_button:
                back();
                break;
            case R.id.notes_add_floating_button:
                showAddNoteDialog();
                break;
        }
    }


    private void setOnListScrollListener() {
        binding.notesListRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE)
                    binding.notesAddFloatingButton.show();
                else
                    binding.notesAddFloatingButton.hide();
            }
        });
    }


    private void setOnItemClickListener() {
        adapter.setOnItemLongClickListener((object, position, view) -> showOptionsDialog((Note) object, position));
    }


    //NOTES / LIST----------------------------------------------------------------------------------


    private List<Note> getAllDaysNotesList() {
        List<Note> list = new ArrayList<>();
        for (Day day : days)
            list.addAll(day.getNotes());
        Collections.sort(list);
        Collections.reverse(list);
        return list;
    }


    private void addNote(String text) {
        adapter.add(new Note(text));
        notes = adapter.getList();
        dayViewModel.updateDay(today.getId(), new HashMap<String, Object>() {{
            put(Constants.DB_NOTES, adapter.getTodayList());
        }});
        today.setNotes(adapter.getTodayList());
        dayViewModel.setToday(today);
        setBindingData();
    }


    private void removeNote(Note note, int noteIndex) {
        adapter.remove(noteIndex);
        notes = adapter.getList();
        Integer dayIndex = getDayIndexOfNote(note);
        if (dayIndex != null) {
            days.get(dayIndex).getNotes().remove(note);
            updateDay(dayIndex);
        }
    }


    private void editNote(Note note, int noteIndex) {
        adapter.edit(noteIndex, note);
        notes = adapter.getList();
        Integer dayIndex = getDayIndexOfNote(note);
        if (dayIndex != null) {
            days.get(dayIndex).getNotes().set(days.get(dayIndex).getNotes().indexOf(note), note);
            updateDay(dayIndex);
        }
    }


    private void updateDay(int index) {
        dayViewModel.updateDay(days.get(index).getId(), new HashMap<String, Object>() {{
            put(Constants.DB_NOTES, days.get(index).getNotes());
        }});
        dayViewModel.setDays(days);
        setBindingData();
    }


    private Integer getDayIndexOfNote(Note note) {
        Calendar cNote = Calendar.getInstance(), cDay = Calendar.getInstance();
        cNote.setTimeInMillis(note.getDate());
        for (int i = 0; i < days.size(); i++) {
            cDay.setTimeInMillis(days.get(i).getDate());
            if (cNote.get(Calendar.DAY_OF_YEAR) == cDay.get(Calendar.DAY_OF_YEAR) &&
                    cNote.get(Calendar.YEAR) == cDay.get(Calendar.YEAR))
                return i;
        }
        return null;
    }


    //DIALOG----------------------------------------------------------------------------------------


    private void showOptionsDialog(Note note, int index) {
        if (getContext() != null) {
            Dialog dialog = new Dialog(getContext());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(true);
            DialogNotesOptionsBinding binding = DialogNotesOptionsBinding.inflate(LayoutInflater.from(getContext()));
            dialog.setContentView(binding.getRoot());
            binding.dialogOptionsEdit.setOnClickListener(view -> {
                showEditNoteDialog(note, index);
                dialog.dismiss();
            });
            binding.dialogOptionsRemove.setOnClickListener(view -> {
                showRemoveNoteDialog(note, index);
                dialog.dismiss();
            });
            dialog.show();
        }
    }


    private void showAddNoteDialog() {
        if (getContext() != null) {
            Dialog dialog = new Dialog(getContext());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(true);
            DialogAddNoteBinding binding = DialogAddNoteBinding.inflate(LayoutInflater.from(getContext()));
            dialog.setContentView(binding.getRoot());
            binding.dialogAddNoteButtonPositive.setOnClickListener(view -> {
                if (validateInput(binding.dialogAddNoteInput, binding.dialogAddNoteInputLayout)
                        && binding.dialogAddNoteInput.getText() != null) {
                    addNote(binding.dialogAddNoteInput.getText().toString());
                    dialog.dismiss();
                }
            });
            binding.dialogAddNoteButtonNegative.setOnClickListener(view -> dialog.dismiss());
            dialog.show();
        }
    }


    private void showRemoveNoteDialog(Note note, int index) {
        if (getContext() != null) {
            Dialog dialog = new Dialog(getContext());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(true);
            DialogCustomBinding binding = DialogCustomBinding.inflate(LayoutInflater.from(getContext()));
            dialog.setContentView(binding.getRoot());

            binding.dialogCustomTitle.setText(getResources().getString(R.string.dialog_remove_note_title));
            binding.dialogCustomDesc.setText(getResources().getString(R.string.dialog_remove_note_desc));
            binding.dialogCustomButtonPositive.setText(getResources().getString(R.string.dialog_button_yes));
            RippleDrawable.setRippleEffectButton(
                    binding.dialogCustomButtonPositive,
                    Color.TRANSPARENT,
                    getResources().getColor(R.color.red_bg_lighter)
            );
            binding.dialogCustomButtonPositive.setTextColor(getResources().getColor(R.color.main_red));
            binding.dialogCustomButtonPositive.setOnClickListener(v -> {
                removeNote(note, index);
                dialog.dismiss();
            });
            binding.dialogCustomButtonNegative.setText(getResources().getString(R.string.dialog_button_no));
            RippleDrawable.setRippleEffectButton(
                    binding.dialogCustomButtonNegative,
                    Color.TRANSPARENT,
                    getResources().getColor(R.color.red_bg_lighter)
            );
            binding.dialogCustomButtonNegative.setTextColor(getResources().getColor(R.color.main_red));
            binding.dialogCustomButtonNegative.setOnClickListener(v -> dialog.dismiss());

            dialog.show();
        }
    }


    private void showEditNoteDialog(Note note, int index) {
        if (getContext() != null) {
            Dialog dialog = new Dialog(getContext());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(true);
            DialogEditNoteBinding binding = DialogEditNoteBinding.inflate(LayoutInflater.from(getContext()));
            dialog.setContentView(binding.getRoot());
            binding.dialogEditNoteInput.setText(note.getDescription());
            binding.dialogEditNoteInput.setSelection(note.getDescription().length());
            binding.dialogEditNoteButtonPositive.setOnClickListener(view -> {
                if (validateInput(binding.dialogEditNoteInput, binding.dialogEditNoteInputLayout)
                        && binding.dialogEditNoteInput.getText() != null) {
                    note.setDescription(binding.dialogEditNoteInput.getText().toString());
                    editNote(note, index);
                    dialog.dismiss();
                }
            });
            binding.dialogEditNoteButtonNegative.setOnClickListener(view -> dialog.dismiss());
            dialog.show();
        }
    }


    //OTHERS----------------------------------------------------------------------------------------


    private boolean validateInput(TextInputEditText input, TextInputLayout layout) {
        return new FormHandler(getContext()).validateInput(input, layout);
    }


    private void back() {
        if (getParentFragmentManager().getBackStackEntryCount() > 0)
            getParentFragmentManager().popBackStack();
    }


    private void showSnackBar(String message, int duration) {
        getSnackBarInteractions().showSnackBar(binding.getRoot(), getActivity(), message, duration);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
