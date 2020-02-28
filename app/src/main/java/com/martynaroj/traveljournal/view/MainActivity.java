package com.martynaroj.traveljournal.view;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

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
import com.victor.loading.rotate.RotateLoading;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationListener,
        ProgressBarListener, SnackbarListener {

    private NavigationBarAdapter adapter;
    private List<Fragment> fragmentsList = new ArrayList<>();
    private boolean backPressedOnce = false;
    private ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        User user = getUserFromIntent();
        initNavAdapter(user);
        setListeners();
        binding.bottomNavigationView.setTypeface(ResourcesCompat.getFont(this, R.font.roboto_light));
    }


    private User getUserFromIntent() {
        return (User) getIntent().getSerializableExtra(Constants.USER);
    }


    private void setListeners() {
        binding.viewPager.addOnPageChangeListener(new ViewPagerListener() {
            @Override
            public void onPageSelected(int i) {
                binding.bottomNavigationView.setCurrentActiveItem(i);
            }
        });
        binding.bottomNavigationView.setNavigationChangeListener((view, position) -> binding.viewPager.setCurrentItem(position, true));
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


    @Override
    public void onBackPressed() {
        Fragment fragment = adapter.getItem(binding.bottomNavigationView.getCurrentActiveItemPosition())
                .getChildFragmentManager().findFragmentById(R.id.fragment_profile);
        if (fragment instanceof IOnBackPressed && ((IOnBackPressed) fragment).onBackPressed()) {
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
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(() -> backPressedOnce = false, 2000);
    }


    @Override
    public void changeFragment(Fragment previous, Fragment next, Boolean addToBackStack) {
        if (previous.getView() != null) {
            FragmentTransaction fragmentTransaction = previous.getChildFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(
                    R.anim.enter_right_to_left, R.anim.exit_right_to_left,
                    R.anim.enter_left_to_right, R.anim.exit_left_to_right);
            fragmentTransaction.replace(previous.getView().getId(), next);
            if (addToBackStack) fragmentTransaction.addToBackStack(next.getClass().getName());
            fragmentTransaction.commit();
        }
    }


    @Override
    public void changeNavigationBarItem(int id, Fragment fragment) {
        if(adapter.getItem(id).getChildFragmentManager().getBackStackEntryCount() > 0)
            adapter.getItem(id).getChildFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        adapter.changeItem(id, fragment);
        adapter.notifyDataSetChanged();
    }


    protected void enableDisableViewGroup(ViewGroup viewGroup, boolean enabled) {
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = viewGroup.getChildAt(i);
            view.setEnabled(enabled);
            if (view instanceof ViewGroup) {
                enableDisableViewGroup((ViewGroup) view, enabled);
            }
        }
    }


    public void startProgressBar(View root, View progressBarLayout, RotateLoading progressBar) {
        progressBarLayout.setVisibility(View.VISIBLE);
        progressBar.start();
        enableDisableViewGroup((ViewGroup) root, false);
    }


    public void stopProgressBar(View root, View progressBarLayout, RotateLoading progressBar) {
        progressBarLayout.setVisibility(View.INVISIBLE);
        progressBar.stop();
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

}
