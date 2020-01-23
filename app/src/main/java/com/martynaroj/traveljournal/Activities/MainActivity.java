package com.martynaroj.traveljournal.Activities;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.gauravk.bubblenavigation.BubbleNavigationLinearView;
import com.gauravk.bubblenavigation.listener.BubbleNavigationChangeListener;
import com.martynaroj.traveljournal.Adapters.NavigationBarAdapter;
import com.martynaroj.traveljournal.Fragments.BoardFragment;
import com.martynaroj.traveljournal.Fragments.HomeFragment;
import com.martynaroj.traveljournal.Fragments.ProfileFragment;
import com.martynaroj.traveljournal.R;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private BubbleNavigationLinearView navigation;
    private ViewPager viewPager;
    private NavigationBarAdapter adapter;
    private List<Fragment> fragmentsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();
        setListeners();
        viewPager.setAdapter(adapter);
        navigation.setTypeface(ResourcesCompat.getFont(this, R.font.roboto_light));
    }


    private void setListeners() {
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
            }

            @Override
            public void onPageSelected(int i) {
                navigation.setCurrentActiveItem(i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });

        navigation.setNavigationChangeListener(new BubbleNavigationChangeListener() {
            @Override
            public void onNavigationChanged(View view, int position) {
                viewPager.setCurrentItem(position, true);
            }
        });
    }


    private void findViews() {
        fragmentsList.add(0, new HomeFragment());
        fragmentsList.add(1, new BoardFragment());
        fragmentsList.add(2, new ProfileFragment());
        navigation = findViewById(R.id.bottom_navigation_view);
        viewPager = findViewById(R.id.view_pager);
        adapter = new NavigationBarAdapter(fragmentsList, getSupportFragmentManager());
    }

}
