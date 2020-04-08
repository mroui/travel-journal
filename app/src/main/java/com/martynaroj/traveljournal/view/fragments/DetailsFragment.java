package com.martynaroj.traveljournal.view.fragments;

import android.Manifest;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.hootsuite.nachos.NachoTextView;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.DialogEditTravelDetailsBinding;
import com.martynaroj.traveljournal.databinding.FragmentDetailsBinding;
import com.martynaroj.traveljournal.services.models.Address;
import com.martynaroj.traveljournal.services.models.Reservation;
import com.martynaroj.traveljournal.services.models.Travel;
import com.martynaroj.traveljournal.view.adapters.HashtagAdapter;
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.view.others.classes.FileUriUtils;
import com.martynaroj.traveljournal.view.others.classes.FormHandler;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;
import com.martynaroj.traveljournal.viewmodels.ReservationViewModel;
import com.martynaroj.traveljournal.viewmodels.UserViewModel;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.app.Activity.RESULT_OK;
import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class DetailsFragment extends BaseFragment implements View.OnClickListener {

    private FragmentDetailsBinding binding;
    private UserViewModel userViewModel;
    private ReservationViewModel reservationViewModel;

    private Travel travel;
    private Address destination;
    private Reservation accommodation;
    private Reservation transport;

    private Dialog editDialog;
    private DialogEditTravelDetailsBinding dialogBinding;

    private Uri newImageUri;


    public static DetailsFragment newInstance(Travel travel, Address destination) {
        DetailsFragment fragment = new DetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable(Constants.BUNDLE_TRAVEL, travel);
        args.putSerializable(Constants.BUNDLE_DESTINATION, destination);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            travel = (Travel) getArguments().getSerializable(Constants.BUNDLE_TRAVEL);
            destination = (Address) getArguments().getSerializable(Constants.BUNDLE_DESTINATION);
        }
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDetailsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initViewModels();
        loadTravelDetails();
        setListeners();
        observeUserChanges();

        return view;
    }


    //INIT DATA-------------------------------------------------------------------------------------


    private void initViewModels() {
        if (getActivity() != null) {
            userViewModel = new ViewModelProvider(getActivity()).get(UserViewModel.class);
            reservationViewModel = new ViewModelProvider(getActivity()).get(ReservationViewModel.class);
        }
    }


    private void loadTravelDetails() {
        if (travel != null) {
            startProgressBar();
            reservationViewModel.getReservations(
                    new ArrayList<>(Arrays.asList(travel.getAccommodation(), travel.getTransport()))
            );
            reservationViewModel.getReservationsList().observe(getViewLifecycleOwner(), reservations -> {
                if (reservations != null && reservations.size() > 0) {
                    if (reservations.get(0).getId().equals(travel.getAccommodation())) {
                        accommodation = reservations.get(0);
                        transport = reservations.get(1);
                    } else {
                        accommodation = reservations.get(1);
                        transport = reservations.get(0);
                    }
                    initContentData();
                } else {
                    showSnackBar(getResources().getString(R.string.messages_error_failed_load_travel), Snackbar.LENGTH_LONG);
                    back();
                }
                stopProgressBar();
            });
        }
    }


    private void initContentData() {
        binding.setTravel(travel);
        binding.setDestination(destination);
        binding.setAccommodation(accommodation);
        binding.setTransport(transport);
        initTags();
    }


    private void initTags() {
        if (travel != null) {
            binding.detailsTagsView.setData(travel.getTags(), item -> {
                SpannableString spannableString = new SpannableString(item);
                spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#000000")),
                        0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                return spannableString;
            });
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
        binding.detailsArrowButton.setOnClickListener(this);
        binding.detailsAccommodationFileValue.setOnClickListener(this);
        binding.detailsTransportFileValue.setOnClickListener(this);
        binding.detailsTagsSeeAllButton.setOnClickListener(this);
        binding.detailsEditDetailsButton.setOnClickListener(this);
        binding.detailsEndButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.details_arrow_button:
                back();
                break;
            case R.id.details_accommodation_file_value:
                downloadFile(accommodation.getFile());
                break;
            case R.id.details_transport_file_value:
                downloadFile(transport.getFile());
                break;
            case R.id.details_tags_see_all_button:
                seeAllTags();
                break;
            case R.id.details_edit_details_button:
                showEditDetailsDialog();
                break;
            case R.id.dialog_edit_travel_details_image_button:
                checkPermissionsToSelectImage();
                break;
            case R.id.dialog_edit_travel_details_tags_text_input:
                dialogBinding.dialogEditTravelDetailsTagsError.setVisibility(View.GONE);
                break;
            case R.id.dialog_edit_travel_details_button_positive:
                validateEditDetails();
                break;
            case R.id.dialog_edit_travel_details_button_negative:
                editDialog.dismiss();
                break;
            case R.id.dialog_edit_travel_details_image_file_remove_button:
                removeImage();
                break;
            case R.id.details_end_button:
                endTravel();
                break;
        }
    }


    private void setEditDialogListeners() {
        dialogBinding.dialogEditTravelDetailsTagsTextInput.setOnClickListener(this);
        dialogBinding.dialogEditTravelDetailsTagsTextInput.setOnFocusChangeListener(
                (view, focus) -> dialogBinding.dialogEditTravelDetailsTagsError.setVisibility(View.GONE)
        );
        dialogBinding.dialogEditTravelDetailsImageButton.setOnClickListener(this);
        dialogBinding.dialogEditTravelDetailsImageFileRemoveButton.setOnClickListener(this);
        dialogBinding.dialogEditTravelDetailsButtonNegative.setOnClickListener(this);
        dialogBinding.dialogEditTravelDetailsButtonPositive.setOnClickListener(this);
    }


    //DETAILS---------------------------------------------------------------------------------------


    private void downloadFile(String url) {
        if (getContext() != null) {
            String file = readFilenameFromUrl(url);
            if (file != null) {
                DownloadManager downloadManager = (DownloadManager) getContext()
                        .getSystemService(Context.DOWNLOAD_SERVICE);
                Uri uri = Uri.parse(url);
                DownloadManager.Request request = new DownloadManager.Request(uri);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalFilesDir(getContext(), DIRECTORY_DOWNLOADS, file);
                if (downloadManager != null)
                    downloadManager.enqueue(request);
                else
                    showSnackBar(getResources().getString(R.string.messages_error_failed_download_file),
                            Snackbar.LENGTH_LONG);
            } else
                showSnackBar(getResources().getString(R.string.messages_error_failed_read_file),
                        Snackbar.LENGTH_LONG);
        }
    }


    private String readFilenameFromUrl(String url) {
        Matcher matcher = Pattern.compile("%2..*%2F(.*?)\\?alt").matcher(url);
        String file = null;
        if (matcher.find())
            file = matcher.group(1);
        return file;
    }


    private void seeAllTags() {
        ConstraintLayout.LayoutParams constraintLayout = (ConstraintLayout.LayoutParams)
                binding.detailsTagsView.getLayoutParams();
        String seeAllLessText;
        if (binding.detailsTagsView.getLayoutParams().height == ConstraintLayout.LayoutParams.WRAP_CONTENT) {
            constraintLayout.height = Constants.TAGS_VIEW_HEIGHT;
            seeAllLessText = getResources().getString(R.string.details_see_all_tags);
        } else {
            constraintLayout.height = ConstraintLayout.LayoutParams.WRAP_CONTENT;
            seeAllLessText = getResources().getString(R.string.details_see_less_tags);
        }
        binding.detailsTagsSeeAllButton.setPaintFlags(
                binding.detailsTagsSeeAllButton.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG
        );
        binding.detailsTagsSeeAllButton.setText(seeAllLessText);
        binding.detailsTagsView.setLayoutParams(constraintLayout);
    }


    //EDIT------------------------------------------------------------------------------------------


    private void showEditDetailsDialog() {
        if (getContext() != null && getActivity() != null) {
            editDialog = new Dialog(getContext());
            editDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            editDialog.setCancelable(false);
            editDialog.setContentView(R.layout.dialog_edit_travel_details);

            dialogBinding = DialogEditTravelDetailsBinding.inflate(LayoutInflater.from(getContext()));
            editDialog.setContentView(dialogBinding.getRoot());

            dialogBinding.dialogEditTravelDetailsNameTextInput.setText(travel.getName());
            loadTravelImage(travel.getImage(), readFilenameFromUrl(travel.getImage()));
            setTagsView();
            setEditDialogListeners();

            editDialog.show();
        }
    }


    private void loadTravelImage(String image, String fileName) {
        if (getContext() != null && image != null) {
            Glide.with(getContext())
                    .load(image)
                    .fitCenter()
                    .placeholder(R.drawable.no_image)
                    .centerCrop()
                    .into(dialogBinding.dialogEditTravelDetailsImageButton);
            dialogBinding.dialogEditTravelDetailsImageFileContainer.setVisibility(View.VISIBLE);
            dialogBinding.dialogEditTravelDetailsImageFileName.setText(fileName);
        } else
            dialogBinding.dialogEditTravelDetailsImageFileContainer.setVisibility(View.GONE);
    }


    private void setTagsView() {
        if (getContext() != null) {
            HashtagAdapter adapter = new HashtagAdapter(
                    getContext(),
                    new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.preferences)))
            );
            dialogBinding.dialogEditTravelDetailsTagsTextInput.setAdapter(adapter);
            dialogBinding.dialogEditTravelDetailsTagsTextInput.setThreshold(1);
            dialogBinding.dialogEditTravelDetailsTagsTextInput.setText(travel.getTags());
        }
    }


    private void validateEditDetails() {
        boolean isNameValid = new FormHandler(getContext()).validateLength(
                dialogBinding.dialogEditTravelDetailsNameTextInput,
                dialogBinding.dialogEditTravelDetailsNameTextLayout,
                8
        );
        dialogBinding.dialogEditTravelDetailsTagsTextInput.setText(
                getUniqueTags(dialogBinding.dialogEditTravelDetailsTagsTextInput)
        );
        boolean isTagsValid = dialogBinding.dialogEditTravelDetailsTagsTextInput.getChipValues().size() >= 3;

        if (isNameValid && isTagsValid) {
            //todo: update travel
            editDialog.dismiss();
        } else if (!isTagsValid) {
            dialogBinding.dialogEditTravelDetailsTagsError.setVisibility(View.VISIBLE);
            dialogBinding.dialogEditTravelDetailsContentContainer.fullScroll(View.FOCUS_DOWN);
        }
    }


    private List<String> getUniqueTags(NachoTextView tagsInput) {
        return new ArrayList<>(new LinkedHashSet<>(tagsInput.getChipValues()));
    }


    private void removeImage() {
        dialogBinding.dialogEditTravelDetailsImageFileContainer.setVisibility(View.GONE);
        dialogBinding.dialogEditTravelDetailsImageButton.setImageResource(R.drawable.no_image);
    }


    private void selectImage() {
        if (getActivity() != null && getContext() != null) {
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                    .setAspectRatio(3, 2)
                    .start(getContext(), this);
        }
    }


    private void checkPermissionsToSelectImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if (!isReadStoragePermissionsGranted())
                requestReadStoragePermissions();
            else
                selectImage();
        else
            selectImage();
    }


    private boolean isReadStoragePermissionsGranted() {
        if (getContext() != null)
            return ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        else
            return false;
    }


    private void requestReadStoragePermissions() {
        if (getActivity() != null) {
            ActivityCompat.requestPermissions(
                    getActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    Constants.RC_EXTERNAL_STORAGE_IMG
            );
        }
    }


    //END-------------------------------------------------------------------------------------------


    private void endTravel() {
        //todo: end travel, save to file, set sharing option, remove unnecessary data like days from
        //todo: databse, etc, add travel description
    }


    //OTHERS----------------------------------------------------------------------------------------


    private void back() {
        if (getParentFragmentManager().getBackStackEntryCount() > 0)
            getParentFragmentManager().popBackStack();
    }


    private void startProgressBar() {
        getProgressBarInteractions().startProgressBar(binding.getRoot(),
                binding.detailsProgressbarLayout, binding.detailsProgressbar);
    }


    private void stopProgressBar() {
        getProgressBarInteractions().stopProgressBar(binding.getRoot(),
                binding.detailsProgressbarLayout, binding.detailsProgressbar);
    }


    private void showSnackBar(String message, int duration) {
        getSnackBarInteractions().showSnackBar(binding.getRoot(), getActivity(), message, duration);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK && result != null) {
                newImageUri = result.getUri();
                loadTravelImage(newImageUri.toString(), FileUriUtils.getFileName(getContext(), newImageUri));
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE && result != null)
                showSnackBar(result.getError().getMessage(), Snackbar.LENGTH_LONG);
        } else
            showSnackBar(getResources().getString(R.string.messages_error_no_file_selected), Snackbar.LENGTH_SHORT);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        dialogBinding = null;
    }

}
