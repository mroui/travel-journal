package com.martynaroj.traveljournal.view.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.DialogNotesOptionsBinding;
import com.martynaroj.traveljournal.databinding.FragmentNotesBinding;
import com.martynaroj.traveljournal.services.models.Day;
import com.martynaroj.traveljournal.services.models.Note;
import com.martynaroj.traveljournal.view.adapters.NoteAdapter;
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;
import com.martynaroj.traveljournal.viewmodels.DayViewModel;
import com.martynaroj.traveljournal.viewmodels.UserViewModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
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
        binding.setIsListEmpty(notes.size() == 0);
    }


    private void initListAdapter() {
        if (getContext() != null) {
            adapter = new NoteAdapter(getContext(), notes);
            binding.notesListRecyclerView.setAdapter(adapter);
            setOnItemClickListener();
        }
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
                //todo: add
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
        adapter.setOnItemLongClickListener((object, position, view) -> showOptionsDialog((Note) object));
    }


    //LIST------------------------------------------------------------------------------------------


    private List<Note> getAllDaysNotesList() {
        List<Note> list = new ArrayList<>();
        for (Day day : days)
            list.addAll(day.getNotes());
        Collections.sort(list);
        Collections.reverse(list);
        return list;
    }


    //DIALOG----------------------------------------------------------------------------------------


    private void showOptionsDialog(Note note) {
        if (getContext() != null) {
            Dialog dialog = new Dialog(getContext());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(true);
            DialogNotesOptionsBinding binding = DialogNotesOptionsBinding.inflate(LayoutInflater.from(getContext()));
            dialog.setContentView(binding.getRoot());
            binding.dialogOptionsEdit.setOnClickListener(view -> {
                //todo: edit
            });
            binding.dialogOptionsRemove.setOnClickListener(view -> {
                //todo: remove
            });
            dialog.show();
        }
    }


    //OTHERS----------------------------------------------------------------------------------------


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
