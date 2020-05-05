package com.martynaroj.traveljournal.services.models;

import android.annotation.SuppressLint;
import android.widget.ImageView;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.martynaroj.traveljournal.BR;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

@IgnoreExtraProperties
public class Travel extends BaseObservable implements Serializable {

    String id;
    String owner;
    String name;
    String image;
    long datetimeFrom;
    long datetimeTo;
    String destination;
    private String transport;
    private String accommodation;
    private Double budget;
    List<String> tags;
    private List<Day.PackingCategory> packingList;
    private boolean packing;
    private List<String> days;
    String description;


    public Travel() {
        tags = new ArrayList<>();
        days = new ArrayList<>();
    }


    public Travel(String id, String owner, String name, long datetimeFrom,
                  long datetimeTo, String destination, String transport, String accommodation,
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
    public long getDatetimeFrom() {
        return datetimeFrom;
    }


    public void setDatetimeFrom(long datetimeFrom) {
        this.datetimeFrom = datetimeFrom;
        notifyPropertyChanged(BR.datetimeFrom);
    }


    @Bindable
    public long getDatetimeTo() {
        return datetimeTo;
    }


    public void setDatetimeTo(long datetimeTo) {
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
    public List<Day.PackingCategory> getPackingList() {
        return packingList;
    }


    public void setPackingList(List<Day.PackingCategory> packingList) {
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


    @Bindable
    public String getDescription() {
        return description;
    }


    public void setDescription(String description) {
        this.description = description;
        notifyPropertyChanged(BR.description);
    }


    @Exclude
    public static long whatDay(long date1, long date2) {
        Calendar cdate1 = Calendar.getInstance();
        Calendar cdate2 = Calendar.getInstance();
        cdate1.setTimeInMillis(date1);
        cdate2.setTimeInMillis(date2);

        cdate1.set(
                cdate1.get(Calendar.YEAR),
                cdate1.get(Calendar.MONTH),
                cdate1.get(Calendar.DAY_OF_MONTH),
                0, 0, 0
        );
        cdate1.set(Calendar.MILLISECOND, 0);

        cdate2.set(
                cdate2.get(Calendar.YEAR),
                cdate2.get(Calendar.MONTH),
                cdate2.get(Calendar.DAY_OF_MONTH),
                0, 0, 0
        );
        cdate2.set(Calendar.MILLISECOND, 0);

        long difference = cdate2.getTimeInMillis() - cdate1.getTimeInMillis();
        return TimeUnit.DAYS.convert(difference, TimeUnit.MILLISECONDS) + 1;
    }


    @Exclude
    public long whatDayToday() {
        long daysFrom = whatDay(datetimeFrom, Calendar.getInstance().getTimeInMillis());
        long daysTo = whatDay(Calendar.getInstance().getTimeInMillis(), datetimeTo);
        if (daysFrom < 1)
            return com.martynaroj.traveljournal.view.others.enums.Travel.BEFORE.value;
        else if (daysTo < 1)
            return com.martynaroj.traveljournal.view.others.enums.Travel.AFTER.value;
        else
            return daysFrom;
    }


    @Exclude
    public String getDaysRemainsString() {
        long days = whatDayToday();
        if (days >= 1)
            return days + Constants.DAY;
        else if (days == com.martynaroj.traveljournal.view.others.enums.Travel.BEFORE.value) {
            return Constants.STARTS_ON + getDateString(datetimeFrom);
        } else if (days == com.martynaroj.traveljournal.view.others.enums.Travel.AFTER.value) {
            return Constants.ENDS_ON + getDateString(datetimeTo);
        }
        return "";
    }


    @Exclude
    @SuppressLint("SimpleDateFormat")
    private String getDateString(long time) {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(time);
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        return format.format(date.getTime());
    }


    @Exclude
    @SuppressLint("SimpleDateFormat")
    private String getTimeString(long time) {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(time);
        SimpleDateFormat format = new SimpleDateFormat("hh:mm a");
        return format.format(date.getTime());
    }


    @Exclude
    public String getDateTimeString(long time) {
        return getDateString(time) + ", " + getTimeString(time);
    }


    @Exclude
    public String getDateRangeString() {
        return getDateString(datetimeFrom) + " - " + getDateString(datetimeTo);
    }


    @Exclude
    public int getTagsLength() {
        int count = 0;
        if (this.tags != null) {
            for (String string : this.tags) {
                count += string.length();
            }
        }
        return count;
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
