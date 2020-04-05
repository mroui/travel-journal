package com.martynaroj.traveljournal.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.martynaroj.traveljournal.services.models.Day;
import com.martynaroj.traveljournal.services.respositories.DayRepository;

import java.util.List;

public class DayViewModel extends AndroidViewModel {

    private DayRepository dayRepository;
    private LiveData<List<Day>> daysLiveData;

    public DayViewModel(@NonNull Application application) {
        super(application);
        dayRepository = new DayRepository(application.getApplicationContext());
    }

    public String generateId() {
        return dayRepository.generateId();
    }

}