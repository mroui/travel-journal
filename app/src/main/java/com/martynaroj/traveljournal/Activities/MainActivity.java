package com.martynaroj.traveljournal.Activities;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import com.gauravk.bubblenavigation.BubbleNavigationLinearView;
import com.martynaroj.traveljournal.R;

public class MainActivity extends AppCompatActivity {

    private BubbleNavigationLinearView bottomNavigationBar;
    private ViewPager fragmentsViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setViews();
    }

    private void setViews() {
        bottomNavigationBar = findViewById(R.id.bottom_navigation_view);
        fragmentsViewPager = findViewById(R.id.view_pager);
    }


}
