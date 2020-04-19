package com.martynaroj.traveljournal.view.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.DialogAddNoteBinding;
import com.martynaroj.traveljournal.databinding.DialogCustomBinding;
import com.martynaroj.traveljournal.databinding.DialogEditNoteBinding;
import com.martynaroj.traveljournal.databinding.DialogOptionsBinding;
import com.martynaroj.traveljournal.databinding.FragmentNotesBinding;
import com.martynaroj.traveljournal.services.models.Day;
import com.martynaroj.traveljournal.services.models.Note;
import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.view.adapters.NoteAdapter;
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.view.others.classes.DialogHandler;
import com.martynaroj.traveljournal.view.others.classes.FormHandler;
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
    User user;
    DayViewModel dayViewModel;
    Day today;
    List<Day> days;

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
        observeUserChanges(view);

        return view;
    }


    //INIT DATA-------------------------------------------------------------------------------------


    void initViewModels() {
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
            setOnItemLongClickListener();
        }
    }


    private void setBindingData() {
        binding.setIsListEmpty(notes.size() == 0);
    }


    void observeUserChanges(View view) {
        userViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            this.user = user;
            if (user == null) {
                showSnackBar(view, getResources().getString(R.string.messages_not_logged_user),
                        Snackbar.LENGTH_LONG);
                back();
            }
        });
    }


    //LISTENERS-------------------------------------------------------------------------------------


    private void setListeners() {
        binding.notesArrowButton.setOnClickListener(this);
        binding.notesAddFloatingButton.setOnClickListener(this);
        setOnListScrollListener(binding.notesListRecyclerView, binding.notesAddFloatingButton);
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


    void setOnListScrollListener(RecyclerView recyclerView, FloatingActionButton button) {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE)
                    button.show();
                else
                    button.hide();
            }
        });
    }


    private void setOnItemLongClickListener() {
        adapter.setOnItemLongClickListener((object, position, view) -> showOptionsDialog((Note) object, position));
    }


    //NOTES / LIST----------------------------------------------------------------------------------


    private List<Note> getAllDaysNotesList() {
        List<Note> list = new ArrayList<>();
        if (days != null) {
            for (Day day : days)
                list.addAll(day.getNotes());
            Collections.sort(list);
            Collections.reverse(list);
        }
        return list;
    }


    private void addNote(String text) {
        adapter.add(new Note(text));
        notes = adapter.getList();
        binding.notesListRecyclerView.scrollToPosition(0);
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


    Integer getDayIndexOfNote(Note note) {
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
            Dialog dialog = DialogHandler.createDialog(getContext(), true);
            DialogOptionsBinding binding = DialogOptionsBinding.inflate(LayoutInflater.from(getContext()));
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
            Dialog dialog = DialogHandler.createDialog(getContext(), true);
            DialogAddNoteBinding binding = DialogAddNoteBinding.inflate(LayoutInflater.from(getContext()));
            dialog.setContentView(binding.getRoot());
            binding.dialogAddNotePhotoContainer.setVisibility(View.GONE);
            binding.dialogAddNotePlaceContainer.setVisibility(View.GONE);
            binding.dialogAddNoteButtonPositive.setOnClickListener(view -> {
                if (validateInput(binding.dialogAddNoteInput, binding.dialogAddNoteInputLayout)
                        && binding.dialogAddNoteInput.getText() != null) {
                    addNote(binding.dialogAddNoteInput.getText().toString());
                    dismissAddDialog(dialog);
                }
            });
            binding.dialogAddNoteButtonNegative.setOnClickListener(view -> dismissAddDialog(dialog));
            dialog.setOnDismissListener(dialogInterface -> removeAutoCompleteFragment());
            dialog.setOnCancelListener(dialogInterface -> removeAutoCompleteFragment());
            dialog.show();
        }
    }


    private void showRemoveNoteDialog(Note note, int index) {
        if (getContext() != null) {
            Dialog dialog = DialogHandler.createDialog(getContext(), true);
            DialogCustomBinding binding = DialogCustomBinding.inflate(LayoutInflater.from(getContext()));
            dialog.setContentView(binding.getRoot());
            DialogHandler.initContent(
                    getContext(), binding.dialogCustomTitle, R.string.dialog_remove_note_title,
                    binding.dialogCustomDesc, R.string.dialog_remove_note_desc,
                    binding.dialogCustomButtonPositive, R.string.dialog_button_yes,
                    binding.dialogCustomButtonNegative, R.string.dialog_button_no,
                    R.color.main_red, R.color.red_bg_lighter
            );
            binding.dialogCustomButtonPositive.setOnClickListener(v -> {
                removeNote(note, index);
                dialog.dismiss();
            });
            binding.dialogCustomButtonNegative.setOnClickListener(v -> dialog.dismiss());
            dialog.show();
        }
    }


    private void showEditNoteDialog(Note note, int index) {
        if (getContext() != null) {
            Dialog dialog = DialogHandler.createDialog(getContext(), true);
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


    void dismissAddDialog(Dialog dialog) {
        removeAutoCompleteFragment();
        dialog.dismiss();
    }


    //OTHERS----------------------------------------------------------------------------------------


    boolean validateInput(TextInputEditText input, TextInputLayout layout) {
        return new FormHandler(getContext()).validateInput(input, layout);
    }


    void back() {
        if (getParentFragmentManager().getBackStackEntryCount() > 0)
            getParentFragmentManager().popBackStack();
    }


    void showSnackBar(View view, String message, int duration) {
        getSnackBarInteractions().showSnackBar(view, getActivity(), message, duration);
    }


    void removeAutoCompleteFragment() {
        if (getActivity() != null) {
            Fragment fragment = (getActivity().getSupportFragmentManager()
                    .findFragmentById(R.id.dialog_add_note_place_search_fragment));
            if (fragment != null)
                getActivity().getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
