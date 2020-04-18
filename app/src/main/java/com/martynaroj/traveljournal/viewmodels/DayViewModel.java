package com.martynaroj.traveljournal.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.martynaroj.traveljournal.services.models.Day;
import com.martynaroj.traveljournal.services.respositories.DayRepository;
import com.martynaroj.traveljournal.view.others.enums.Status;

import java.util.List;
import java.util.Map;

public class DayViewModel extends AndroidViewModel {

    private DayRepository dayRepository;
    private LiveData<List<Day>> daysLiveData;
    private LiveData<Status> statusLiveData;
    private MutableLiveData<Day> today;
    private MutableLiveData<List<Day>> days;

    public DayViewModel(@NonNull Application application) {
        super(application);
        dayRepository = new DayRepository(application.getApplicationContext());
        today = new MutableLiveData<>();
        days = new MutableLiveData<>();
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

    public void updateDay(String id, Map<String, Object> changes) {
        dayRepository.updateDay(id, changes);
    }

    public void setToday(Day day) {
        this.today.setValue(day);
    }

    public LiveData<Day> getToday() {
        return today;
    }

    public void setDays(List<Day> days) {
        this.days.setValue(days);
    }

    public LiveData<List<Day>> getDays() {
        return days;
    }

    public void removeDay(String id) {
        dayRepository.removeDay(id);
    }

}