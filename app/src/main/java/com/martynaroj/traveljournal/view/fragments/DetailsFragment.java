package com.martynaroj.traveljournal.view.fragments;

import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.hootsuite.nachos.NachoTextView;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.DialogCustomBinding;
import com.martynaroj.traveljournal.databinding.DialogEditTravelDetailsBinding;
import com.martynaroj.traveljournal.databinding.FragmentDetailsBinding;
import com.martynaroj.traveljournal.services.models.Address;
import com.martynaroj.traveljournal.services.models.Day;
import com.martynaroj.traveljournal.services.models.Reservation;
import com.martynaroj.traveljournal.services.models.Travel;
import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.view.adapters.HashtagAdapter;
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.view.others.classes.DialogHandler;
import com.martynaroj.traveljournal.view.others.classes.FileCompressor;
import com.martynaroj.traveljournal.view.others.classes.FileUriUtils;
import com.martynaroj.traveljournal.view.others.classes.FormHandler;
import com.martynaroj.traveljournal.view.others.classes.RequestPermissionsHandler;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;
import com.martynaroj.traveljournal.viewmodels.ReservationViewModel;
import com.martynaroj.traveljournal.viewmodels.StorageViewModel;
import com.martynaroj.traveljournal.viewmodels.TravelViewModel;
import com.martynaroj.traveljournal.viewmodels.UserViewModel;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.DOWNLOAD_SERVICE;
import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class DetailsFragment extends BaseFragment implements View.OnClickListener {

    private FragmentDetailsBinding binding;
    private UserViewModel userViewModel;
    private ReservationViewModel reservationViewModel;
    private TravelViewModel travelViewModel;
    private StorageViewModel storageViewModel;

    private User user;
    private Travel travel;
    private Address destination;
    private Reservation accommodation;
    private Reservation transport;
    private List<Day> days;

    private Dialog editDialog;
    private DialogEditTravelDetailsBinding dialogBinding;

    private Uri newImageUri;
    private boolean changedImage;


    public static DetailsFragment newInstance(User user, Travel travel, Address destination, List<Day> days) {
        DetailsFragment fragment = new DetailsFragment();
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
        binding = FragmentDetailsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initViewModels();
        loadTravelDetails();

        setListeners();

        observeUserChanges();
        observeTravelChanges();

        return view;
    }


    //INIT DATA-------------------------------------------------------------------------------------


    private void initViewModels() {
        if (getActivity() != null) {
            userViewModel = new ViewModelProvider(getActivity()).get(UserViewModel.class);
            reservationViewModel = new ViewModelProvider(getActivity()).get(ReservationViewModel.class);
            travelViewModel = new ViewModelProvider(getActivity()).get(TravelViewModel.class);
            storageViewModel = new ViewModelProvider(getActivity()).get(StorageViewModel.class);
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
        if (travel != null)
            binding.setBudget(new DecimalFormat("0.00").format(travel.getBudget()));
        else
            binding.setBudget(null);
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
            this.user = user;
            if (user == null) {
                showSnackBar(getResources().getString(R.string.messages_not_logged_user), Snackbar.LENGTH_LONG);
                back();
            }
        });
    }


    private void observeTravelChanges() {
        travelViewModel.getTravel().observe(getViewLifecycleOwner(), travel -> {
            if (travel == null)
                back();
            this.travel = travel;
            initContentData();
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
                newImageUri = null;
                editDialog.dismiss();
                break;
            case R.id.dialog_edit_travel_details_image_file_remove_button:
                removeImage();
                break;
            case R.id.details_end_button:
                showEndTravelDialog();
                break;
        }
    }


    private void setEditDialogListeners() {
        dialogBinding.dialogEditTravelDetailsTagsTextInput.setOnClickListener(this);
        dialogBinding.dialogEditTravelDetailsTagsTextInput.setOnFocusChangeListener((view, focus) ->
                dialogBinding.dialogEditTravelDetailsTagsError.setVisibility(View.GONE)
        );
        dialogBinding.dialogEditTravelDetailsImageButton.setOnClickListener(this);
        dialogBinding.dialogEditTravelDetailsImageFileRemoveButton.setOnClickListener(this);
        dialogBinding.dialogEditTravelDetailsButtonNegative.setOnClickListener(this);
        dialogBinding.dialogEditTravelDetailsButtonPositive.setOnClickListener(this);
        dialogBinding.dialogEditTravelDetailsNameTextInput.setOnFocusChangeListener((view, b) ->
                dialogBinding.dialogEditTravelDetailsNameTextLayout.setError(null)
        );
        dialogBinding.dialogEditTravelDetailsNameTextInput.setOnClickListener(view ->
                dialogBinding.dialogEditTravelDetailsNameTextLayout.setError(null)
        );
    }


    //DETAILS---------------------------------------------------------------------------------------


    private void downloadFile(String url) {
        if (getContext() != null) {
            String file = readFilenameFromUrl(url);
            if (file != null) {
                DownloadManager downloadManager = (DownloadManager) getContext().getSystemService(DOWNLOAD_SERVICE);
                Uri uri = Uri.parse(url);
                DownloadManager.Request request = new DownloadManager.Request(uri);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalFilesDir(getContext(), DIRECTORY_DOWNLOADS, file);
                if (downloadManager != null) {
                    downloadManager.enqueue(request);
                    showSnackBar(getResources().getString(R.string.messages_downloading_file), Snackbar.LENGTH_SHORT);
                } else
                    showSnackBar(getResources().getString(R.string.messages_error_failed_download_file), Snackbar.LENGTH_LONG);
            } else
                showSnackBar(getResources().getString(R.string.messages_error_failed_read_file), Snackbar.LENGTH_LONG);
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


    //EDIT STUFF------------------------------------------------------------------------------------


    private void showEditDetailsDialog() {
        if (getContext() != null) {
            editDialog = DialogHandler.createDialog(getContext(), true);
            dialogBinding = DialogEditTravelDetailsBinding.inflate(LayoutInflater.from(getContext()));
            editDialog.setContentView(dialogBinding.getRoot());
            editDialog.setOnCancelListener(dialogInterface -> newImageUri = null);
            setEditDialogContentData();
            editDialog.show();
        }
    }


    private void setEditDialogContentData() {
        if (travel != null) {
            if (travel.getImage() != null)
                loadTravelImage(travel.getImage(), readFilenameFromUrl(travel.getImage()));
            dialogBinding.dialogEditTravelDetailsNameTextInput.setText(travel.getName());
            setTagsView();
            setEditDialogListeners();
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
            saveEditChanges();
            editDialog.dismiss();
        } else if (!isTagsValid) {
            dialogBinding.dialogEditTravelDetailsTagsError.setVisibility(View.VISIBLE);
            dialogBinding.dialogEditTravelDetailsContentContainer.fullScroll(View.FOCUS_DOWN);
        }
    }


    private void removeImage() {
        newImageUri = null;
        changedImage = true;
        dialogBinding.dialogEditTravelDetailsImageFileContainer.setVisibility(View.GONE);
        dialogBinding.dialogEditTravelDetailsImageButton.setImageResource(R.drawable.no_image);
    }


    private void saveEditChanges() {
        Editable newNameEditable = dialogBinding.dialogEditTravelDetailsNameTextInput.getText();
        String newName = newNameEditable != null ? newNameEditable.toString() : "";
        travel.setName(newName);
        travel.setTags(getUniqueTags(dialogBinding.dialogEditTravelDetailsTagsTextInput));
        if (changedImage) {
            if (newImageUri != null) {
                prepareImageToSave();
                return;
            } else
                travel.setImage(null);
        }
        updateTravel(travel.getImage());
    }


    private void updateTravel(String image) {
        travel.setImage(image);
        travelViewModel.updateTravel(travel.getId(), new HashMap<String, Object>() {{
            put(Constants.DB_NAME, travel.getName());
            put(Constants.DB_IMAGE, image);
            put(Constants.DB_TAGS, travel.getTags());
        }});
        showSnackBar(getResources().getString(R.string.messages_changes_saved), Snackbar.LENGTH_SHORT);
        travelViewModel.setTravel(travel);
    }


    //IMAGE-----------------------------------------------------------------------------------------


    private void selectImage() {
        if (getContext() != null)
            CropImage.activity().setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                    .setAspectRatio(3, 2).start(getContext(), this);
    }


    private void checkPermissionsToSelectImage() {
        if (RequestPermissionsHandler.isReadStorageGranted(getContext()))
            selectImage();
        else
            RequestPermissionsHandler.requestReadStorage(getActivity());
    }


    private void prepareImageToSave() {
        if (newImageUri.getPath() != null && getContext() != null) {
            byte[] thumb = FileCompressor.compressToByte(
                    getContext(),
                    newImageUri,
                    Constants.TRAVEL_IMG_H,
                    Constants.TRAVEL_IMG_W
            );
            if (thumb != null)
                uploadImage(thumb);
            else
                showSnackBar(getResources().getString(R.string.messages_error_failed_load_file), Snackbar.LENGTH_LONG);
        }
    }


    private void uploadImage(byte[] thumb) {
        startProgressBar();
        String path = user.getUid() + "/" + Constants.STORAGE_TRAVELS + "/" + travel.getId();
        storageViewModel.saveImageToStorage(thumb, Constants.IMAGE + Constants.JPG_EXT, path);
        storageViewModel.getStorageStatus().observe(getViewLifecycleOwner(), status -> {
            if (status != null && !status.contains(getResources().getString(R.string.messages_error)))
                updateTravel(status);
            else
                showSnackBar(status, Snackbar.LENGTH_LONG);
            stopProgressBar();
        });
    }


    //END-------------------------------------------------------------------------------------------


    private void showEndTravelDialog() {
        if (getContext() != null) {
            Dialog dialog = DialogHandler.createDialog(getContext(), true);
            DialogCustomBinding binding = DialogCustomBinding.inflate(LayoutInflater.from(getContext()));
            dialog.setContentView(binding.getRoot());
            DialogHandler.initContent(getContext(), binding.dialogCustomTitle, R.string.dialog_end_travel_title,
                    binding.dialogCustomDesc, R.string.dialog_end_travel_desc,
                    binding.dialogCustomButtonPositive, R.string.dialog_button_yes,
                    binding.dialogCustomButtonNegative, R.string.dialog_button_no,
                    R.color.main_blue, R.color.blue_bg_light);
            binding.dialogCustomButtonPositive.setOnClickListener(v -> {
                changeFragment(EndTravelFragment.newInstance(this.user, this.travel, this.destination,
                        this.transport, this.accommodation, this.days));
                dialog.dismiss();
            });
            binding.dialogCustomButtonNegative.setOnClickListener(v -> dialog.dismiss());
            dialog.show();
        }
    }


    //OTHERS----------------------------------------------------------------------------------------


    private List<String> getUniqueTags(NachoTextView tagsInput) {
        return new ArrayList<>(new LinkedHashSet<>(tagsInput.getChipValues()));
    }


    private void changeFragment(Fragment next) {
        getNavigationInteractions().changeFragment(this, next, true);
    }


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
                changedImage = true;
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
