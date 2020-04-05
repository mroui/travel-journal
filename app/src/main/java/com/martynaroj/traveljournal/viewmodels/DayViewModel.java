package com.martynaroj.traveljournal.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.martynaroj.traveljournal.services.models.Day;
import com.martynaroj.traveljournal.services.respositories.DayRepository;
import com.martynaroj.traveljournal.view.others.enums.Status;

import java.util.List;

public class DayViewModel extends AndroidViewModel {

    private DayRepository dayRepository;
    private LiveData<List<Day>> daysLiveData;
    private LiveData<Status> statusLiveData;

    public DayViewModel(@NonNull Application application) {
        super(application);
        dayRepository = new DayRepository(application.getApplicationContext());
    }

    public String generateId() {
        return dayRepository.generateId();
    }

    public void getDaysListData(List<String> daysIds) {
        daysLiveData = dayRepository.getDays(daysIds);
    }

    public LiveData<List<Day>> getDaysList() {
        return daysLiveData;
    }

    public void addDay(Day day) {
        statusLiveData = dayRepository.addDay(day);
    }

    public LiveData<Status> getStatusData() {
        return statusLiveData;
    }

}