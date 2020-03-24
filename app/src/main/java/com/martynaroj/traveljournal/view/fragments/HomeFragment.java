package com.martynaroj.traveljournal.view.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.FragmentHomeBinding;
import com.martynaroj.traveljournal.view.base.BaseFragment;

public class HomeFragment extends BaseFragment implements View.OnClickListener {

    private FragmentHomeBinding binding;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        setListeners();
        initExploreTravelsAdapter();

        return view;
    }


    //INIT DATA-------------------------------------------------------------------------------------


    private void initExploreTravelsAdapter() {
        //todo adapter, when finish travel class
//        List<Travel> travels = new ArrayList<>();
//        travels.add(new Travel(R.drawable.default_avatar, "title1", "desc"));
//        travels.add(new Travel(R.drawable.default_avatar, "title2", "desc"));
//        travels.add(new Travel(R.drawable.default_avatar, "title3", "desc"));
//        travels.add(new Travel(R.drawable.default_avatar, "title4", "desc"));
//        travels.add(new Travel(R.drawable.default_avatar, "title5", "desc"));

//        ExploreTravelsAdapter adapter = new ExploreTravelsAdapter(getContext(), travels, true);
//        binding.homeExploreViewpager.setAdapter(adapter);
//        binding.homeExploreViewpager.setPadding(75, 0, 75, 0);
    }


    //LISTENERS-------------------------------------------------------------------------------------


    private void setListeners() {
        binding.homeSearchFriendsButton.setOnClickListener(this);
        binding.homeExploreMapButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.home_search_friends_button:
                if (isLoggedUser())
                    changeFragment(SearchFriendsFragment.newInstance());
                else
                    showSnackBar(getResources().getString(R.string.messages_not_logged_user), Snackbar.LENGTH_LONG);
                break;
            case R.id.home_explore_map_button:
                changeFragment(PlanToVisitFragment.newInstance());
                break;
        }
    }


    //OTHERS----------------------------------------------------------------------------------------


    private boolean isLoggedUser() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }


    private void changeFragment(Fragment next) {
        getNavigationInteractions().changeFragment(this, next, true);
    }


    private void showSnackBar(String message, int duration) {
        getSnackBarInteractions().showSnackBar(binding.getRoot(), getActivity(), message, duration);
    }


    @Override
    public void onResume() {
        super.onResume();
        binding.homeExploreViewpager.resumeAutoScroll();
    }


    @Override
    public void onPause() {
        binding.homeExploreViewpager.pauseAutoScroll();
        super.onPause();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
