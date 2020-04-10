package com.martynaroj.traveljournal.view.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.FragmentNotesBinding;
import com.martynaroj.traveljournal.services.models.Day;
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;
import com.martynaroj.traveljournal.viewmodels.DayViewModel;
import com.martynaroj.traveljournal.viewmodels.UserViewModel;

public class NotesFragment extends BaseFragment implements View.OnClickListener {

    private FragmentNotesBinding binding;
    private UserViewModel userViewModel;
    private DayViewModel dayViewModel;

    private Day today;


    public static NotesFragment newInstance(Day day) {
        NotesFragment fragment = new NotesFragment();
        Bundle args = new Bundle();
        args.putSerializable(Constants.BUNDLE_DAY, day);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
            today = (Day) getArguments().getSerializable(Constants.BUNDLE_DAY);
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
        initListAdapter();
        binding.setIsListEmpty(true);
    }


    private void initListAdapter() {
        //
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
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.notes_arrow_button:
                back();
                break;
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
