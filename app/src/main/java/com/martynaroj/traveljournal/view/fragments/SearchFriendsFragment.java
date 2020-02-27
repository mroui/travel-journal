package com.martynaroj.traveljournal.view.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.martynaroj.traveljournal.R;
import com.martynaroj.traveljournal.databinding.FragmentSearchFriendsBinding;
import com.martynaroj.traveljournal.services.models.User;
import com.martynaroj.traveljournal.view.adapters.UserAdapter;
import com.martynaroj.traveljournal.view.base.BaseFragment;
import com.martynaroj.traveljournal.view.others.classes.SearchViewListener;
import com.martynaroj.traveljournal.view.others.interfaces.Constants;

public class SearchFriendsFragment extends BaseFragment implements View.OnClickListener {

    private FragmentSearchFriendsBinding binding;
    private CollectionReference usersRef;
    private UserAdapter adapter;

    public static SearchFriendsFragment newInstance() {
        return new SearchFriendsFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSearchFriendsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initUsersReference();
        setListeners();
        focusOnSearchView();

        return view;
    }


    private void initUsersReference() {
        usersRef = FirebaseFirestore.getInstance().collection(Constants.USERS);
    }


    private void setListeners() {
        binding.searchFriendsArrowButton.setOnClickListener(this);
        binding.searchFriendsSearchView.setOnQueryTextListener(new SearchViewListener() {
            @Override
            public boolean onQueryTextChange(String s) {
                if (!s.equals("")) {
                    searchUser(s);
                } else {
                    binding.searchFriendsRecyclerView.setAdapter(null);
                    adapter.stopListening();
                    binding.searchFriendsMessage.setVisibility(View.GONE);
                }
                return false;
            }
        });
    }


    private void focusOnSearchView() {
        binding.searchFriendsSearchView.requestFocus();
    }


    private void searchUser(String username) {
        Query query = usersRef.orderBy(Constants.USERNAME).startAt(username).endAt(username + "\uf8ff");
        FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
                .setLifecycleOwner(getViewLifecycleOwner())
                .setQuery(query, User.class)
                .build();
        adapter = new UserAdapter(options, getContext());
        binding.searchFriendsRecyclerView.swapAdapter(adapter, true);
        adapter.startListening();
        adapter.setOnItemClickListener((documentSnapshot, position) -> {
            User user = documentSnapshot.toObject(User.class);
            if (user != null) {
                Toast.makeText(getContext(),
                        "Clicked: " + user.getUid(), Toast.LENGTH_SHORT).show();
            }
        });
        //TODO: query async refactor mvvm: pagination + recyclerview + limit(10) + refresh on swipe + onclick + messages when no results / start to search
        if (adapter.getItemCount() == 0) {
            binding.searchFriendsMessage.setVisibility(View.VISIBLE);
            //binding.searchFriendsMessage.setText(getResources().getString(R.string.search_friends_no_results));
        } else {
            binding.searchFriendsMessage.setVisibility(View.GONE);
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.search_friends_arrow_button:
                if (getParentFragmentManager().getBackStackEntryCount() > 0)
                    getParentFragmentManager().popBackStack();
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null)
            adapter.stopListening();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
