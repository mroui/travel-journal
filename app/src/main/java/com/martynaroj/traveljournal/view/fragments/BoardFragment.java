package com.martynaroj.traveljournal.view.fragments;


import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.FragmentBoardBinding;
import com.martynaroj.traveljournal.services.models.Address;
import com.martynaroj.traveljournal.services.models.Day;
import com.martynaroj.traveljournal.services.models.Travel;
import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.services.models.packing.PackingCategory;
import com.martynaroj.traveljournal.services.models.weatherAPI.WeatherResult;
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.view.others.classes.RippleDrawable;
import com.martynaroj.traveljournal.view.others.enums.Emoji;
import com.martynaroj.traveljournal.view.others.enums.Status;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;
import com.martynaroj.traveljournal.viewmodels.AddressViewModel;
import com.martynaroj.traveljournal.viewmodels.DayViewModel;
import com.martynaroj.traveljournal.viewmodels.TravelViewModel;
import com.martynaroj.traveljournal.viewmodels.UserViewModel;
import com.martynaroj.traveljournal.viewmodels.WeatherViewModel;
import com.nightonke.boommenu.BoomButtons.TextInsideCircleButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoardFragment extends BaseFragment implements View.OnClickListener {

    private FragmentBoardBinding binding;
    private UserViewModel userViewModel;
    private TravelViewModel travelViewModel;
    private AddressViewModel addressViewModel;
    private WeatherViewModel weatherViewModel;
    private DayViewModel dayViewModel;

    private User user;
    private Travel travel;
    private Address destination;
    private WeatherResult weatherResult;
    private Dialog packingDialog;

    private ImageView emojiHappy, emojiNormal, emojiSad, emojiLucky, emojiShocked, emojiBored;
    private Emoji rate;

    private List<Day> days;
    private Day today;

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
        observeTravelChanges();
        observeTodayChanges();
        observeDaysChanges();

        setListeners();

        return view;
    }


    //INIT DATA-------------------------------------------------------------------------------------


    private void initViewModels() {
        if (getActivity() != null) {
            userViewModel = new ViewModelProvider(getActivity()).get(UserViewModel.class);
            travelViewModel = new ViewModelProvider(getActivity()).get(TravelViewModel.class);
            addressViewModel = new ViewModelProvider(getActivity()).get(AddressViewModel.class);
            weatherViewModel = new ViewModelProvider(getActivity()).get(WeatherViewModel.class);
            dayViewModel = new ViewModelProvider(getActivity()).get(DayViewModel.class);
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
                } else
                    showSnackBar(getResources().getString(R.string.messages_error_current_user_not_available), Snackbar.LENGTH_LONG);
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
            initContentData();
            if (user != null && user.getActiveTravelId() != null && !user.getActiveTravelId().equals(""))
                loadTravel(user.getActiveTravelId());
            else
                travelViewModel.setTravel(null);
        });
    }


    private void observeTravelChanges() {
        travelViewModel.getTravel().observe(getViewLifecycleOwner(), travel -> {
            this.travel = travel;
            initContentData();
            checkPackingList();
        });
    }


    private void observeTodayChanges() {
        dayViewModel.getToday().observe(getViewLifecycleOwner(), today -> this.today = today);
    }


    private void observeDaysChanges() {
        dayViewModel.getDays().observe(getViewLifecycleOwner(), days -> {
            this.days = days;
            checkDays();
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


    private List<PackingCategory> getBasicPackingList() {
        List<PackingCategory> basicList = new ArrayList<>();
        for (String category : getResources().getStringArray(R.array.packing_categories))
            basicList.add(new PackingCategory(category));
        return basicList;
    }


    private void initContentData() {
        binding.setUser(user);
        binding.setTravel(travel);
        binding.setDestination(destination);
        binding.setWeatherResult(weatherResult);
    }


    //LISTENERS-------------------------------------------------------------------------------------


    private void setListeners() {
        binding.boardNewJourneyButton.setOnClickListener(this);
        binding.boardFloatingPackingListButton.setOnClickListener(this);
        binding.boardFloatingRateButton.setOnClickListener(this);
        binding.boardTravelGridAddNoteCard.setOnClickListener(this);
        binding.boardTravelGridAddPhotoCard.setOnClickListener(this);
        binding.boardTravelGridAddPlaceCard.setOnClickListener(this);
        binding.boardTravelGridDetailsCard.setOnClickListener(this);
        binding.boardTravelGridManageBudgetCard.setOnClickListener(this);
        binding.boardTravelGridExplorePlacesCard.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.board_new_journey_button:
                startNewJourney();
                break;
            case R.id.board_floating_packing_list_button:
                changeFragment(PackingListFragment.newInstance(this.travel));
                break;
            case R.id.board_floating_rate_button:
                showRateDialog();
                break;
            case R.id.dialog_rate_day_emoji_happy:
                emojiOnClick(Emoji.HAPPY, view, R.drawable.ic_emoji_happy_color);
                break;
            case R.id.dialog_rate_day_emoji_normal:
                emojiOnClick(Emoji.NORMAL, view, R.drawable.ic_emoji_normal_color);
                break;
            case R.id.dialog_rate_day_emoji_sad:
                emojiOnClick(Emoji.SAD, view, R.drawable.ic_emoji_sad_color);
                break;
            case R.id.dialog_rate_day_emoji_lucky:
                emojiOnClick(Emoji.LUCKY, view, R.drawable.ic_emoji_lucky_color);
                break;
            case R.id.dialog_rate_day_emoji_shocked:
                emojiOnClick(Emoji.SHOCKED, view, R.drawable.ic_emoji_shocked_color);
                break;
            case R.id.dialog_rate_day_emoji_bored:
                emojiOnClick(Emoji.BORED, view, R.drawable.ic_emoji_bored_color);
                break;
            case R.id.board_travel_grid_add_note_card:
                changeFragment(NotesFragment.newInstance(today, days));
                break;
            case R.id.board_travel_grid_add_photo_card:
                changeFragment(PhotosFragment.newInstance(today, days));
                break;
            case R.id.board_travel_grid_add_place_card:
                //todo add place
                break;
            case R.id.board_travel_grid_details_card:
                changeFragment(DetailsFragment.newInstance(user, travel, destination));
                break;
            case R.id.board_travel_grid_manage_budget_card:
                changeFragment(BudgetFragment.newInstance(travel, today, days));
                break;
            case R.id.board_travel_grid_explore_places_card:
                changeFragment(ExploreFragment.newInstance(destination));
                break;
        }
    }


    //DIALOG----------------------------------------------------------------------------------------


    private void checkPackingList() {
        if (this.travel != null && !this.travel.isPacking() && travel.getPackingList() == null
                && (packingDialog == null || !packingDialog.isShowing())) {
            showPackingDialog();
        }
    }


    private void showPackingDialog() {
        if (getContext() != null && getActivity() != null) {
            packingDialog = new Dialog(getContext());
            packingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            packingDialog.setCancelable(false);
            packingDialog.setContentView(R.layout.dialog_custom);

            TextView title = packingDialog.findViewById(R.id.dialog_custom_title);
            TextView message = packingDialog.findViewById(R.id.dialog_custom_desc);
            MaterialButton buttonPositive = packingDialog.findViewById(R.id.dialog_custom_button_positive);
            MaterialButton buttonNegative = packingDialog.findViewById(R.id.dialog_custom_button_negative);

            title.setText(getResources().getString(R.string.dialog_packing_title));
            message.setText(getResources().getString(R.string.dialog_packing_desc));
            buttonPositive.setText(getResources().getString(R.string.dialog_button_yes));
            RippleDrawable.setRippleEffectButton(
                    buttonPositive,
                    Color.TRANSPARENT,
                    getResources().getColor(R.color.yellow_bg_lighter)
            );
            buttonPositive.setTextColor(getResources().getColor(R.color.main_yellow));
            buttonPositive.setOnClickListener(v -> {
                packingDialog.dismiss();
                this.travel.setPackingList(getBasicPackingList());
                updateTravel(new HashMap<String, Object>() {{
                    put(Constants.DB_IS_PACKING, true);
                    put(Constants.DB_PACKING_LIST, getBasicPackingList());
                }});
                changeFragment(PackingListFragment.newInstance(this.travel));
            });
            buttonNegative.setText(getResources().getString(R.string.dialog_button_no));
            RippleDrawable.setRippleEffectButton(
                    buttonNegative,
                    Color.TRANSPARENT,
                    getResources().getColor(R.color.yellow_bg_lighter)
            );
            buttonNegative.setTextColor(getResources().getColor(R.color.main_yellow));
            buttonNegative.setOnClickListener(v -> {
                packingDialog.dismiss();
                this.travel.setPackingList(new ArrayList<>());
                updateTravel(new HashMap<String, Object>() {{
                    put(Constants.DB_PACKING_LIST, new ArrayList<>());
                }});
            });

            packingDialog.show();
        }
    }


    private void showRateDialog() {
        if (getContext() != null && getActivity() != null) {
            Dialog dialog = new Dialog(getContext());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(true);
            dialog.setContentView(R.layout.dialog_rate_day);

            emojiHappy = dialog.findViewById(R.id.dialog_rate_day_emoji_happy);
            emojiNormal = dialog.findViewById(R.id.dialog_rate_day_emoji_normal);
            emojiSad = dialog.findViewById(R.id.dialog_rate_day_emoji_sad);
            emojiLucky = dialog.findViewById(R.id.dialog_rate_day_emoji_lucky);
            emojiShocked = dialog.findViewById(R.id.dialog_rate_day_emoji_shocked);
            emojiBored = dialog.findViewById(R.id.dialog_rate_day_emoji_bored);
            setDefaultEmoji();
            setEmojiListener();

            MaterialButton buttonPositive = dialog.findViewById(R.id.dialog_rate_day_button_positive);
            MaterialButton buttonNegative = dialog.findViewById(R.id.dialog_rate_day_button_negative);
            buttonNegative.setOnClickListener(v -> dialog.dismiss());
            buttonPositive.setOnClickListener(v -> {
                today.setRate(rate.ordinal());
                updateDay(new HashMap<String, Object>() {{
                    put(Constants.DB_RATE, rate.ordinal());
                }});
                dialog.dismiss();
            });
            dialog.show();
        }
    }


    //EMOJI-----------------------------------------------------------------------------------------


    private void setEmojiListener() {
        emojiHappy.setOnClickListener(this);
        emojiNormal.setOnClickListener(this);
        emojiSad.setOnClickListener(this);
        emojiLucky.setOnClickListener(this);
        emojiShocked.setOnClickListener(this);
        emojiBored.setOnClickListener(this);
    }


    private void emojiOnClick(Emoji rate, View view, int resource) {
        this.rate = rate;
        clearEmoji();
        ((ImageView) view).setImageResource(resource);
    }


    private void clearEmoji() {
        emojiHappy.setImageResource(R.drawable.ic_emoji_happy);
        emojiNormal.setImageResource(R.drawable.ic_emoji_normal);
        emojiSad.setImageResource(R.drawable.ic_emoji_sad);
        emojiLucky.setImageResource(R.drawable.ic_emoji_lucky);
        emojiShocked.setImageResource(R.drawable.ic_emoji_shocked);
        emojiBored.setImageResource(R.drawable.ic_emoji_bored);
    }


    private void setDefaultEmoji() {
        switch (Emoji.values()[today.getRate()]) {
            case HAPPY:
                emojiOnClick(Emoji.HAPPY, emojiHappy, R.drawable.ic_emoji_happy_color);
                break;
            case NORMAL:
                emojiOnClick(Emoji.NORMAL, emojiNormal, R.drawable.ic_emoji_normal_color);
                break;
            case SAD:
                emojiOnClick(Emoji.SAD, emojiSad, R.drawable.ic_emoji_sad_color);
                break;
            case LUCKY:
                emojiOnClick(Emoji.LUCKY, emojiLucky, R.drawable.ic_emoji_lucky_color);
                break;
            case SHOCKED:
                emojiOnClick(Emoji.SHOCKED, emojiShocked, R.drawable.ic_emoji_shocked_color);
                break;
            case BORED:
                emojiOnClick(Emoji.BORED, emojiBored, R.drawable.ic_emoji_bored_color);
                break;
        }
    }


    //DATABASE--------------------------------------------------------------------------------------


    private void updateTravel(Map<String, Object> changes) {
        travelViewModel.updateTravel(travel.getId(), changes);
        loadTravel(travel.getId());
    }


    private void updateDay(Map<String, Object> changes) {
        dayViewModel.updateDay(today.getId(), changes);
    }


    private void loadTravel(String id) {
        startProgressBar();
        travelViewModel.getTravelData(id);
        travelViewModel.getTravelLiveData().observe(getViewLifecycleOwner(), travel -> {
            travelViewModel.setTravel(travel);
            this.travel = travel;
            initContentData();
            if (travel != null) {
                loadDestination(travel.getDestination());
                loadDays();
            } else
                stopProgressBar();
        });
    }


    private void loadDays() {
        days = new ArrayList<>();
        if (travel.getDays().size() > 0) {
            startProgressBar();
            dayViewModel.getDaysListData(travel.getDays());
            dayViewModel.getDaysList().observe(getViewLifecycleOwner(), list -> {
                if (list != null)
                    days = list;
                checkDays();
                stopProgressBar();
            });
        } else
            checkDays();
    }


    private void checkDays() {
        long whichDay = travel.whatDay();
        if (whichDay >= 1) {
            if (travel.getDays().size() == whichDay) {
                getToday(travel.getDays().get((int) whichDay - 1));
            } else {
                while (travel.getDays().size() != whichDay - 1)
                    travel.getDays().add(null);
                addNewDay();
            }
        }
    }


    private void addNewDay() {
        String id = dayViewModel.generateId();
        today = new Day(id, Calendar.getInstance().getTimeInMillis());
        dayViewModel.addDay(today);
        dayViewModel.getStatusData().observe(getViewLifecycleOwner(), status -> {
            if (status != null && !status.equals(Status.ERROR)) {
                travel.getDays().add(id);
                updateTravel(new HashMap<String, Object>() {{
                    put(Constants.DB_DAYS, travel.getDays());
                }});
            } else
                showSnackBar(getResources().getString(R.string.messages_error_failed_add_new_day), Snackbar.LENGTH_LONG);
        });
    }


    private void getToday(String id) {
        if (travel != null && travel.getDays() != null && id != null && days.size() > 0)
            for (Day day : days)
                if (day.getId().equals(id)) {
                    today = day;
                    break;
                }
    }


    private void loadDestination(String id) {
        startProgressBar();
        addressViewModel.getAddress(id);
        addressViewModel.getAddressData().observe(getViewLifecycleOwner(), destination -> {
            this.destination = destination;
            initContentData();
            if (destination != null)
                loadWeather();
            else
                stopProgressBar();
        });
    }


    private void loadWeather() {
        startProgressBar();
        weatherViewModel.getWeather(new LatLng(destination.getLatitude(), destination.getLongitude()));
        weatherViewModel.getWeatherResultData().observe(getViewLifecycleOwner(), weatherResult -> {
            this.weatherResult = weatherResult;
            initContentData();
            if (weatherResult == null)
                showSnackBar(getResources().getString(R.string.messages_error_localize), Snackbar.LENGTH_LONG);
            stopProgressBar();
        });
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
