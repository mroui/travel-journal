package com.martynaroj.traveljournal.services.models;

import android.annotation.SuppressLint;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.martynaroj.traveljournal.BR;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@IgnoreExtraProperties
public class Itinerary extends Travel implements Serializable {

    private String file;
    private int privacy;
    private long createdDate;
    private long popularity;
    private long daysAmount;

    @Exclude
    private String transport;
    @Exclude
    private String accommodation;
    @Exclude
    private Double budget;
    @Exclude
    private List<Day.PackingCategory> packingList;
    @Exclude
    private boolean packing;
    @Exclude
    private List<String> days;


    private Itinerary() {
        tags = new ArrayList<>();
        this.createdDate = System.currentTimeMillis();
        popularity = 0;
    }


    public Itinerary(String id, String owner, String name, String image, long datetimeFrom, long datetimeTo,
                     String destination, String description, List<String> tags, String file, int privacy) {
        this();
        this.id = id;
        this.owner = owner;
        this.name = name;
        this.image = image;
        this.datetimeFrom = datetimeFrom;
        this.datetimeTo = datetimeTo;
        this.destination = destination;
        this.description = description;
        this.tags = tags;
        this.file = file;
        this.privacy = privacy;
        this.daysAmount = Travel.whatDay(datetimeFrom, datetimeTo);
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
    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
        notifyPropertyChanged(BR.name);
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
    public String getDescription() {
        return description;
    }


    public void setDescription(String description) {
        this.description = description;
        notifyPropertyChanged(BR.description);
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
    public String getFile() {
        return file;
    }


    public void setFile(String file) {
        this.file = file;
        notifyPropertyChanged(BR.file);
    }


    @Bindable
    public int getPrivacy() {
        return privacy;
    }


    public void setPrivacy(int privacy) {
        this.privacy = privacy;
        notifyPropertyChanged(BR.privacy);
    }


    @Bindable
    public long getCreatedDate() {
        return createdDate;
    }


    @Bindable
    public long getPopularity() {
        return popularity;
    }


    public void addPopularity() {
        this.popularity += 1;
        notifyPropertyChanged(BR.popularity);
    }


    public void subtractPopularity() {
        this.popularity -= 1;
        notifyPropertyChanged(BR.popularity);
    }


    @Bindable
    public long getDaysAmount() {
        return daysAmount;
    }


    @Exclude
    public String getDestinationString() {
        return destination.replace("&", ", ");
    }


    @Exclude
    @SuppressLint("SimpleDateFormat")
    public String getDateString(long time) {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(time);
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        return format.format(date.getTime());
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

    public static class PackingItem extends BaseObservable implements Serializable {

        private String name;
        private boolean checked;

        public PackingItem() {
        }


        public PackingItem(String name) {
            this.name = name;
        }

        @Bindable
        public String getName() {
            return name;
        }

        @Bindable
        public boolean isChecked() {
            return checked;
        }

        public void setChecked(boolean checked) {
            this.checked = checked;
            notifyPropertyChanged(BR.checked);
        }

    }
}
