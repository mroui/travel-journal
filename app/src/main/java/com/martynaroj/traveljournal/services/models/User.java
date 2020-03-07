package com.martynaroj.traveljournal.services.models;

import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.martynaroj.traveljournal.BR;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.view.others.enums.Privacy;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User extends BaseObservable implements Serializable {

    private String uid;
    private String username;
    private String email;
    private String photo;
    private String bio;
    private String location;
    private List<String> preferences;
    private List<String> friends;
    private List<String> notifications;
    private Map<String, Integer> privacy;
    private List<Marker> markers;
    //private List<Travel> travels;

    public User() {
    }

    public User(String uid, String username, String email) {
        this.uid = uid;
        this.username = username;
        this.email = email;

        this.privacy = new HashMap<>();
        defineDefaultPrivacy();
    }

    private void defineDefaultPrivacy() {
        privacy.put(Constants.EMAIL, Privacy.PUBLIC.ordinal());
        privacy.put(Constants.LOCATION, Privacy.PUBLIC.ordinal());
        privacy.put(Constants.PREFERENCES, Privacy.PUBLIC.ordinal());
    }

    @Bindable
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        notifyPropertyChanged(BR.username);
    }

    @Bindable
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        notifyPropertyChanged(BR.email);
    }

    @Bindable
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
        notifyPropertyChanged(BR.uid);
    }

    @Bindable
    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
        notifyPropertyChanged(BR.photo);
    }

    @Bindable
    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
        notifyPropertyChanged(BR.bio);
    }

    @Bindable
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
        notifyPropertyChanged(BR.location);
    }

    @Bindable
    public List<String> getPreferences() {
        return preferences;
    }

    public void setPreferences(List<String> preferences) {
        this.preferences = preferences;
        notifyPropertyChanged(BR.preferences);
    }

    public int getPreferencesSize() {
        int count = 0;
        if (this.preferences != null) {
            for (String string : this.preferences) {
                count += string.length();
            }
        }
        return count;
    }

    @Bindable
    public List<String> getFriends() {
        return friends;
    }

    public void setFriends(List<String> friends) {
        this.friends = friends;
        notifyPropertyChanged(BR.friends);
    }

    @Bindable
    public List<String> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<String> notifications) {
        this.notifications = notifications;
        notifyPropertyChanged(BR.notifications);
    }

    @Bindable
    public Map<String, Integer> getPrivacy() {
        return privacy;
    }

    @Bindable
    public List<Marker> getMarkers() {
        return markers;
    }

    public void setMarkers(List<Marker> markers) {
        this.markers = markers;
        notifyPropertyChanged(BR.markers);
    }

    public boolean hasNotifications() {
        return notifications != null && !notifications.isEmpty();
    }

    public void setPrivacy(Map<String, Integer> privacy) {
        this.privacy = privacy;
        notifyPropertyChanged(BR.privacy);
    }

    @SuppressWarnings("ConstantConditions")
    @Bindable
    public int getPrivacyEmail() {
        if (this.privacy != null && this.privacy.get(Constants.EMAIL) != null)
            return this.privacy.get(Constants.EMAIL);
        else return 2;
    }

    @SuppressWarnings("ConstantConditions")
    @Bindable
    private int getPrivacyLocation() {
        if (this.privacy != null && this.privacy.get(Constants.LOCATION) != null)
            return this.privacy.get(Constants.LOCATION);
        else return 2;
    }

    @SuppressWarnings("ConstantConditions")
    @Bindable
    private int getPrivacyPreferences() {
        if (this.privacy != null && this.privacy.get(Constants.PREFERENCES) != null)
            return this.privacy.get(Constants.PREFERENCES);
        else return 2;
    }

    public boolean isEmailAvailableForUser (User loggedUser) {
        return (!isUserProfile(loggedUser) && this.getPrivacyEmail() == 0) || isUserProfile(loggedUser);
    }

    public boolean isLocationAvailableForUser (User loggedUser) {
        return ((!isUserProfile(loggedUser) && this.getPrivacyLocation() == 0) || isUserProfile(loggedUser))
                && this.location != null;
    }

    public boolean isPreferencesAvailableForUser (User loggedUser) {
        return ((!isUserProfile(loggedUser) && this.getPrivacyPreferences() == 0) || isUserProfile(loggedUser))
                && this.preferences != null;
    }

    public boolean isUserProfile (User loggedUser) {
        return loggedUser != null && this.uid.equals(loggedUser.getUid());
    }

    public boolean hasFriend(User loggedUser) {
        return loggedUser != null && this.friends != null && this.friends.contains(loggedUser.uid);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        try {
            if (obj == null || getClass() != obj.getClass())
                return false;

            User u = (User) obj;
            return ((uid.equals(u.getUid()))
                    && (username.equals(u.getUsername()))
                    && (email.equals(u.getEmail()))
                    && (photo.equals(u.getPhoto()) || (photo == null && u.getPhoto() == null))
                    && (bio.equals(u.getBio()) || (bio == null && u.getBio() == null))
                    && (location.equals(u.getLocation())
                        || (location == null && u.getLocation() == null))
                    && ((preferences != null && preferences.equals(u.getPreferences()))
                        || (preferences == null && u.getPreferences() == null))
                    && (friends != null && (friends.equals(u.getFriends()))
                        || (friends == null && u.getFriends() == null))
                    && (privacy.equals(u.getPrivacy())));
        } catch (Exception ex) {
            return false;
        }
    }

    @BindingAdapter("imageUrl")
    public static void loadImage(ImageView v, String imgUrl){
        Glide.with(v.getContext())
                .load(imgUrl)
                .placeholder(R.drawable.default_avatar)
                .into(v);
    }

}
