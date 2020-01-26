package com.martynaroj.traveljournal;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.gauravk.bubblenavigation.listener.BubbleNavigationChangeListener;
import com.martynaroj.traveljournal.Adapters.NavigationBarAdapter;
import com.martynaroj.traveljournal.Fragments.BoardFragment;
import com.martynaroj.traveljournal.Fragments.HomeFragment;
import com.martynaroj.traveljournal.Fragments.ProfileFragment;
import com.martynaroj.traveljournal.Interfaces.NavigationListener;
import com.martynaroj.traveljournal.Others.ViewPagerListener;
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
        binding.bottomNavigationView.setNavigationChangeListener(new BubbleNavigationChangeListener() {
            @Override
            public void onNavigationChanged(View view, int position) {
                binding.viewPager.setCurrentItem(position, true);
            }
        });
    }


    private void initNavAdapter() {
        fragmentsList.add(0, HomeFragment.newInstance());
        fragmentsList.add(1, BoardFragment.newInstance());
        fragmentsList.add(2, ProfileFragment.newInstance());
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
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                backPressedOnce = false;
            }
        }, 2000);
    }


    @Override
    public void changeFragment(Fragment previous, Fragment next, Boolean addToBackStack) {
        if (previous.getView() != null) {
            FragmentTransaction fragmentTransaction = previous.getChildFragmentManager().beginTransaction();
            fragmentTransaction.replace(previous.getView().getId(), next);
            if (addToBackStack) fragmentTransaction.addToBackStack(next.getTag());
            fragmentTransaction.commit();
        }
    }
}
