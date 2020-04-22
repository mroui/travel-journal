package com.martynaroj.traveljournal.view.others.interfaces;

import com.google.android.gms.maps.model.LatLng;

public interface Constants {
    int RC_SIGN_IN = 9001;
    int RC_SAVE_CREDENTIALS = 9002;
    int RC_EXTERNAL_STORAGE_FILE = 8002;
    int RC_ACCESS_FINE_LOCATION = 7001;
    int RC_BROADCAST = 6001;

    int PREFERENCES_VIEW_HEIGHT = 303;
    int TAGS_VIEW_HEIGHT = 303;

    String SUCCESS = "SUCCESS";
    String ERROR = "ERROR";

    String USER = "user";

    String USERS = "Users";
    String ADDRESSES = "Addresses";
    String NOTIFICATIONS = "Notifications";
    String MARKERS = "Markers";
    String TRAVELS = "Travels";
    String RESERVATIONS = "Reservations";
    String DAYS = "Days";
    String ITINERARIES = "Itineraries";

    String DB_UID = "uid";
    String DB_PHOTO = "photo";
    String DB_BIO = "bio";
    String DB_LOCATION = "location";
    String DB_USERNAME = "username";
    String DB_EMAIL = "email";
    String DB_PREFERENCES = "preferences";
    String DB_PRIVACY = "privacy";
    String DB_FRIENDS = "friends";
    String DB_MARKERS = "markers";
    String DB_ACTIVE_TRAVEL_ID = "activeTravelId";
    String DB_IS_PACKING = "packing";
    String DB_PACKING_LIST = "packingList";
    String DB_DAYS = "days";
    String DB_RATE = "rate";
    String DB_NAME = "name";
    String DB_TAGS = "tags";
    String DB_IMAGE = "image";
    String DB_EXPENSES = "expenses";
    String DB_NOTES = "notes";
    String DB_PHOTOS = "photos";
    String DB_PLACES = "places";
    String DB_TRAVELS = "travels";
    String DB_SAVED_TRAVELS = "savedTravels";
    String DB_NOTIFICATIONS = "notifications";
    String DB_POPULARITY = "popularity";
    String DB_CREATED_DATE = "createdDate";
    String DB_DAYS_AMOUNT = "daysAmount";

    String PUBLIC = "Public";
    String FRIENDS = "Friends";
    String ONLY_ME = "Only Me";

    String [] MARKER_COLORS = {
                "#007fff", "#0000ff", "#00ffff", "#00ff00", "#ff00ff",
            "#ffa500", "#ff0000", "#ff007f", "#ee82ee", "#ffff00"
    };

    String MAP = "MAP";
    String WEATHER = "WEATHER";
    String CURRENCY = "CURRENCY";
    String TRANSLATOR = "TRANSLATE";
    String ALARM = "ALARM";

    String PLAN_TO_VISIT_TUTORIAL = "PLAN_TO_VISIT_TUTORIAL_DIALOG";
    String UPDATE_PROFILE_DIALOG = "UPDATE_PROFILE_DIALOG";

    int NEARBY_PLACES_RADIUS = 500;

    String CHANNEL_ID = "ALARM_CHANNEL";
    CharSequence CHANNEL_NAME = "ALARM_CHANNEL";

    String ALARM_DIALOG = "ALARM_DIALOG";
    String ALARM_TIME = "ALARM_TIME";
    String ALARM_NOTE = "ALARM_NOTE";
    String PREFERENCES = "PREFERENCES";
    String ALARM_DESC = "ALARM_DESC";

    LatLng LAT_LNG_LONDON = new LatLng(51.507359, -0.136439);

    String LANGUAGE_EN = "en";
    int LANGUAGE_EN_INDEX = 10;
    String DETECT_LANGUAGE = "Detect language";

    String CURRENCY_EUR = "EUR";
    int CURRENCY_EUR_INDEX = 8;
    int MAX_CURRENCY_LENGTH = 10;

    String NETWORK_ERROR = "NETWORK_ERROR";
    String TIMEOUT = "TIMEOUT";

    String TRANSPORT_FILE = "TRANSPORT_FILE";
    String ACCOMMODATION_FILE = "ACCOMMODATION_FILE";
    String TRAVEL_IMAGE_FILE = "TRAVEL_IMAGE_FILE";

    String IMAGE = "image";
    String ACCOMMODATION = "accommodation";
    String TRANSPORT = "transport";

    String STORAGE_TRAVELS = "travels";
    String STORAGE_DAYS = "days";

    String PDF_EXT = ".pdf";
    String JPG_EXT = ".jpg";

    String DAY = " day";
    String STARTS_ON = "Starts on ";
    String ENDS_ON = "Ended on ";

    String BUNDLE_DESTINATION = "DESTINATION";
    String BUNDLE_TRAVEL = "TRAVEL";
    String BUNDLE_USER = "USER";
    String BUNDLE_DAY = "DAY";
    String BUNDLE_DAYS = "DAYS";
    String BUNDLE_TRANSPORT = "BUNDLE_TRANSPORT";
    String BUNDLE_ACCOMMODATION = "BUNDLE_ACCOMMODATION";
    String BUNDLE_ITINERARY = "BUNDLE_ITINERARY";
    String BUNDLE_LOGGED_USER = "BUNDLE_LOGGED_USER";

    int USER_IMG_H = 100;
    int USER_IMG_W = 100;
    int TRAVEL_IMG_H = 200;
    int TRAVEL_IMG_W = 300;
    int RESERVATION_IMG_H = 400;
    int RESERVATIONS_IMG_W = 400;

    int PAGE_A4_WIDTH = 595;
    int PAGE_A4_HEIGHT = 842;

}