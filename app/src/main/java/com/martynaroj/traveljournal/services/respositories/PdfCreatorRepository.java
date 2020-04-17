package com.martynaroj.traveljournal.services.respositories;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import com.martynaroj.traveljournal.services.models.Address;
import com.martynaroj.traveljournal.services.models.Day;
import com.martynaroj.traveljournal.services.models.Travel;
import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.view.others.classes.PDFCreator;

import java.io.File;
import java.util.List;

public class PdfCreatorRepository {

    private Context context;

    public PdfCreatorRepository(Context context) {
        this.context = context;
    }

    public MutableLiveData<String> createPDF(File file, User user, Travel travel, Address destination, List<Day> days) {
        MutableLiveData<String> statusData = new MutableLiveData<>();
        new PDFCreator(context, file, user, travel, destination, days, statusData::setValue).execute();
        return statusData;
    }

}
