package com.martynaroj.traveljournal.View;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.View.Adapters.NavigationBarAdapter;
import com.martynaroj.traveljournal.View.Fragments.BoardFragment;
import com.martynaroj.traveljournal.View.Fragments.HomeFragment;
import com.martynaroj.traveljournal.View.Fragments.LogInFragment;
import com.martynaroj.traveljournal.View.Interfaces.NavigationListener;
import com.martynaroj.traveljournal.View.Others.ViewPagerListener;
import com.martynaroj.traveljournal.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationListener {

    private NavigationBarAdapter adapter;
    private List<Fragment> fragmentsList = new ArrayList<>();
    private boolean backPressedOnce = false;
    private ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initNavAdapter();
        setListeners();
        binding.bottomNavigationView.setTypeface(ResourcesCompat.getFont(this, R.font.roboto_light));
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


    private void initNavAdapter() {
        fragmentsList.add(0, HomeFragment.newInstance());
        fragmentsList.add(1, BoardFragment.newInstance());
        fragmentsList.add(2, LogInFragment.newInstance());
        adapter = new NavigationBarAdapter(fragmentsList, getSupportFragmentManager());
        binding.viewPager.setAdapter(adapter);
    }


    @Override
    public void onBackPressed() {
        if (adapter.getItem(binding.bottomNavigationView.getCurrentActiveItemPosition()).getChildFragmentManager().getBackStackEntryCount() >= 1) {
            adapter.getItem(binding.bottomNavigationView.getCurrentActiveItemPosition()).getChildFragmentManager().popBackStack();
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
            if (addToBackStack) fragmentTransaction.addToBackStack(next.getTag());
            fragmentTransaction.commit();
        }
    }


    @Override
    public void changeNavigationBarItem(int id, Fragment fragment) {
        adapter.getItem(id).getChildFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        adapter.changeItem(id, fragment);
        adapter.notifyDataSetChanged();
    }
}
