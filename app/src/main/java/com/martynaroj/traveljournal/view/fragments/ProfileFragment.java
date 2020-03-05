package com.martynaroj.traveljournal.view.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.FragmentProfileBinding;
import com.martynaroj.traveljournal.services.models.Address;
import com.martynaroj.traveljournal.services.models.Notification;
import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.view.adapters.NotificationAdapter;
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;
import com.martynaroj.traveljournal.viewmodels.AddressViewModel;
import com.martynaroj.traveljournal.viewmodels.NotificationViewModel;
import com.martynaroj.traveljournal.viewmodels.UserViewModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ProfileFragment extends BaseFragment implements View.OnClickListener {

    private FragmentProfileBinding binding;
    private UserViewModel userViewModel;
    private User user;
    private User loggedUser;

    private AddressViewModel addressViewModel;
    private Geocoder geocoder;

    private NotificationViewModel notificationViewModel;
    private Dialog notificationsDialog;
    private NotificationAdapter notificationAdapter;

    public static ProfileFragment newInstance(User user) {
        return new ProfileFragment(user);
    }


    public ProfileFragment() {
        super();
    }


    private ProfileFragment(User user) {
        super();
        this.user = user;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initViewModels();
        initGeocoder();
        setListeners();

        initUser();
        observeUserChanges();
        initLoggedUser();

        return view;
    }


    //INIT DATA-------------------------------------------------------------------------------------


    private void initUser() {
        if (user != null) {
            binding.setUser(user);
            initPreferences();
            initLocalization();
        } else if (getParentFragmentManager().getBackStackEntryCount() > 0) {
            getParentFragmentManager().popBackStack();
        }
    }


    private void initGeocoder() {
        geocoder = new Geocoder(getActivity(), Locale.getDefault());
    }


    private void initLoggedUser() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            startProgressBar();
            userViewModel.getUserData(firebaseAuth.getCurrentUser().getUid());
            userViewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
                if (user != null) {
                    binding.setLoggedUser(user);
                    this.loggedUser = user;
                    checkProfile();
                } else {
                    showSnackBar(getResources().getString(R.string.messages_error_current_user_not_available), Snackbar.LENGTH_LONG);
                }
                stopProgressBar();
            });
        }
    }


    private void initLocalization() {
        if (user != null) {
            if (user.getLocation() != null && !user.getLocation().equals("")) {
                startProgressBar();
                addressViewModel.getAddress(user.getLocation());
                addressViewModel.getAddressData().observe(getViewLifecycleOwner(), address -> {
                    if (address != null) {
                        getLocationCity(address);
                    }
                    stopProgressBar();
                });
            } else stopProgressBar();
        }
    }


    private void initPreferences() {
        if (user != null && user.getPreferences() != null) {
            binding.profilePreferences.setData(user.getPreferences(), item -> {
                SpannableString spannableString = new SpannableString(item);
                spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#000000")),
                        0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                return spannableString;
            });
        }
    }


    private void initViewModels() {
        if (getActivity() != null) {
            userViewModel = new ViewModelProvider(getActivity()).get(UserViewModel.class);
            addressViewModel = new ViewModelProvider(getActivity()).get(AddressViewModel.class);
            notificationViewModel = new ViewModelProvider(getActivity()).get(NotificationViewModel.class);
        }
    }


    private void observeUserChanges() {
        userViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null && user.isUserProfile(this.user)) {
                this.user = user;
                binding.setUser(user);
                initPreferences();
                initLocalization();
            }
        });
    }


    private void setNotificationsList(List<Notification> notifications) {
        RecyclerView notificationsRecyclerView = notificationsDialog.findViewById(R.id.dialog_notifications_recycler_view);
        notificationAdapter = new NotificationAdapter(getContext(), notifications);
        notificationsRecyclerView.setAdapter(notificationAdapter);
        notificationAdapter.setOnItemClickListener((object, position, view) -> {
            Notification notification = (Notification) object;
            if (notification != null) {
                switch (view.getId()) {
                    case R.id.notification_item:
                        notificationsDialog.dismiss();
                        changeFragment(ProfileFragment.newInstance(notification.getUserFrom()));
                        break;
                    case R.id.notification_item_accept_button:
                        removeNotification(notification, position);
                        addToFriends(notification);
                        break;
                    case R.id.notification_item_discard_button:
                        removeNotification(notification, position);
                        break;
                }
            }
        });
    }


    //CHECKING/GETTING DATA-------------------------------------------------------------------------


    private void checkProfile() {
        if (user != null && user.getUid().equals(loggedUser.getUid())) {
            checkNewAccount();
        }
    }


    private void checkNewAccount() {
        if (user.getLocation() == null && user.getPreferences() == null && user.getBio() == null
        && user.getPhoto() == null)
            openUpdateProfileDialog();
    }


    private void getLocationCity(Address address) {
        try {
            List<android.location.Address> addresses = geocoder
                    .getFromLocation(address.getLatitude(), address.getLongitude(), 1);
            if (addresses != null && addresses.size() > 0) {
                binding.setLocation(addresses.get(0).getLocality() + ", " + addresses.get(0).getCountryName());
            }
        } catch (IOException ignored) {}
    }


    private void getNotifications() {
        if (getContext() != null) {
            if (user.getNotifications()!=null && !user.getNotifications().isEmpty()) {
                startProgressBar();
                notificationViewModel.getNotificationsListData(user.getNotifications());
                notificationViewModel.getNotificationsList().observe(getViewLifecycleOwner(), list -> {
                    if (list != null) {
                        getNotificationsUsersFrom(list);
                    }
                });
            } else {
                showNotificationsDialog(new ArrayList<>());
            }
        }
    }


    private void getNotificationsUsersFrom(List<Notification> notifications) {
        List<String> usersIds = new ArrayList<>();
        for (Notification notification : notifications) {
            usersIds.add(notification.getIdFrom());
        }
        userViewModel.getUsersListData(usersIds);
        userViewModel.getUsersList().observe(getViewLifecycleOwner(), users -> {
            if (users != null) {
                for (int i=0; i<notifications.size(); i++) {
                    notifications.get(i).setUserFrom(users.get(i));
                }
                showNotificationsDialog(notifications);
                stopProgressBar();
            }
        });
    }


    private List<String> getFilteredList(List<String> list, String statement) {
        List<String> filtered = new ArrayList<>();
        for (String obj : list)
            if (!obj.equals(statement))
                filtered.add(obj);
        return filtered;
    }


    private void getFriendsInfo() {
        if (loggedUser != null && user != null) {
            if (user.isUserProfile(loggedUser)) {
                getNavigationInteractions().changeFragment(this, FriendsListFragment.newInstance(user), true);
            } else if (user.hasFriend(loggedUser)) {
                getNavigationInteractions().changeFragment(getParentFragment(), FriendsListFragment.newInstance(user), true);
            } else {
                checkRequestExists();
            }
        }
    }


    //DIALOGS---------------------------------------------------------------------------------------


    private void openUpdateProfileDialog() {
        if (getContext() != null) {
            Dialog dialog = new Dialog(getContext());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.dialog_update_profile);
            dialog.findViewById(R.id.dialog_update_profile_later_button).setOnClickListener(v -> dialog.dismiss());
            dialog.findViewById(R.id.dialog_update_profile_now_button).setOnClickListener(v -> {
                openSettings();
                dialog.dismiss();
            });
            dialog.show();
        }
    }


    private void showNotificationsDialog(List<Notification> notifications) {
        if (getContext() != null) {
            notificationsDialog = new Dialog(getContext());
            notificationsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            notificationsDialog.setCancelable(true);
            notificationsDialog.setContentView(R.layout.dialog_notifications);
            notificationsDialog.findViewById(R.id.dialog_notifications_ok_button).setOnClickListener(v -> notificationsDialog.dismiss());

            if (!notifications.isEmpty()) {
                notificationsDialog.findViewById(R.id.dialog_notifications_no_results).setVisibility(View.INVISIBLE);
                setNotificationsList(notifications);
            } else {
                notificationsDialog.findViewById(R.id.dialog_notifications_recycler_view).setVisibility(View.INVISIBLE);
            }

            notificationsDialog.show();
        }
    }


    private void showContactDialog() {
        if (getContext() != null) {
            Dialog dialog = new Dialog(getContext());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(true);
            dialog.setContentView(R.layout.dialog_custom);

            TextView title = dialog.findViewById(R.id.dialog_custom_title);
            TextView message = dialog.findViewById(R.id.dialog_custom_desc);
            MaterialButton buttonPositive = dialog.findViewById(R.id.dialog_custom_buttom_positive);
            MaterialButton buttonNegative = dialog.findViewById(R.id.dialog_custom_button_negative);

            title.setText(getResources().getString(R.string.dialog_my_email_title));
            message.setText(user.getEmail());
            buttonPositive.setText(getResources().getString(R.string.dialog_button_ok));
            buttonPositive.setOnClickListener(v -> dialog.dismiss());
            buttonNegative.setVisibility(View.GONE);

            dialog.show();
        }
    }


    //LISTENERS-------------------------------------------------------------------------------------


    private void setListeners() {
        binding.profileSignOutButton.setOnClickListener(this);
        binding.profileNotifications.setOnClickListener(this);
        binding.profileContact.setOnClickListener(this);
        binding.profileTravels.setOnClickListener(this);
        binding.profileFriends.setOnClickListener(this);
        binding.profileSeeAllPreferences.setOnClickListener(this);
        binding.profileSettingsButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.profile_notifications:
                getNotifications();
                return;
            case R.id.profile_contact:
                getContactInfo();
                return;
            case R.id.profile_travels:
                changeFragment(TravelsListFragment.newInstance());
                return;
            case R.id.profile_friends:
                getFriendsInfo();
                return;
            case R.id.profile_see_all_preferences:
                seeAllPreferences();
                return;
            case R.id.profile_settings_button:
                openSettings();
                return;
            case R.id.profile_sign_out_button:
                signOut();
        }
    }


    //MAIN METHODS----------------------------------------------------------------------------------


    private void addToFriends(Notification notification) {
        Map<String, Object> changesLoggedUser = new HashMap<>();
        List<String> loggedUserFriends = user.getFriends() != null
                ? new ArrayList<>(user.getFriends()) : new ArrayList<>();
        loggedUserFriends.add(notification.getIdFrom());
        changesLoggedUser.put(Constants.DB_FRIENDS, loggedUserFriends);
        String successMessage = getResources().getString(R.string.messages_friends_success) + " " + notification.getUserFrom().getUsername() + "!";
        updateUser(changesLoggedUser, successMessage, getResources().getString(R.string.messages_error_failed_add_friends));

        Map<String, Object> changesFriendUser = new HashMap<>();
        List<String> friendUserFriends = notification.getUserFrom().getFriends() != null
                ? new ArrayList<>(notification.getUserFrom().getFriends()) : new ArrayList<>();
        friendUserFriends.add(notification.getIdTo());
        changesFriendUser.put(Constants.DB_FRIENDS, friendUserFriends);
        userViewModel.updateUser(notification.getUserFrom(), changesFriendUser);
    }


    private void removeNotification(Notification notification, int position) {
        Map<String, Object> changes = new HashMap<>();
        changes.put(Constants.NOTIFICATIONS.toLowerCase(), getFilteredList(user.getNotifications(), notification.getId()));
        updateUser(changes, null, getResources().getString(R.string.messages_error_failed_remove_notification));
        notificationAdapter.remove(position);
        notificationViewModel.removeNotification(notification.getId());
        if (notificationAdapter.getItemCount() == 0)
            notificationsDialog.findViewById(R.id.dialog_notifications_no_results).setVisibility(View.VISIBLE);
    }


    private void checkRequestExists() {
        if (user.getNotifications() != null && !user.getNotifications().isEmpty()) {
            AtomicBoolean exists = new AtomicBoolean(false);
            AtomicInteger counter = new AtomicInteger(0);
            for (String id : user.getNotifications()) {
                notificationViewModel.getNotificationData(id);
                notificationViewModel.getNotification().observe(getViewLifecycleOwner(), notification -> {
                    if (notification != null && notification.getIdFrom().equals(loggedUser.getUid())) {
                        exists.set(true);
                    }
                    counter.getAndIncrement();
                    if (exists.get()) {
                        showSnackBar(getResources().getString(R.string.messages_error_friend_notification_exists), Snackbar.LENGTH_LONG);
                        disableView(binding.profileFriends);
                    } else if (counter.get() == user.getNotifications().size()) {
                        sendFriendsRequest();
                    }
                });
            }
        } else {
            sendFriendsRequest();
        }
    }


    private void sendFriendsRequest() {
        startProgressBar();
        notificationViewModel.sendNotification(loggedUser, user,
                com.martynaroj.traveljournal.view.others.enums.Notification.FRIEND.ordinal());
        notificationViewModel.getNotificationResponse().observe(getViewLifecycleOwner(), status -> {
            if (status != null) {
                if (!status.contains(Constants.ERROR)) {
                    HashMap<String, Object> changes = new HashMap<>();
                    List<String> notifications = user.getNotifications() == null ? new ArrayList<>() : user.getNotifications();
                    notifications.add(status);
                    changes.put(Constants.NOTIFICATIONS.toLowerCase(), notifications);
                    disableView(binding.profileFriends);
                    updateUser(changes,
                            getResources().getString(R.string.messages_notification_sent),
                            getResources().getString(R.string.messages_error_failed_add_notification));
                } else {
                    showSnackBar(status, Snackbar.LENGTH_LONG);
                    stopProgressBar();
                }
            }
        });
    }


    private void updateUser(Map<String, Object> changes, String messageSuccess, String messageError) {
        userViewModel.updateUser(user, changes);
        userViewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                this.user = user;
                binding.setUser(user);
                if (user.isUserProfile(loggedUser)) {
                    this.loggedUser = user;
                    binding.setLoggedUser(user);
                }
                if (messageSuccess != null)
                    showSnackBar(messageSuccess, Snackbar.LENGTH_SHORT);
            } else if (messageError != null)
                showSnackBar(messageError, Snackbar.LENGTH_LONG);
            stopProgressBar();
        });
    }


    private void getContactInfo() {
        if (user != null) {
            if (loggedUser != null && user.isUserProfile(loggedUser)) {
                    showContactDialog();
            } else {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("plain/text");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{user.getEmail()});
                startActivity(Intent.createChooser(intent, ""));
            }
        }
    }


    private void openSettings() {
        Fragment settingsFragment = ProfileSettingsFragment.newInstance();
        Bundle args = new Bundle();
        args.putSerializable(Constants.USER, user);
        settingsFragment.setArguments(args);
        getNavigationInteractions().changeFragment(this, settingsFragment, true);
    }


    private void seeAllPreferences() {
        ConstraintLayout.LayoutParams constraintLayout = (ConstraintLayout.LayoutParams) binding.profilePreferences.getLayoutParams();
        String seePreferences;
        if (binding.profilePreferences.getLayoutParams().height == ConstraintLayout.LayoutParams.WRAP_CONTENT) {
            constraintLayout.height = Constants.PREFERENCES_VIEW_HEIGHT;
            seePreferences = getResources().getString(R.string.profile_see_all_pref);
        } else {
            constraintLayout.height = ConstraintLayout.LayoutParams.WRAP_CONTENT;
            seePreferences = getResources().getString(R.string.profile_see_less_pref);
        }
        binding.profileSeeAllPreferences.setPaintFlags(binding.profileSeeAllPreferences.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        binding.profileSeeAllPreferences.setText(seePreferences);
        binding.profilePreferences.setLayoutParams(constraintLayout);
    }


    private void signOut() {
        startProgressBar();
        FirebaseAuth.getInstance().signOut();
        showSnackBar(getResources().getString(R.string.messages_signing_out_success), Snackbar.LENGTH_SHORT);
        stopProgressBar();
        getNavigationInteractions().changeNavigationBarItem(2, LogInFragment.newInstance());
    }


    //OTHERS----------------------------------------------------------------------------------------


    private void changeFragment(BaseFragment next) {
        if (user.isUserProfile(loggedUser))
            getNavigationInteractions().changeFragment(this, next, true);
        else
            getNavigationInteractions().changeFragment(getParentFragment(), next, true);
    }


    private void disableView(View view) {
        view.setEnabled(false);
        view.setAlpha(0.4f);
    }


    private void startProgressBar() {
        getProgressBarInteractions().startProgressBar(binding.getRoot(), binding.profileProgressbarLayout, binding.profileProgressbar);
    }


    private void stopProgressBar() {
        getProgressBarInteractions().stopProgressBar(binding.getRoot(), binding.profileProgressbarLayout, binding.profileProgressbar);
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
