package me.fliife.colbert.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.fliife.colbert.R;
import me.fliife.colbert.utils.VolleySingleton;

public class FbFeedFragment extends Fragment {
    private static final String FB_PAGE_ID = "fbpageid";

    private String fbPageId;

    public FbFeedFragment() {
        // Required empty public constructor
    }

    public static FbFeedFragment newInstance(String fbPageId) {
        FbFeedFragment fragment = new FbFeedFragment();
        Bundle args = new Bundle();
        args.putString(FB_PAGE_ID, fbPageId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            fbPageId = getArguments().getString(FB_PAGE_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_fb_feed, container, false);
        final RecyclerView recyclerView = rootView.findViewById(R.id.fbrecyclerview);
        VolleySingleton volleySingleton = VolleySingleton.getInstance();

        //volleySingleton.addToRequestQueue();

        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
