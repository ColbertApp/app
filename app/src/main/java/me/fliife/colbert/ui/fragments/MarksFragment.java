package me.fliife.colbert.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import me.fliife.colbert.R;

public class MarksFragment extends Fragment {

    public MarksFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.generic_rv_layout, container, false);

        final RecyclerView recyclerView = rootView.findViewById(R.id.generic_rv);

        return rootView;
    }

}