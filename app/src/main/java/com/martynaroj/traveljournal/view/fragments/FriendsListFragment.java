package com.martynaroj.traveljournal.view.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.DialogCustomBinding;
import com.martynaroj.traveljournal.databinding.FragmentFriendsListBinding;
import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.view.adapters.UserAdapter;
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.view.others.classes.DialogHandler;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;
import com.martynaroj.traveljournal.viewmodels.UserViewModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendsListFragment extends BaseFragment {

    private FragmentFriendsListBinding binding;
    private UserViewModel userViewModel;
    private User user;
    private User loggedUser;
    private UserAdapter adapter;


    public FriendsListFragment() {
        super();
    }


    public static FriendsListFragment newInstance(User user) {
        FriendsListFragment fragment = new FriendsListFragment();
        Bundle args = new Bundle();
        args.putSerializable(Constants.BUNDLE_USER, user);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user = (User) getArguments().getSerializable(Constants.BUNDLE_USER);
        }
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFriendsListBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initViewModels();
        setListeners();

        initLoggedUser();
        initFriends();

        return view;
    }


    //INIT DATA-------------------------------------------------------------------------------------


    private void initViewModels() {
        if (getActivity() != null) {
            userViewModel = new ViewModelProvider(getActivity()).get(UserViewModel.class);
        }
    }


    private void initLoggedUser() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            startProgressBar();
            userViewModel.getUserData(firebaseAuth.getCurrentUser().getUid());
            userViewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
                if (user != null) {
                    this.loggedUser = user;
                    initTitle();
                } else
                    showSnackBar(getResources().getString(R.string.messages_error_current_user_not_available), Snackbar.LENGTH_LONG);
                stopProgressBar();
            });
        }
    }


    @SuppressLint("SetTextI18n")
    private void initTitle() {
        if (user != null && loggedUser != null) {
            if (user.isUserProfile(loggedUser))
                binding.friendsListTitle.setText("My " + getResources().getString(R.string.friends_list_title));
            else
                binding.friendsListTitle.setText(user.getUsername() + "\'s\n" + getResources().getString(R.string.friends_list_title));
        }
    }


    private void initFriends() {
        if (user != null && user.getFriends() != null && !user.getFriends().isEmpty()) {
            startProgressBar();
            binding.friendsListMessage.setVisibility(View.INVISIBLE);
            userViewModel.getUsersListData(user.getFriends());
            userViewModel.getUsersList().observe(getViewLifecycleOwner(), users -> {
                if (users != null)
                    initFriendsList(users);
                stopProgressBar();
            });
        } else {
            binding.friendsListRecyclerView.setVisibility(View.INVISIBLE);
            binding.friendsListMessage.setVisibility(View.VISIBLE);
        }
    }


    private void initFriendsList(List<User> friends) {
        adapter = new UserAdapter(getContext(), friends, user.isUserProfile(loggedUser));
        binding.friendsListRecyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener((object, position, view) -> {
            User userItem = (User) object;
            if (userItem != null) {
                switch (view.getId()) {
                    case R.id.user_item:
                        if (loggedUser != null && loggedUser.isUserProfile(userItem))
                            showSnackBar(getResources().getString(R.string.messages_its_you), Snackbar.LENGTH_SHORT);
                        else
                            changeFragment(ProfileFragment.newInstance(userItem));
                        break;
                    case R.id.user_item_delete_button:
                        showDeleteDialog(userItem, position);
                        break;
                }
            }
        });
    }


    //LISTENERS-------------------------------------------------------------------------------------


    private void setListeners() {
        binding.friendsListArrowButton.setOnClickListener(view -> back());
    }


    //DIALOG----------------------------------------------------------------------------------------


    @SuppressLint("SetTextI18n")
    private void showDeleteDialog(User user, int position) {
        if (getContext() != null) {
            Dialog dialog = DialogHandler.createDialog(getContext(), true);
            DialogCustomBinding binding = DialogCustomBinding.inflate(LayoutInflater.from(getContext()));
            dialog.setContentView(binding.getRoot());
            DialogHandler.initContent(getContext(), binding.dialogCustomTitle, R.string.dialog_remove_friends_title,
                    binding.dialogCustomDesc, R.string.dialog_remove_friends_desc,
                    binding.dialogCustomButtonPositive, R.string.dialog_button_yes,
                    binding.dialogCustomButtonNegative, R.string.dialog_button_no,
                    R.color.main_blue, R.color.blue_bg_lighter);
            binding.dialogCustomDesc.setText(getResources().getString(
                    R.string.dialog_remove_friends_desc) + " " + user.getUsername() + "?"
            );
            binding.dialogCustomButtonPositive.setOnClickListener(v -> {
                dialog.dismiss();
                removeFriend(user, position);
            });
            binding.dialogCustomButtonNegative.setOnClickListener(v -> dialog.dismiss());
            dialog.show();
        }
    }


    //FRIENDS---------------------------------------------------------------------------------------


    private void removeFriend(User friend, int position) {
        startProgressBar();

        Map<String, Object> changesFriendUser = new HashMap<>();
        friend.getFriends().remove(loggedUser.getUid());
        changesFriendUser.put(Constants.DB_FRIENDS, friend.getFriends());
        userViewModel.updateUser(true, friend, changesFriendUser);

        Map<String, Object> changesLoggedUser = new HashMap<>();
        loggedUser.getFriends().remove(friend.getUid());
        changesLoggedUser.put(Constants.DB_FRIENDS, loggedUser.getFriends());

        updateUser(changesLoggedUser, position);
    }


    private void updateUser(Map<String, Object> changesLoggedUser, int position) {
        userViewModel.updateUser(true, loggedUser, changesLoggedUser);
        userViewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                this.user = user;
                this.loggedUser = user;
                userViewModel.setUser(user);
                adapter.remove(position);
                if (adapter.getItemCount() == 0) {
                    binding.friendsListRecyclerView.setVisibility(View.INVISIBLE);
                    binding.friendsListMessage.setVisibility(View.VISIBLE);
                }
                showSnackBar(getResources().getString(R.string.messages_remove_friend_success), Snackbar.LENGTH_SHORT);
            } else
                showSnackBar(getResources().getString(R.string.messages_error_failed_remove_friend), Snackbar.LENGTH_LONG);
            stopProgressBar();
        });
    }


    //OTHERS----------------------------------------------------------------------------------------


    private void back() {
        if (getParentFragmentManager().getBackStackEntryCount() > 0)
            getParentFragmentManager().popBackStack();
    }


    private void changeFragment(BaseFragment next) {
        getNavigationInteractions().changeFragment(getParentFragment(), next, true);
    }


    private void startProgressBar() {
        getProgressBarInteractions().startProgressBar(binding.getRoot(),
                binding.friendsListProgressbarLayout, binding.friendsListProgressbar);
    }


    private void stopProgressBar() {
        getProgressBarInteractions().stopProgressBar(binding.getRoot(),
                binding.friendsListProgressbarLayout, binding.friendsListProgressbar);
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
