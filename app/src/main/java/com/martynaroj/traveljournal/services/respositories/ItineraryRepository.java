package com.martynaroj.traveljournal.services.respositories;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.martynaroj.traveljournal.services.models.Itinerary;
import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.view.others.enums.Criterion;
import com.martynaroj.traveljournal.view.others.enums.Privacy;
import com.martynaroj.traveljournal.view.others.enums.Status;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ItineraryRepository {

    private Context context;
    private CollectionReference itinerariesRef;

    private ItineraryRepository() {
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        itinerariesRef = rootRef.collection(Constants.ITINERARIES);
    }


    public ItineraryRepository(Context context) {
        this();
        this.context = context;
    }


    public MutableLiveData<Status> addItinerary(Itinerary itinerary) {
        MutableLiveData<Status> status = new MutableLiveData<>();
        DocumentReference itineraryRef = itinerariesRef.document(itinerary.getId());
        itineraryRef.set(itinerary).addOnCompleteListener(task -> {
            if (task.isSuccessful())
                status.setValue(Status.SUCCESS);
            else
                status.setValue(Status.ERROR);
        });
        return status;
    }


    public MutableLiveData<List<Itinerary>> getItineraries(List<String> ids) {
        MutableLiveData<List<Itinerary>> itinerariesData = new MutableLiveData<>();
        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
        for (String id : ids)
            if (id != null)
                tasks.add(itinerariesRef.document(id).get());
        Task<List<DocumentSnapshot>> finalTask = Tasks.whenAllSuccess(tasks);
        finalTask.addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<Itinerary> itineraries = new ArrayList<>();
                for (DocumentSnapshot documentSnapshot : task.getResult())
                    itineraries.add(documentSnapshot.toObject(Itinerary.class));
                itinerariesData.setValue(itineraries);
            }
        });
        return itinerariesData;
    }


    public void removeItinerary(String id) {
        DocumentReference itineraryRef = itinerariesRef.document(id);
        itineraryRef.delete();
    }


    public void updateItinerary(Itinerary itinerary, Map<String, Object> map) {
        DocumentReference itineraryRef = itinerariesRef.document(itinerary.getId());
        itineraryRef.update(map);
    }


    public LiveData<List<Itinerary>> getItinerariesOrderBy(User user, int limit, String orderBy, Query.Direction direction) {
        MutableLiveData<List<Itinerary>> itinerariesData = new MutableLiveData<>();
        itinerariesRef.orderBy(orderBy, direction).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<Itinerary> itineraries = new ArrayList<>();
                for (int i = 0; i < task.getResult().size(); i++) {
                    Itinerary itinerary = task.getResult().getDocuments().get(i).toObject(Itinerary.class);
                    if (itinerary != null
                            && (itinerary.getPrivacy() == Privacy.PUBLIC.ordinal()
                            || (itinerary.getPrivacy() == Privacy.FRIENDS.ordinal()
                            && user != null
                            && user.getFriends().contains(itinerary.getOwner()))
                            || (user != null
                            && user.getUid().equals(itinerary.getOwner()))))
                        itineraries.add(itinerary);
                    if (itineraries.size() == limit)
                        break;
                }
                itinerariesData.setValue(itineraries);
            } else
                itinerariesData.setValue(null);
        });
        return itinerariesData;
    }


    public LiveData<List<DocumentSnapshot>> getItinerariesDocumentsListStartAt(
            User user, int limit, DocumentSnapshot last, String orderBy, Query.Direction direction,
            Criterion... criteria) {
        MutableLiveData<List<DocumentSnapshot>> documentsData = new MutableLiveData<>();
        Query query = itinerariesRef.orderBy(orderBy, direction);
        if (last != null)
            query = query.startAfter(last);
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null)
                documentsData.setValue(getListWithLimitAndCriterion(task.getResult(), user, limit, criteria));
            else
                documentsData.setValue(null);
        });
        return documentsData;
    }


    //CHECK CRITERIA--------------------------------------------------------------------------------


    private List<DocumentSnapshot> getListWithLimitAndCriterion(QuerySnapshot querySnapshot,
                                                                User user, int limit,
                                                                Criterion... criteria) {
        List<DocumentSnapshot> documentsList = new ArrayList<>();
        for (int i = 0; i < querySnapshot.size(); i++) {
            Itinerary itinerary = querySnapshot.getDocuments().get(i).toObject(Itinerary.class);
            if (itinerary != null
                    && (itinerary.getPrivacy() == Privacy.PUBLIC.ordinal()
                    || (itinerary.getPrivacy() == Privacy.FRIENDS.ordinal()
                    && user != null
                    && user.getFriends().contains(itinerary.getOwner()))
                    || (user != null
                    && user.getUid().equals(itinerary.getOwner()))))
                if (criteria.length == 0 || checkCriterion(itinerary, criteria))
                    documentsList.add(querySnapshot.getDocuments().get(i));
            if (documentsList.size() == limit)
                break;
        }
        return documentsList;
    }


    private boolean checkCriterion(Itinerary itinerary, Criterion... criteria) {
        boolean result = false;
        for (Criterion c : criteria) {
            switch (c) {
                case KEYWORDS:
                    result = checkKeywordsCriterion(itinerary, (c.getValue()).trim().toLowerCase());
                    break;
                case DAYS_FROM:
                    result = checkDaysCriterion(itinerary, c.getValue(), true);
                    break;
                case DAYS_TO:
                    result = checkDaysCriterion(itinerary, c.getValue(), false);
                    break;
                case DESTINATION:
                    result = checkDestinationCriterion(itinerary, c.getValue());
                    break;
                case TAGS:
                    result = checkTagsCriterion(itinerary, c.getValue());
                    break;
            }
            if (!result)
                break;
        }
        return result;
    }


    private boolean checkDestinationCriterion(Itinerary itinerary, String value) {
        if (!value.trim().isEmpty())
            return itinerary.getDestination().trim().toLowerCase().contains(value);
        else
            return true;
    }


    private boolean checkDaysCriterion(Itinerary itinerary, String value, boolean from) {
        if (!value.trim().isEmpty())
            if (from)
                return itinerary.getDaysAmount() >= Integer.parseInt(value);
            else
                return itinerary.getDaysAmount() <= Integer.parseInt(value);
        else
            return true;
    }


    private boolean checkTagsCriterion(Itinerary itinerary, String value) {
        List<String> tags = Arrays.asList(value.split("&"));
        if (tags.size() > 0)
            if ((tags.size() == 1 && tags.get(0).trim().isEmpty()))
                return true;
            else
                return itinerary.getTags().containsAll(tags);
        else
            return true;
    }


    private boolean checkKeywordsCriterion(Itinerary itinerary, String value) {
        List<String> keywords = Arrays.asList(value.split("\\s+"));
        if (itinerary.getName().contains(value))
            return true;
        else if (itinerary.getDescription().trim().toLowerCase().contains(value))
            return true;
        else if (itinerary.getDestinationString().trim().toLowerCase().contains(value))
            return true;
        else
            return itinerary.getTags().containsAll(keywords);
    }

}
