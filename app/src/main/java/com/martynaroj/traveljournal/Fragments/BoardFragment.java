package com.martynaroj.traveljournal.Fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.martynaroj.traveljournal.Base.BaseFragment;
import com.martynaroj.traveljournal.R;

public class BoardFragment extends BaseFragment {

    public static BoardFragment newInstance() {
        return new BoardFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_board, container, false);
        return view;
    }

}
