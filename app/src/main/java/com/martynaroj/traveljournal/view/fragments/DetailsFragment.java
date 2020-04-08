package com.martynaroj.traveljournal.view.fragments;

import android.app.DownloadManager;
import android.content.Context;
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
import com.martynaroj.traveljournal.databinding.FragmentDetailsBinding;
import com.martynaroj.traveljournal.services.models.Address;
import com.martynaroj.traveljournal.services.models.Reservation;
import com.martynaroj.traveljournal.services.models.Travel;
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;
import com.martynaroj.traveljournal.viewmodels.ReservationViewModel;
import com.martynaroj.traveljournal.viewmodels.UserViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class DetailsFragment extends BaseFragment implements View.OnClickListener {

    private FragmentDetailsBinding binding;
    private UserViewModel userViewModel;
    private ReservationViewModel reservationViewModel;

    private Travel travel;
    private Address destination;
    private Reservation accommodation;
    private Reservation transport;


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
        //todo: finish button & edit button
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
        }
    }


    //FILES----------------------------------------------------------------------------------------


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


    //OTHERS----------------------------------------------------------------------------------------


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
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
