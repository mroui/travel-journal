package com.martynaroj.traveljournal.view.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.DialogCustomBinding;
import com.martynaroj.traveljournal.databinding.FragmentEndTravelBinding;
import com.martynaroj.traveljournal.services.models.Address;
import com.martynaroj.traveljournal.services.models.Day;
import com.martynaroj.traveljournal.services.models.Travel;
import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.view.interfaces.IOnBackPressed;
import com.martynaroj.traveljournal.view.others.classes.DialogHandler;
import com.martynaroj.traveljournal.view.others.classes.PDFCreator;
import com.martynaroj.traveljournal.view.others.classes.RequestPermissionsHandler;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;
import com.martynaroj.traveljournal.viewmodels.UserViewModel;

import java.io.Serializable;
import java.util.List;

public class EndTravelFragment extends BaseFragment implements View.OnClickListener, IOnBackPressed {

    private FragmentEndTravelBinding binding;
    private UserViewModel userViewModel;

    private User user;
    private Travel travel;
    private Address destination;
    private List<Day> days;


    public static EndTravelFragment newInstance(User user, Travel travel, Address destination, List<Day> days) {
        EndTravelFragment fragment = new EndTravelFragment();
        Bundle args = new Bundle();
        args.putSerializable(Constants.BUNDLE_USER, user);
        args.putSerializable(Constants.BUNDLE_TRAVEL, travel);
        args.putSerializable(Constants.BUNDLE_DESTINATION, destination);
        args.putSerializable(Constants.BUNDLE_DAYS, (Serializable) days);
        fragment.setArguments(args);
        return fragment;
    }


    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user = (User) getArguments().getSerializable(Constants.BUNDLE_USER);
            travel = (Travel) getArguments().getSerializable(Constants.BUNDLE_TRAVEL);
            destination = (Address) getArguments().getSerializable(Constants.BUNDLE_DESTINATION);
            days = (List<Day>) getArguments().getSerializable(Constants.BUNDLE_DAYS);
        }
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEndTravelBinding.inflate(inflater, container, false);
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
        }
    }


    private void initContentData() {
        binding.endTravelPrivacySpinner.setItems(Constants.PUBLIC, Constants.FRIENDS, Constants.ONLY_ME);
    }


    private void observeUserChanges() {
        userViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            this.user = user;
            if (user == null) {
                showSnackBar(binding.getRoot(), getResources().getString(R.string.messages_not_logged_user),
                        Snackbar.LENGTH_LONG);
                back();
            }
        });
    }


    //LISTENERS-------------------------------------------------------------------------------------


    private void setListeners() {
        binding.endTravelArrowButton.setOnClickListener(this);
        binding.endTravelFinishButton.setOnClickListener(this);
        setOnInputTouchListener();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.end_travel_arrow_button:
                showBackDialog();
                break;
            case R.id.end_travel_finish_button:
                finishTravel();
                break;
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    private void setOnInputTouchListener() {
        binding.endTravelDescriptionInput.setOnTouchListener((v, event) -> {
            v.getParent().requestDisallowInterceptTouchEvent(true);
            if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
                v.getParent().requestDisallowInterceptTouchEvent(false);
            }
            return false;
        });
    }


    //DIALOGS---------------------------------------------------------------------------------------


    private void showBackDialog() {
        if (getContext() != null) {
            Dialog dialog = DialogHandler.createDialog(getContext(), true);
            DialogCustomBinding binding = DialogCustomBinding.inflate(LayoutInflater.from(getContext()));
            dialog.setContentView(binding.getRoot());
            DialogHandler.initContent(getContext(), binding.dialogCustomTitle, R.string.dialog_unsaved_changes_title,
                    binding.dialogCustomDesc, R.string.dialog_unsaved_changes_desc,
                    binding.dialogCustomButtonPositive, R.string.dialog_button_yes,
                    binding.dialogCustomButtonNegative, R.string.dialog_button_no,
                    R.color.main_blue, R.color.blue_bg_lighter);
            binding.dialogCustomButtonPositive.setOnClickListener(v -> {
                dialog.dismiss();
                back();
            });
            binding.dialogCustomButtonNegative.setOnClickListener(v -> dialog.dismiss());
            dialog.show();
        }
    }


    //FINISH----------------------------------------------------------------------------------------


    private void finishTravel() {
        if (readWritePermissionsGranted()) {
            setTravelDescription();
            createPDF();
        }
    }


    private boolean readWritePermissionsGranted() {
        if(RequestPermissionsHandler.isWriteStorageGranted(getContext())) {
            if (RequestPermissionsHandler.isReadStorageGranted(getContext()))
                return true;
            else {
                RequestPermissionsHandler.requestReadStorage(getActivity());
                return false;
            }
        } else {
            RequestPermissionsHandler.requestWriteStorage(getActivity());
            return false;
        }
    }


    private void setTravelDescription() {
        String desc = binding.endTravelDescriptionInput.getText() != null
                ? binding.endTravelDescriptionInput.getText().toString() : "";
        travel.setDescription(desc);
    }


    private void createPDF() {
        if (getContext() != null) {
            PDFCreator pdfCreator = new PDFCreator(getContext(), user, travel, destination, days);
            pdfCreator.init();
            showSnackBar(binding.getRoot(), pdfCreator.tryToSave(), Snackbar.LENGTH_SHORT);
            pdfCreator.openFile();
        }
    }


    //OTHERS----------------------------------------------------------------------------------------


    private void showSnackBar(View view, String message, int duration) {
        getSnackBarInteractions().showSnackBar(view, getActivity(), message, duration);
    }


    private void back() {
        if (getParentFragmentManager().getBackStackEntryCount() > 0)
            getParentFragmentManager().popBackStack();
    }


    @Override
    public boolean onBackPressed() {
        showBackDialog();
        return true;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
