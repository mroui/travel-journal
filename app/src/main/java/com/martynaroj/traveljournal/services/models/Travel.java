package com.martynaroj.traveljournal.services.models;

import android.annotation.SuppressLint;
import android.widget.ImageView;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.martynaroj.traveljournal.BR;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.services.models.packing.PackingCategory;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Travel extends BaseObservable implements Serializable {

    private String id;
    private String owner;
    private String name;
    private String image;
    private Timestamp datetimeFrom;
    private Timestamp datetimeTo;
    private String destination;
    private String transport;
    private String accommodation;
    private Double budget;
    private List<String> tags;
    private List<PackingCategory> packingList;
    private boolean packing;
    private List<String> days;


    public Travel() {
        tags = new ArrayList<>();
        packingList = new ArrayList<>();
        days = new ArrayList<>();
    }


    public Travel(String id, String owner, String name, Timestamp datetimeFrom,
                  Timestamp datetimeTo, String destination, String transport, String accommodation,
                  Double budget, List<String> tags) {
        this();
        this.id = id;
        this.owner = owner;
        this.name = name;
        this.datetimeFrom = datetimeFrom;
        this.datetimeTo = datetimeTo;
        this.destination = destination;
        this.transport = transport;
        this.accommodation = accommodation;
        this.budget = budget;
        this.tags = tags;
    }


    @Bindable
    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
        notifyPropertyChanged(BR.id);
    }


    @Bindable
    public String getOwner() {
        return owner;
    }


    public void setOwner(String owner) {
        this.owner = owner;
        notifyPropertyChanged(BR.owner);
    }


    @Bindable
    public String getImage() {
        return image;
    }


    public void setImage(String image) {
        this.image = image;
        notifyPropertyChanged(BR.image);
    }


    @Bindable
    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
        notifyPropertyChanged(BR.name);
    }


    @Bindable
    public Timestamp getDatetimeFrom() {
        return datetimeFrom;
    }


    public void setDatetimeFrom(Timestamp datetimeFrom) {
        this.datetimeFrom = datetimeFrom;
        notifyPropertyChanged(BR.datetimeFrom);
    }


    @Bindable
    public Timestamp getDatetimeTo() {
        return datetimeTo;
    }


    public void setDatetimeTo(Timestamp datetimeTo) {
        this.datetimeTo = datetimeTo;
        notifyPropertyChanged(BR.datetimeTo);
    }


    @Bindable
    public String getDestination() {
        return destination;
    }


    public void setDestination(String destination) {
        this.destination = destination;
        notifyPropertyChanged(BR.destination);
    }


    @Bindable
    public String getTransport() {
        return transport;
    }


    public void setTransport(String transport) {
        this.transport = transport;
        notifyPropertyChanged(BR.transport);
    }


    @Bindable
    public String getAccommodation() {
        return accommodation;
    }


    public void setAccommodation(String accommodation) {
        this.accommodation = accommodation;
        notifyPropertyChanged(BR.accommodation);
    }


    @Bindable
    public Double getBudget() {
        return budget;
    }


    public void setBudget(Double budget) {
        this.budget = budget;
        notifyPropertyChanged(BR.budget);
    }


    @Bindable
    public List<String> getTags() {
        return tags;
    }


    public void setTags(List<String> tags) {
        this.tags = tags;
        notifyPropertyChanged(BR.tags);
    }


    @Bindable
    public List<String> getDays() {
        return days;
    }


    public void setDays(List<String> days) {
        this.days = days;
        notifyPropertyChanged(BR.days);
    }


    @Bindable
    public List<PackingCategory> getPackingList() {
        return packingList;
    }

    public void setPackingList(List<PackingCategory> packingList) {
        this.packingList = packingList;
        notifyPropertyChanged(BR.packingList);
    }


    @Bindable
    public boolean isPacking() {
        return packing;
    }


    public void setPacking(boolean packing) {
        this.packing = packing;
        notifyPropertyChanged(BR.packing);
    }


    public long whatDay() {
        Calendar now = Calendar.getInstance();
        Calendar startDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();

        startDate.setTimeInMillis(datetimeFrom.getSeconds() * 1000);
        startDate.set(
                startDate.get(Calendar.YEAR),
                startDate.get(Calendar.MONTH),
                startDate.get(Calendar.DAY_OF_MONTH),
                0, 0, 0
        );
        startDate.set(Calendar.MILLISECOND, 0);

        now.set(
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH),
                0, 0, 0
        );
        now.set(Calendar.MILLISECOND, 0);

        endDate.setTimeInMillis(datetimeTo.getSeconds() * 1000);
        endDate.set(
                endDate.get(Calendar.YEAR),
                endDate.get(Calendar.MONTH),
                endDate.get(Calendar.DAY_OF_MONTH),
                0, 0, 0
        );
        endDate.set(Calendar.MILLISECOND, 0);

        long differenceFrom = now.getTimeInMillis() - startDate.getTimeInMillis();
        long daysFrom = TimeUnit.DAYS.convert(differenceFrom, TimeUnit.MILLISECONDS) + 1;

        long differenceTo = endDate.getTimeInMillis() - now.getTimeInMillis();
        long daysTo = TimeUnit.DAYS.convert(differenceTo, TimeUnit.MILLISECONDS) + 1;

        if (daysFrom < 1)
            return com.martynaroj.traveljournal.view.others.enums.Travel.BEFORE.value;
        else if (daysTo < 1)
            return com.martynaroj.traveljournal.view.others.enums.Travel.AFTER.value;
        else
            return daysFrom;
    }


    public String getDaysRemainsString() {
        long days = whatDay();
        if (days >= 1)
            return days + Constants.DAY;
        else if (days == com.martynaroj.traveljournal.view.others.enums.Travel.BEFORE.value) {
            return Constants.STARTS_ON + getDateString(datetimeFrom.getSeconds() * 1000);
        } else if (days == com.martynaroj.traveljournal.view.others.enums.Travel.AFTER.value) {
            return Constants.ENDS_ON + getDateString(datetimeTo.getSeconds() * 1000);
        }
        return "";
    }


    @SuppressLint("SimpleDateFormat")
    private String getDateString(long time) {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(time);
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        return format.format(date.getTime());
    }


    @SuppressLint("SimpleDateFormat")
    private String getTimeString(long time) {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(time);
        SimpleDateFormat format = new SimpleDateFormat("HH:mm aa");
        return format.format(date.getTime());
    }


    public String getDateTimeString(Timestamp timestamp) {
        long milisec = timestamp.toDate().getTime();
        return getDateString(milisec) + ", " + getTimeString(milisec);
    }


    @BindingAdapter("travelUrl")
    public static void loadImage(ImageView v, String imgUrl){
        Glide.with(v.getContext())
                .load(imgUrl)
                .fitCenter()
                .placeholder(R.drawable.no_image)
                .centerCrop()
                .into(v);
    }

}
