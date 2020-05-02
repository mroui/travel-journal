package com.martynaroj.traveljournal.view.fragments;

import android.app.DownloadManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.FragmentTravelBinding;
import com.martynaroj.traveljournal.services.models.Itinerary;
import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.view.others.classes.FileUriUtils;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;
import com.martynaroj.traveljournal.viewmodels.ItineraryViewModel;
import com.martynaroj.traveljournal.viewmodels.UserViewModel;

import java.util.HashMap;

import static android.content.Context.DOWNLOAD_SERVICE;
import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class TravelFragment extends BaseFragment implements View.OnClickListener {

    private FragmentTravelBinding binding;

    private UserViewModel userViewModel;
    private ItineraryViewModel itineraryViewModel;

    private Itinerary itinerary;
    private User owner, user;


    public static TravelFragment newInstance(Itinerary itinerary, User user) {
        TravelFragment fragment = new TravelFragment();
        Bundle args = new Bundle();
        args.putSerializable(Constants.BUNDLE_ITINERARY, itinerary);
        args.putSerializable(Constants.BUNDLE_USER, user);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            itinerary = (Itinerary) getArguments().getSerializable(Constants.BUNDLE_ITINERARY);
            user = (User) getArguments().getSerializable(Constants.BUNDLE_USER);
        }
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTravelBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initViewModels();
        initContentData();
        setListeners();
        observeUserChanges();
        observeItineraryChanges();

        return view;
    }


    //INIT DATA-------------------------------------------------------------------------------------


    private void initViewModels() {
        if (getActivity() != null) {
            userViewModel = new ViewModelProvider(getActivity()).get(UserViewModel.class);
            itineraryViewModel = new ViewModelProvider(getActivity()).get(ItineraryViewModel.class);
        }
    }


    private void initContentData() {
        binding.setItinerary(itinerary);
        binding.setUser(user);
        initTags();
        loadOwner();
        disableSaveButton();
        setOwnerLinkUnderline();
    }


    private void initTags() {
        if (itinerary != null) {
            binding.travelTagsView.setData(itinerary.getTags(), item -> {
                SpannableString spannableString = new SpannableString(item);
                spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#000000")),
                        0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                return spannableString;
            });
        }
    }


    private void loadOwner() {
        startProgressBar();
        userViewModel.getUserData(itinerary.getOwner());
        userViewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
            this.owner = user;
            if (user != null)
                binding.setOwner(user);
            stopProgressBar();
        });
    }


    private void observeUserChanges() {
        userViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            this.user = user;
        });
    }


    private void observeItineraryChanges() {
        itineraryViewModel.getItinerary().observe(getViewLifecycleOwner(), itinerary -> {
            this.itinerary = itinerary;
            if (itinerary == null) {
                showSnackBar(getResources().getString(R.string.messages_error_failed_load_travel), Snackbar.LENGTH_LONG);
                back();
            }
        });
    }


    private void setOwnerLinkUnderline() {
        binding.travelOwnerValue.setPaintFlags(binding.travelOwnerValue.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
    }


    //LISTENERS-------------------------------------------------------------------------------------


    private void setListeners() {
        binding.travelArrowButton.setOnClickListener(this);
        binding.travelFileValue.setOnClickListener(this);
        binding.travelOwnerValue.setOnClickListener(this);
        binding.travelTagsSeeAllButton.setOnClickListener(this);
        binding.travelSaveButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.travel_arrow_button:
                back();
                break;
            case R.id.travel_file_value:
                downloadFile();
                break;
            case R.id.travel_owner_value:
                if (owner.equals(user))
                    showSnackBar(getResources().getString(R.string.messages_its_you), Snackbar.LENGTH_SHORT);
                else
                    changeFragment(ProfileFragment.newInstance(owner));
                break;
            case R.id.travel_tags_see_all_button:
                seeAllTags();
                break;
            case R.id.travel_save_button:
                saveTravel();
                break;
        }
    }


    //MAIN-------------------------------------------------------------------------------------


    private void downloadFile() {
        if (getContext() != null) {
            if (user != null) {
                Uri uri = Uri.parse(itinerary.getFile());
                String fileName = FileUriUtils.getFileName(getContext(), uri);
                DownloadManager downloadManager = (DownloadManager) getContext().getSystemService(DOWNLOAD_SERVICE);
                DownloadManager.Request request = new DownloadManager.Request(uri);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalFilesDir(getContext(), DIRECTORY_DOWNLOADS, fileName);
                if (downloadManager != null) {
                    downloadManager.enqueue(request);
                    showSnackBar(getResources().getString(R.string.messages_downloading_file), Snackbar.LENGTH_SHORT);
                } else
                    showSnackBar(getResources().getString(R.string.messages_error_failed_download_file), Snackbar.LENGTH_LONG);
            } else
                showSnackBar(getResources().getString(R.string.messages_not_logged_user), Snackbar.LENGTH_LONG);
        }
    }


    private void seeAllTags() {
        ConstraintLayout.LayoutParams constraintLayout = (ConstraintLayout.LayoutParams)
                binding.travelTagsView.getLayoutParams();
        String seeAllLessText;
        if (binding.travelTagsView.getLayoutParams().height == ConstraintLayout.LayoutParams.WRAP_CONTENT) {
            constraintLayout.height = Constants.TAGS_VIEW_HEIGHT;
            seeAllLessText = getResources().getString(R.string.travel_tags_see_all_tags);
        } else {
            constraintLayout.height = ConstraintLayout.LayoutParams.WRAP_CONTENT;
            seeAllLessText = getResources().getString(R.string.travel_tags_see_less_tags);
        }
        binding.travelTagsSeeAllButton.setPaintFlags(
                binding.travelTagsSeeAllButton.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG
        );
        binding.travelTagsSeeAllButton.setText(seeAllLessText);
        binding.travelTagsView.setLayoutParams(constraintLayout);
    }


    private void saveTravel() {
        if (user != null) {
            user.getSavedTravels().add(itinerary.getId());
            binding.setUser(user);
            userViewModel.updateUser(true, user, new HashMap<String, Object>() {{
                put(Constants.DB_SAVED_TRAVELS, user.getSavedTravels());
            }});
            itinerary.addPopularity();
            binding.setItinerary(itinerary);
            itineraryViewModel.updateItinerary(itinerary, new HashMap<String, Object>() {{
                put(Constants.DB_POPULARITY, itinerary.getPopularity());
            }});
            showSnackBar(getResources().getString(R.string.messages_save_travel_success), Snackbar.LENGTH_SHORT);
        } else
            showSnackBar(getResources().getString(R.string.messages_not_logged_user), Snackbar.LENGTH_LONG);
    }


    //OTHERS----------------------------------------------------------------------------------------


    private void disableSaveButton() {
        if(user == null || user.getSavedTravels().contains(itinerary.getId()))
            binding.travelSaveButton.setEnabled(false);
    }


    private void showSnackBar(String message, int duration) {
        getSnackBarInteractions().showSnackBar(binding.getRoot(), getActivity(), message, duration);
    }


    private void startProgressBar() {
        getProgressBarInteractions().startProgressBar(binding.getRoot(), binding.travelProgressbarLayout,
                binding.travelProgressbar);
    }


    private void stopProgressBar() {
        getProgressBarInteractions().stopProgressBar(binding.getRoot(), binding.travelProgressbarLayout,
                binding.travelProgressbar);
        disableSaveButton();
    }


    private void changeFragment(BaseFragment next) {
        getNavigationInteractions().changeFragment(getParentFragment(), next, true);
    }


    private void back() {
        if (getParentFragmentManager().getBackStackEntryCount() > 0)
            getParentFragmentManager().popBackStack();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
