package com.martynaroj.traveljournal.view.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.DialogAddNoteBinding;
import com.martynaroj.traveljournal.databinding.FragmentPhotosBinding;
import com.martynaroj.traveljournal.services.models.Day;
import com.martynaroj.traveljournal.services.models.Photo;
import com.martynaroj.traveljournal.services.models.Travel;
import com.martynaroj.traveljournal.view.adapters.PhotoAdapter;
import com.martynaroj.traveljournal.view.others.classes.DialogHandler;
import com.martynaroj.traveljournal.view.others.classes.FileCompressor;
import com.martynaroj.traveljournal.view.others.classes.RequestPermissionsHandler;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;
import com.martynaroj.traveljournal.viewmodels.StorageViewModel;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class PhotosFragment extends NotesFragment {

    private FragmentPhotosBinding binding;
    private StorageViewModel storageViewModel;
    private Travel travel;
    private List<Photo> photos;
    private PhotoAdapter adapter;

    private ImageButton imageButton;
    private TextInputEditText imageDescription;
    private TextView imageError;
    private Uri newImageUri;


    public static PhotosFragment newInstance(Travel travel, Day day, List<Day> days) {
        PhotosFragment fragment = new PhotosFragment();
        Bundle args = new Bundle();
        args.putSerializable(Constants.BUNDLE_TRAVEL, travel);
        args.putSerializable(Constants.BUNDLE_DAY, day);
        args.putSerializable(Constants.BUNDLE_DAYS, (Serializable) days);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            travel = (Travel) getArguments().getSerializable(Constants.BUNDLE_TRAVEL);
        }
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPhotosBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initViewModels();
        initContentData();
        setListeners();
        observeUserChanges();

        return view;
    }


    //INIT DATA-------------------------------------------------------------------------------------


    @Override
    void initViewModels() {
        super.initViewModels();
        if (getActivity() != null)
            storageViewModel = new ViewModelProvider(getActivity()).get(StorageViewModel.class);
    }

    private void initContentData() {
        photos = getAllDaysPhotosList();
        initListAdapter();
        setBindingData();
    }


    private void initListAdapter() {
        if (getContext() != null) {
            adapter = new PhotoAdapter(getContext(), photos);
            binding.photosListRecyclerView.setAdapter(adapter);
            setOnItemClickListener();
        }
    }


    private void setBindingData() {
        binding.setIsListEmpty(photos.size() == 0);
    }


    //LISTENERS-------------------------------------------------------------------------------------


    private void setListeners() {
        binding.photosArrowButton.setOnClickListener(this);
        binding.photosAddFloatingButton.setOnClickListener(this);
        setOnListScrollListener(binding.photosListRecyclerView, binding.photosAddFloatingButton);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.photos_arrow_button:
                back();
                break;
            case R.id.photos_add_floating_button:
                showAddPhotoDialog();
                break;
        }
    }


    private void setOnItemClickListener() {
        adapter.setOnItemLongClickListener((object, position, view) -> {
            //todo: show options
        });
    }


    //PHOTOS / LIST---------------------------------------------------------------------------------


    private List<Photo> getAllDaysPhotosList() {
        List<Photo> list = new ArrayList<>();
        for (Day day : days)
            list.addAll(day.getPhotos());
        Collections.sort(list);
        Collections.reverse(list);
        return list;
    }


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
        String path = user.getUid() + "/" + Constants.STORAGE_TRAVELS + "/" + travel.getId() + "/" + Constants.STORAGE_DAYS;
        storageViewModel.saveImageToStorage(thumb, System.currentTimeMillis() + Constants.JPG_EXT, path);
        storageViewModel.getStorageStatus().observe(getViewLifecycleOwner(), status -> {
            if (status != null && !status.contains(getResources().getString(R.string.messages_error)))
                addPhoto(status);
            else
                showSnackBar(status, Snackbar.LENGTH_LONG);
            stopProgressBar();
        });
    }


    private void addPhoto(String url) {
        String description = imageDescription.getText() != null ? imageDescription.getText().toString() : null;
        adapter.add(new Photo(url, description));
        photos = adapter.getList();
        binding.photosListRecyclerView.scrollToPosition(0);
        dayViewModel.updateDay(today.getId(), new HashMap<String, Object>() {{
            put(Constants.DB_PHOTOS, adapter.getTodayList());
        }});
        today.setPhotos(adapter.getTodayList());
        dayViewModel.setToday(today);
        setBindingData();
    }


    private void loadImage() {
        if (getContext() != null)
            Glide.with(getContext()).load(newImageUri).centerCrop().into(imageButton);
        imageError.setVisibility(View.GONE);
    }


    //DIALOG----------------------------------------------------------------------------------------


    private void showAddPhotoDialog() {
        if (getContext() != null) {
            Dialog dialog = DialogHandler.createDialog(getContext(), true);
            DialogAddNoteBinding binding = DialogAddNoteBinding.inflate(LayoutInflater.from(getContext()));
            dialog.setContentView(binding.getRoot());
            DialogHandler.initContent(getContext(), binding.dialogAddNoteTitle, R.string.dialog_add_photo_title,
                    binding.dialogAddNoteDesc, R.string.dialog_add_photo_desc,
                    binding.dialogAddNoteButtonPositive, R.string.dialog_button_add,
                    binding.dialogAddNoteButtonNegative, R.string.dialog_button_cancel,
                    R.color.main_green, R.color.green_bg_light);

            binding.dialogAddNoteInputLayout.setBoxStrokeColor(getResources().getColor(R.color.main_green));
            imageButton = binding.dialogAddNotePhoto;
            imageDescription = binding.dialogAddNoteInput;
            imageError = binding.dialogAddNotePhotoError;

            dialog.setOnCancelListener(dialogInterface -> newImageUri = null);
            binding.dialogAddNotePhoto.setOnClickListener(view -> checkPermissionsToSelectImage());
            binding.dialogAddNoteButtonPositive.setOnClickListener(view -> {
                if (validateImage()) {
                    prepareImageToSave();
                    dialog.dismiss();
                }
            });
            binding.dialogAddNoteButtonNegative.setOnClickListener(view -> {
                dialog.dismiss();
                newImageUri = null;
            });
            dialog.show();
        }
    }


    //OTHERS----------------------------------------------------------------------------------------


    private boolean validateImage() {
        if (newImageUri != null)
            return true;
        else {
            imageError.setVisibility(View.VISIBLE);
            return false;
        }
    }


    private void startProgressBar() {
        getProgressBarInteractions().startProgressBar(binding.getRoot(),
                binding.photosProgressbarLayout, binding.photosProgressbar);
    }


    private void stopProgressBar() {
        getProgressBarInteractions().stopProgressBar(binding.getRoot(),
                binding.photosProgressbarLayout, binding.photosProgressbar);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK && result != null) {
                newImageUri = result.getUri();
                loadImage();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE && result != null)
                showSnackBar(result.getError().getMessage(), Snackbar.LENGTH_LONG);
        } else
            showSnackBar(getResources().getString(R.string.messages_error_no_file_selected), Snackbar.LENGTH_SHORT);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
