package com.martynaroj.traveljournal.view.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.FragmentFriendsListBinding;
import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.view.adapters.UserAdapter;
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.viewmodels.UserViewModel;

import java.util.List;

public class FriendsListFragment extends BaseFragment implements View.OnClickListener {

    private FragmentFriendsListBinding binding;
    private UserViewModel userViewModel;
    private User user;
    private User loggedUser;


    static FriendsListFragment newInstance(User user) {
        return new FriendsListFragment(user);
    }


    public FriendsListFragment() {
        super();
    }


    private FriendsListFragment(User user) {
        super();
        this.user = user;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFriendsListBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initViewModels();
        setListeners();

        initUser();
        initLoggedUser();

        initFriends();

        return view;
    }


    private void initViewModels() {
        if (getActivity() != null) {
            userViewModel = new ViewModelProvider(getActivity()).get(UserViewModel.class);
        }
    }


    private void setListeners() {
        binding.friendsListArrowButton.setOnClickListener(this);
    }


    private void initUser() {
        if (user == null) {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
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
                } else {
                    showSnackBar(getResources().getString(R.string.messages_error_current_user_not_available), Snackbar.LENGTH_LONG);
                }
                stopProgressBar();
            });
        }
    }


    private void initFriends() {
        if (user != null && user.getFriends() != null && !user.getFriends().isEmpty()) {
            startProgressBar();
            binding.friendsListMessage.setVisibility(View.INVISIBLE);
            userViewModel.getUsersListData(user.getFriends());
            userViewModel.getUsersList().observe(getViewLifecycleOwner(), users -> {
                if (users != null) {
                    initFriendsList(users);
                    stopProgressBar();
                }
            });
        } else {
            binding.friendsListRecyclerView.setVisibility(View.INVISIBLE);
        }
    }


    private void initFriendsList(List<User> friends) {
        UserAdapter adapter = new UserAdapter(getContext(), friends);
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
                }
            }
        });
    }


    private void changeFragment(BaseFragment next) {
        if (user.isUserProfile(loggedUser))
            getNavigationInteractions().changeFragment(this, next, true);
        else
            getNavigationInteractions().changeFragment(getParentFragment(), next, true);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.friends_list_arrow_button:
                if (getParentFragmentManager().getBackStackEntryCount() > 0)
                    getParentFragmentManager().popBackStack();
        }
    }


    private void startProgressBar() {
        getProgressBarInteractions().startProgressBar(binding.getRoot(), binding.friendsListProgressbarLayout, binding.friendsListProgressbar);
    }


    private void stopProgressBar() {
        getProgressBarInteractions().stopProgressBar(binding.getRoot(), binding.friendsListProgressbarLayout, binding.friendsListProgressbar);
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
