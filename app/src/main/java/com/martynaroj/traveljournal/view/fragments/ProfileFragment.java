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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    private RecyclerView notificationsRecyclerView;

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
        getLoggedUser();

        return view;
    }

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


    private void observeUserChanges() {
        userViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                this.user = user;
                binding.setUser(user);
                initPreferences();
                initLocalization();
            }
        });
    }


    private void getLoggedUser() {
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


    private void getLocationCity(Address address) {
        try {
            List<android.location.Address> addresses = geocoder.getFromLocation(address.getLatitude(), address.getLongitude(), 1);
            if (addresses != null && addresses.size() > 0) {
                binding.setLocation(addresses.get(0).getLocality() + ", " + addresses.get(0).getCountryName());
            }
        } catch (IOException ignored) {}
    }


    private void initPreferences() {
        if (user != null && user.getPreferences() != null) {
            binding.profilePreferences.setData(user.getPreferences(), item -> {
                SpannableString spannableString = new SpannableString(item);
                spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#000000")), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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
                openTravels();
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


    private void showNotificationsDialog(List<Notification> notifications) {
        if (getContext() != null) {
            notificationsDialog = new Dialog(getContext());
            notificationsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            notificationsDialog.setCancelable(true);
            notificationsDialog.setContentView(R.layout.dialog_notifications);
            notificationsDialog.findViewById(R.id.dialog_notifications_ok_button).setOnClickListener(v -> notificationsDialog.dismiss());

            if (!notifications.isEmpty()) {
                notificationsDialog.findViewById(R.id.dialog_notifications_no_results).setVisibility(View.INVISIBLE);
                setNotificationsRecyclerView(notifications);
            } else {
                notificationsDialog.findViewById(R.id.dialog_notifications_recycler_view).setVisibility(View.INVISIBLE);
            }

            notificationsDialog.show();
        }
    }


    private void setNotificationsRecyclerView(List<Notification> notifications) {
        notificationsRecyclerView = notificationsDialog.findViewById(R.id.dialog_notifications_recycler_view);
        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        setNotificationsAdapter(notifications);
    }


    private void setNotificationsAdapter(List<Notification> notifications) {
        NotificationAdapter adapter = new NotificationAdapter(getContext(), notifications);
        notificationsRecyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener((object, position, view) -> {
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


    private void addToFriends(Notification notification) {
        //TODO: add to friends userFrom & userTo
    }


    private void removeNotification(Notification notification, int position) {
        //TODO: remove from user's notifications list & from notifications collection
        //notificationViewModel.removeNotification();
        //userViewModel.updateUser();
    }


    private void changeFragment(ProfileFragment next) {
        getNavigationInteractions().changeFragment(this, next, true);
    }


    private void getFriendsInfo() {
        if (loggedUser != null && user != null) {
            if (user.getUid().equals(loggedUser.getUid())) {
                getNavigationInteractions().changeFragment(this, FriendsListFragment.newInstance(), true);
            } else if (user.hasFriend(loggedUser)) {
                getNavigationInteractions().changeFragment(getParentFragment(), FriendsListFragment.newInstance(), true);
            } else {
                checkRequestExists();
            }
        }
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
                        disableFriendButton();
                    } else if (counter.get() == user.getNotifications().size()) {
                        sendFriendsRequest();
                    }
                });
            }
        } else {
            sendFriendsRequest();
        }
    }


    private void disableFriendButton() {
        binding.profileFriends.setEnabled(false);
        binding.profileFriends.setAlpha(0.4f);
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
                    disableFriendButton();
                    updateUser(changes);
                } else {
                    showSnackBar(status, Snackbar.LENGTH_LONG);
                    stopProgressBar();
                }
            }
        });
    }


    private void updateUser(Map<String, Object> changes) {
        userViewModel.updateUser(user, changes);
        userViewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                this.user = user;
                binding.setUser(user);
                showSnackBar(getResources().getString(R.string.messages_notification_sent), Snackbar.LENGTH_SHORT);
            } else {
                showSnackBar(getResources().getString(R.string.messages_error_failed_add_notification), Snackbar.LENGTH_LONG);
            }
            stopProgressBar();
        });
    }


    private void getContactInfo() {
        if (user != null) {
            if (loggedUser != null && user.isUserProfile(loggedUser)) {
                if (getContext() != null) {
                    Dialog dialog = new Dialog(getContext());
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setCancelable(true);
                    dialog.setContentView(R.layout.dialog_contact);
                    dialog.findViewById(R.id.dialog_contact_ok_button).setOnClickListener(v -> dialog.dismiss());
                    TextView email = dialog.findViewById(R.id.dialog_contact_desc);
                    email.setText(user.getEmail());
                    dialog.show();
                }
            } else {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("plain/text");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{user.getEmail()});
                startActivity(Intent.createChooser(intent, ""));
            }
        }
    }


    private void openTravels() {
        getNavigationInteractions().changeFragment(this, TravelsListFragment.newInstance(), true);
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
