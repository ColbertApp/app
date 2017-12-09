package me.fliife.colbert;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import me.fliife.colbert.storage.StorageUtils;
import me.fliife.colbert.ui.MarkAdapter;

public class MarksListDialogFragment extends BottomSheetDialogFragment {

    private static final String SUB_NAME = "sub_name";

    public static MarksListDialogFragment newInstance(String subname) {
        final MarksListDialogFragment fragment = new MarksListDialogFragment();
        final Bundle args = new Bundle();
        args.putString(SUB_NAME, subname);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_marks_list_dialog, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        final RecyclerView recyclerView = (RecyclerView) view;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new MarkAdapter(StorageUtils.getPronoteObjectFromDatabase(getContext()), getArguments().getString(SUB_NAME)));
    }
}
