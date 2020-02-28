package com.martynaroj.traveljournal.view.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.paging.FirestorePagingOptions;
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

public class SearchFriendsFragment extends BaseFragment {

    private FragmentSearchFriendsBinding binding;
    private CollectionReference usersRef;
    private PagedList.Config usersPagingConfig;
    private UserAdapter adapter;

    public static SearchFriendsFragment newInstance() {
        return new SearchFriendsFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSearchFriendsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        initData();
        setListeners();
        focusOnSearchView();

        return view;
    }


    private void initData() {
        usersRef = FirebaseFirestore.getInstance().collection(Constants.USERS);
        usersPagingConfig = new PagedList.Config.Builder().setInitialLoadSizeHint(10).setPageSize(3).build();
    }


    private void setListeners() {
        binding.searchFriendsArrowButton.setOnClickListener(view -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0)
                getParentFragmentManager().popBackStack();
        });
        binding.searchFriendsSearchView.setOnQueryTextListener(new SearchViewListener() {
            @Override
            public boolean onQueryTextChange(String s) {
                if (!s.equals("")) {
                    searchUser(s);
                } else {
                    binding.searchFriendsRecyclerView.setAdapter(null);
                    binding.searchFriendsMessage.setVisibility(View.GONE);
                }
                return false;
            }
        });
    }


    private void setAdapterOnItemClickListener() {
        adapter.setOnItemClickListener((snapshot, position) -> {
            User user = snapshot.toObject(User.class);
            if (user != null) {
                hideKeyboard();
                changeFragment(ProfileFragment.newInstance(user));
            }
        });
    }


    @SuppressWarnings("ConstantConditions")
    private void hideKeyboard() {
        if (getActivity() != null) {
            ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
    }


    private void changeFragment(Fragment next) {
        getNavigationInteractions().changeFragment(getParentFragment(), next, true);
    }


    private void setAdapterObserver() {
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            public void onItemRangeInserted(int positionStart, int itemCount) {
                int totalNumberOfItems = adapter.getItemCount();
                if(totalNumberOfItems == 0) {
                    binding.searchFriendsMessage.setVisibility(View.VISIBLE);
                    binding.searchFriendsMessage.setText(getResources().getString(R.string.search_friends_no_results));
                } else {
                    binding.searchFriendsMessage.setVisibility(View.GONE);
                }
            }
        });
    }


    private void focusOnSearchView() {
        if (getContext() != null) {
            binding.searchFriendsSearchView.requestFocus();
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            assert imm != null;
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }


    private void searchUser(String username) {
        Query query = usersRef.orderBy(Constants.USERNAME).startAt(username).endAt(username + "\uf8ff");
        FirestorePagingOptions<User> options = new FirestorePagingOptions.Builder<User>()
                .setLifecycleOwner(getViewLifecycleOwner())
                .setQuery(query, usersPagingConfig, User.class)
                .build();
        adapter = new UserAdapter(options, getContext());
        binding.searchFriendsRecyclerView.swapAdapter(adapter, true);
        setAdapterOnItemClickListener();
        setAdapterObserver();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
