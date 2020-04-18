package com.martynaroj.traveljournal.view.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.martynaroj.traveljournal.BuildConfig;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.DialogCustomBinding;
import com.martynaroj.traveljournal.databinding.FragmentEndTravelBinding;
import com.martynaroj.traveljournal.services.models.Address;
import com.martynaroj.traveljournal.services.models.Day;
import com.martynaroj.traveljournal.services.models.Itinerary;
import com.martynaroj.traveljournal.services.models.Travel;
import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.view.interfaces.IOnBackPressed;
import com.martynaroj.traveljournal.view.others.classes.DialogHandler;
import com.martynaroj.traveljournal.view.others.classes.RequestPermissionsHandler;
import com.martynaroj.traveljournal.view.others.enums.Status;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;
import com.martynaroj.traveljournal.viewmodels.ItineraryViewModel;
import com.martynaroj.traveljournal.viewmodels.PdfCreatorViewModel;
import com.martynaroj.traveljournal.viewmodels.StorageViewModel;
import com.martynaroj.traveljournal.viewmodels.TravelViewModel;
import com.martynaroj.traveljournal.viewmodels.UserViewModel;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class EndTravelFragment extends BaseFragment implements View.OnClickListener, IOnBackPressed {

    private FragmentEndTravelBinding binding;
    private UserViewModel userViewModel;
    private PdfCreatorViewModel pdfCreatorViewModel;
    private StorageViewModel storageViewModel;
    private ItineraryViewModel itineraryViewModel;
    private TravelViewModel travelViewModel;

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
        observeTravelChanges();

        return view;
    }


    //INIT DATA-------------------------------------------------------------------------------------


    private void initViewModels() {
        if (getActivity() != null) {
            userViewModel = new ViewModelProvider(getActivity()).get(UserViewModel.class);
            pdfCreatorViewModel = new ViewModelProvider(getActivity()).get(PdfCreatorViewModel.class);
            storageViewModel = new ViewModelProvider(getActivity()).get(StorageViewModel.class);
            itineraryViewModel = new ViewModelProvider(getActivity()).get(ItineraryViewModel.class);
            travelViewModel = new ViewModelProvider(getActivity()).get(TravelViewModel.class);
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


    private void observeTravelChanges() {
        travelViewModel.getTravel().observe(getViewLifecycleOwner(), travel -> {
            this.travel = travel;
            if (travel == null)
                back();
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
        if (RequestPermissionsHandler.isWriteStorageGranted(getContext())) {
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
            startProgressBar();
            File file = createFile();
            pdfCreatorViewModel.createPDF(file, user, travel, destination, days);
            pdfCreatorViewModel.getStatus().observe(getViewLifecycleOwner(), status -> {
                if (status != null) {
                    if (!status.contains(getResources().getString(R.string.messages_error))) {
                        openFile(file);
                        showSnackBar(binding.getRoot(), status, Snackbar.LENGTH_SHORT);
                        saveFileToStorage(file);
                    } else {
                        showSnackBar(binding.getRoot(), status, Snackbar.LENGTH_LONG);
                        stopProgressBar();
                    }
                }
            });
        }
    }


    private void saveFileToStorage(File file) {
        String path = user.getUid() + "/" + Constants.STORAGE_TRAVELS + "/" + travel.getId();
        storageViewModel.saveFileToStorage(getFileUri(file), file.getName(), path);
        storageViewModel.getStorageStatus().observe(getViewLifecycleOwner(), status -> {
            if (status != null && !status.contains(getResources().getString(R.string.messages_error))) {
                createItinerary();
            } else {
                showSnackBar(binding.getRoot(), status, Snackbar.LENGTH_LONG);
                stopProgressBar();
            }
        });
    }


    private void createItinerary() {
        
    }


    //FILE------------------------------------------------------------------------------------------


    private File createFile() {
        return new File(Objects.requireNonNull(getContext()).getExternalFilesDir(
                Environment.DIRECTORY_DOWNLOADS),
                getFilenameFormat(travel.getName()) + Constants.PDF_EXT
        );
    }


    private void openFile(File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(getFileUri(file), "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        if (getContext() != null && intent.resolveActivity(getContext().getPackageManager()) != null)
            getContext().startActivity(intent);
    }


    private String getFilenameFormat(String text) {
        return text.replaceAll("\\s+", "_").replaceAll("[{}$&+,:;=\\\\?@#|/'<>.^*()%!-]", "");
    }


    private Uri getFileUri(File file) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M && getContext() != null)
            return FileProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID + ".provider", file);
        else
            return Uri.fromFile(file);
    }


    //OTHERS----------------------------------------------------------------------------------------


    private void showSnackBar(View view, String message, int duration) {
        getSnackBarInteractions().showSnackBar(view, getActivity(), message, duration);
    }


    private void startProgressBar() {
        getProgressBarInteractions().startProgressBar(binding.getRoot(), binding.endTravelProgressbarLayout,
                binding.endTravelProgressbar);
    }


    private void stopProgressBar() {
        getProgressBarInteractions().stopProgressBar(binding.getRoot(), binding.endTravelProgressbarLayout,
                binding.endTravelProgressbar);
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
