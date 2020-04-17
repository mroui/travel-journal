package com.martynaroj.traveljournal.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.martynaroj.traveljournal.services.models.Address;
import com.martynaroj.traveljournal.services.models.Day;
import com.martynaroj.traveljournal.services.models.Travel;
import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.services.respositories.PdfCreatorRepository;

import java.io.File;
import java.util.List;

public class PdfCreatorViewModel extends AndroidViewModel {

    private PdfCreatorRepository pdfCreatorRepository;
    private MutableLiveData<String> statusLiveData;

    public PdfCreatorViewModel(Application application) {
        super(application);
        pdfCreatorRepository = new PdfCreatorRepository(application.getApplicationContext());
    }

    public LiveData<String> getStatus() {
        return statusLiveData;
    }

    public void createPDF(File file, User user, Travel travel, Address destination, List<Day> days) {
        statusLiveData = pdfCreatorRepository.createPDF(file, user, travel, destination, days);
    }

}