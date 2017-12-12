package me.fliife.colbert.ui.adapters;

import android.content.ContentValues;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import me.fliife.colbert.MarksListDialogFragment;
import me.fliife.colbert.R;
import me.fliife.colbert.utils.PronoteObject;
import org.apache.commons.lang3.StringEscapeUtils;

public class AvgAdapter extends RecyclerView.Adapter<AvgAdapter.ViewHolder> {
    private PronoteObject pronoteDataset;
    private FragmentManager fragmentManager;

    public AvgAdapter(PronoteObject pronoteObject, FragmentManager fragmentManager) {
        pronoteDataset = pronoteObject;
        this.fragmentManager = fragmentManager;
    }

    @Override
    public AvgAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.avg_card, parent, false);
        AvgAdapter.ViewHolder vh = new AvgAdapter.ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(final AvgAdapter.ViewHolder holder, int position) {
        final ContentValues averageElement = pronoteDataset.getAverages().get(position);
        holder.mSubject.setText(StringEscapeUtils.unescapeHtml4(averageElement.getAsString("subject")));
        holder.mValue.setText(averageElement.getAsString("average").substring(9));
        holder.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MarksListDialogFragment.newInstance(averageElement.getAsString("subject")).show(fragmentManager, "dialog");
            }
        });
    }

    @Override
    public int getItemCount() {
        return pronoteDataset.getAverages().size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView mSubject;
        TextView mValue;
        CardView mCardView;

        ViewHolder(View container) {
            super(container);
            mSubject = container.findViewById(R.id.avg_sub);
            mValue = container.findViewById(R.id.avg_value);
            mCardView = container.findViewById(R.id.avg_card);
        }
    }
}

