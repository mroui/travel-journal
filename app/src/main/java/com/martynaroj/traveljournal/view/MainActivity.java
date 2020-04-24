package com.martynaroj.traveljournal.view;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.ActivityMainBinding;
import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.view.adapters.NavigationBarAdapter;
import com.martynaroj.traveljournal.view.fragments.BoardFragment;
import com.martynaroj.traveljournal.view.fragments.HomeFragment;
import com.martynaroj.traveljournal.view.fragments.LogInFragment;
import com.martynaroj.traveljournal.view.fragments.ProfileFragment;
import com.martynaroj.traveljournal.view.interfaces.IOnBackPressed;
import com.martynaroj.traveljournal.view.interfaces.NavigationListener;
import com.martynaroj.traveljournal.view.interfaces.ProgressBarListener;
import com.martynaroj.traveljournal.view.interfaces.SnackbarListener;
import com.martynaroj.traveljournal.view.others.classes.ViewPagerListener;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;
import com.martynaroj.traveljournal.viewmodels.UserViewModel;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationListener,
        ProgressBarListener, SnackbarListener {

    private NavigationBarAdapter adapter;
    private List<Fragment> fragmentsList;
    private boolean backPressedOnce = false;
    private ActivityMainBinding binding;

    private UserViewModel userViewModel;
    private User user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initViewModels();
        initContentData();
        setListeners();
    }


    //INIT DATA-------------------------------------------------------------------------------------


    private void initViewModels() {
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
    }


    private void initContentData() {
        fragmentsList = new ArrayList<>();
        binding.bottomNavigationView.setTypeface(ResourcesCompat.getFont(this, R.font.roboto_light));
        initNavAdapter(getUserFromIntent());
    }


    private User getUserFromIntent() {
        this.user = (User) getIntent().getSerializableExtra(Constants.USER);
        userViewModel.setUser(this.user);
        return this.user;
    }


    private void initNavAdapter(User user) {
        fragmentsList.add(0, HomeFragment.newInstance());
        fragmentsList.add(1, BoardFragment.newInstance());
        if (user != null)
            fragmentsList.add(2, ProfileFragment.newInstance(user));
        else
            fragmentsList.add(2, LogInFragment.newInstance());
        adapter = new NavigationBarAdapter(fragmentsList, getSupportFragmentManager());
        binding.viewPager.setAdapter(adapter);
    }


    //LISTENERS-------------------------------------------------------------------------------------


    private void setListeners() {
        binding.viewPager.addOnPageChangeListener(new ViewPagerListener() {
            @Override
            public void onPageSelected(int i) {
                binding.bottomNavigationView.setCurrentActiveItem(i);
            }
        });
        binding.bottomNavigationView.setNavigationChangeListener((view, position) ->
                binding.viewPager.setCurrentItem(position, true));
    }


    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = adapter.getItem(binding.bottomNavigationView
                .getCurrentActiveItemPosition()).getChildFragmentManager();

        Fragment profileFragment = fragmentManager.findFragmentById(R.id.fragment_profile);
        if (profileFragment instanceof IOnBackPressed && ((IOnBackPressed) profileFragment).onBackPressed())
            return;

        Fragment exploreMapFragment = fragmentManager.findFragmentById(R.id.fragment_home);
        if (exploreMapFragment instanceof IOnBackPressed && ((IOnBackPressed) exploreMapFragment).onBackPressed())
            return;

        Fragment boardFragment = fragmentManager.findFragmentById(R.id.fragment_board);
        if (boardFragment instanceof IOnBackPressed && ((IOnBackPressed) boardFragment).onBackPressed())
            return;
        else if (boardFragment != null){
            Fragment detailsFragment = boardFragment.getChildFragmentManager().findFragmentById(R.id.fragment_details);
            if (detailsFragment instanceof IOnBackPressed && ((IOnBackPressed) detailsFragment).onBackPressed())
                return;
        }

        if (adapter.getItem(binding.bottomNavigationView.getCurrentActiveItemPosition())
                .getChildFragmentManager().getBackStackEntryCount() >= 1) {
            adapter.getItem(binding.bottomNavigationView.getCurrentActiveItemPosition())
                    .getChildFragmentManager().popBackStack();
            return;
        }

        if (backPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.backPressedOnce = true;
        showSnackBar(binding.getRoot(), this, getResources().getString(R.string.messages_click_back_to_exit), Snackbar.LENGTH_SHORT);
        new Handler().postDelayed(() -> backPressedOnce = false, 2000);
    }


    //OTHERS----------------------------------------------------------------------------------------


    @Override
    public void changeFragment(Fragment previous, Fragment next, Boolean addToBackStack) {
        if (previous.getView() != null) {
            FragmentTransaction fragmentTransaction = previous.getChildFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(
                    R.anim.enter_right_to_left, R.anim.exit_right_to_left,
                    R.anim.enter_left_to_right, R.anim.exit_left_to_right);
            fragmentTransaction.replace(previous.getView().getId(), next);
            if (addToBackStack)
                fragmentTransaction.addToBackStack(next.getClass().getName());
            fragmentTransaction.commit();
        }
    }


    @Override
    public void changeNavigationBarItem(int id, Fragment fragment) {
        if (adapter.getItem(id).getChildFragmentManager().getBackStackEntryCount() > 0)
            adapter.getItem(id).getChildFragmentManager().popBackStackImmediate(null,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE);
        adapter.changeItem(id, fragment);
        adapter.notifyDataSetChanged();
    }


    protected void enableDisableViewGroup(ViewGroup viewGroup, boolean enabled) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View view = viewGroup.getChildAt(i);
            view.setEnabled(enabled);
            if (view instanceof ViewGroup)
                enableDisableViewGroup((ViewGroup) view, enabled);
        }
    }


    public void startProgressBar(View root, View progressBarLayout, ProgressBar progressBar) {
        progressBarLayout.setVisibility(View.VISIBLE);
        enableDisableViewGroup((ViewGroup) root, false);
    }


    public void stopProgressBar(View root, View progressBarLayout, ProgressBar progressBar) {
        progressBarLayout.setVisibility(View.INVISIBLE);
        enableDisableViewGroup((ViewGroup) root, true);
    }


    @Override
    public void showSnackBar(View root, Activity activity, String message, int duration) {
        Snackbar snackbar = Snackbar.make(root, message, duration);
        snackbar.setAnchorView(activity.findViewById(R.id.bottom_navigation_view));
        TextView textView = snackbar.getView().findViewById(R.id.snackbar_text);
        textView.setMaxLines(3);
        snackbar.show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
