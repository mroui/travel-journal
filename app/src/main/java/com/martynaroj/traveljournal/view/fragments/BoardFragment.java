package com.martynaroj.traveljournal.view.fragments;


import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.FragmentBoardBinding;
import com.martynaroj.traveljournal.services.models.Travel;
import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.view.others.classes.RippleDrawable;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;
import com.martynaroj.traveljournal.viewmodels.TravelViewModel;
import com.martynaroj.traveljournal.viewmodels.UserViewModel;
import com.nightonke.boommenu.BoomButtons.TextInsideCircleButton;

import java.util.ArrayList;
import java.util.HashMap;

public class BoardFragment extends BaseFragment implements View.OnClickListener {

    private FragmentBoardBinding binding;
    private UserViewModel userViewModel;
    private TravelViewModel travelViewModel;
    private User user;
    private Travel travel;

    public static BoardFragment newInstance() {
        return new BoardFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBoardBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initViewModels();
        initLoggedUser();
        initFloatingMenu();

        observeUserChanges();

        setListeners();

        return view;
    }


    //INIT DATA-------------------------------------------------------------------------------------


    private void initViewModels() {
        if (getActivity() != null) {
            userViewModel = new ViewModelProvider(getActivity()).get(UserViewModel.class);
            travelViewModel = new ViewModelProvider(getActivity()).get(TravelViewModel.class);
        }
    }


    private void initLoggedUser() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            startProgressBar();
            userViewModel.getUserData(firebaseAuth.getCurrentUser().getUid());
            userViewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
                if (user != null) {
                    this.user = user;
                    binding.setUser(user);
                } else {
                    showSnackBar(getResources().getString(R.string.messages_error_current_user_not_available), Snackbar.LENGTH_LONG);
                }
                stopProgressBar();
            });
        }
    }


    private void initFloatingMenu() {
        binding.boardFloatingMenuButton.addBuilder(createMenuItem(R.drawable.ic_map_white, Constants.MAP,
                getResources().getColor(R.color.main_red)));
        binding.boardFloatingMenuButton.addBuilder(createMenuItem(R.drawable.ic_weather_white, Constants.WEATHER,
                getResources().getColor(R.color.main_yellow)));
        binding.boardFloatingMenuButton.addBuilder(createMenuItem(R.drawable.ic_money_white, Constants.CURRENCY,
                getResources().getColor(R.color.main_green)));
        binding.boardFloatingMenuButton.addBuilder(createMenuItem(R.drawable.ic_translator_white, Constants.TRANSLATOR,
                getResources().getColor(R.color.main_blue)));
        binding.boardFloatingMenuButton.addBuilder(createMenuItem(R.drawable.ic_alarm_white, Constants.ALARM,
                getResources().getColor(R.color.main_violet)));
    }


    private void observeUserChanges() {
        userViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            this.user = user;
            binding.setUser(user);
            if (user.getActiveTravelId() != null && !user.getActiveTravelId().equals(""))
                loadTravel(user.getActiveTravelId());
        });
    }


    private void loadTravel(String id) {
        startProgressBar();
        travelViewModel.getTravel(id);
        travelViewModel.getTravelData().observe(getViewLifecycleOwner(), travel -> {
            if (travel != null) {
                this.travel = travel;
                //todo: set travel on view later
                //binding.setTravel(travel);
                checkPackingList();
            }
            stopProgressBar();
        });
    }


    private TextInsideCircleButton.Builder createMenuItem(int icon, String text, int color) {
        if (getContext() != null) {
            return new TextInsideCircleButton.Builder()
                    .normalImageRes(icon)
                    .imagePadding(new Rect(30, 30, 30, 50))
                    .normalText(text)
                    .typeface(ResourcesCompat.getFont(getContext(), R.font.raleway_bold))
                    .textSize(8)
                    .normalColor(color)
                    .highlightedColor(Color.WHITE)
                    .rippleEffect(true)
                    .listener(index -> {
                        if (user != null) {
                            switch (index) {
                                case 0:
                                    changeFragment(MapFragment.newInstance());
                                    break;
                                case 1:
                                    changeFragment(WeatherFragment.newInstance());
                                    break;
                                case 2:
                                    changeFragment(CurrencyFragment.newInstance());
                                    break;
                                case 3:
                                    changeFragment(TranslatorFragment.newInstance());
                                    break;
                                case 4:
                                    changeFragment(AlarmFragment.newInstance());
                            }
                        } else
                            showSnackBar(getResources().getString(R.string.messages_not_logged_user), Snackbar.LENGTH_LONG);
                    });
        } else return null;
    }


    //LISTENERS-------------------------------------------------------------------------------------


    private void setListeners() {
        binding.boardNewJourneyButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.board_new_journey_button) {
            startNewJourney();
        }
    }


    //DIALOG----------------------------------------------------------------------------------------


    private void checkPackingList() {
        if (!this.travel.isPacking() && travel.getPackingList() == null) {
            showPackingDialog();
        }
    }


    private void showPackingDialog() {
        if (getContext() != null && getActivity() != null) {
            Dialog dialog = new Dialog(getContext());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.dialog_custom);

            TextView title = dialog.findViewById(R.id.dialog_custom_title);
            TextView message = dialog.findViewById(R.id.dialog_custom_desc);
            MaterialButton buttonPositive = dialog.findViewById(R.id.dialog_custom_buttom_positive);
            MaterialButton buttonNegative = dialog.findViewById(R.id.dialog_custom_button_negative);

            title.setText(getResources().getString(R.string.dialog_packing_title));
            message.setText(getResources().getString(R.string.dialog_packing_desc));
            buttonPositive.setText(getResources().getString(R.string.dialog_button_yes));
            RippleDrawable.setRippleEffectButton(
                    buttonPositive,
                    Color.TRANSPARENT,
                    getResources().getColor(R.color.yellow_bg_lighter)
            );
            buttonPositive.setTextColor(getResources().getColor(R.color.yellow_active));
            buttonPositive.setOnClickListener(v -> {
                dialog.dismiss();
                travelViewModel.updateTravel(travel.getId(), new HashMap<String, Object>() {{
                    put(Constants.DB_IS_PACKING, true);
                    put(Constants.DB_PACKING_LIST, new ArrayList<>());
                }});
                //todo: go to packing list fragment
            });
            buttonNegative.setText(getResources().getString(R.string.dialog_button_no));
            RippleDrawable.setRippleEffectButton(
                    buttonNegative,
                    Color.TRANSPARENT,
                    getResources().getColor(R.color.yellow_bg_lighter)
            );
            buttonNegative.setTextColor(getResources().getColor(R.color.yellow_active));
            buttonNegative.setOnClickListener(v -> {
                dialog.dismiss();
                travelViewModel.updateTravel(travel.getId(), new HashMap<String, Object>() {{
                    put(Constants.DB_PACKING_LIST, new ArrayList<>());
                }});
            });

            dialog.show();
        }
    }


    //OTHERS----------------------------------------------------------------------------------------


    private void startNewJourney() {
        if (user != null) {
            changeFragment(CreateTravelFragment.newInstance(user));
        } else {
            showSnackBar(getResources().getString(R.string.messages_not_logged_user), Snackbar.LENGTH_LONG);
        }
    }


    private void changeFragment(Fragment next) {
        getNavigationInteractions().changeFragment(this, next, true);
    }


    private void startProgressBar() {
        getProgressBarInteractions().startProgressBar(binding.getRoot(),
                binding.boardProgressbarLayout, binding.boardProgressbar);
    }


    private void stopProgressBar() {
        getProgressBarInteractions().stopProgressBar(binding.getRoot(),
                binding.boardProgressbarLayout, binding.boardProgressbar);
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
