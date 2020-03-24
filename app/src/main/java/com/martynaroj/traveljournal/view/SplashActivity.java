package com.martynaroj.traveljournal.view;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.services.retrofit.Rest;
import com.martynaroj.traveljournal.viewmodels.SplashViewModel;

import static com.martynaroj.traveljournal.view.others.interfaces.Constants.USER;

public class SplashActivity extends AppCompatActivity {

    private SplashViewModel splashViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initSplashViewModel();
        checkCurrentUserAuth();
        initServices();
    }


    //INIT DATA-------------------------------------------------------------------------------------


    private void initSplashViewModel() {
        splashViewModel = new ViewModelProvider(this).get(SplashViewModel.class);
    }


    private void checkCurrentUserAuth() {
        splashViewModel.checkCurrentUserAuth();
        splashViewModel.getIsUserAuthLiveData().observe(this, user -> {
            if (user.isAuthenticated()) {
                getUserFromDatabase(user.getData().getUid());
            } else {
                startMainActivity(null);
                finish();
            }
        });
    }


    private void initServices() {
        Rest.initPlaces();
        Rest.initWeather();
        Rest.initTranslator();
        Rest.initCurrencyConverter();
    }


    private void getUserFromDatabase(String uid) {
        splashViewModel.setUid(uid);
        splashViewModel.getUserLiveData().observe(this, user -> {
            startMainActivity(user.getData());
            finish();
        });
    }


    //OTHERS----------------------------------------------------------------------------------------


    private void startMainActivity(User user) {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        intent.putExtra(USER, user);
        startActivity(intent);
    }

}