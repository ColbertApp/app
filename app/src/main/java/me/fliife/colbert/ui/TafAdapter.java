package me.fliife.colbert.ui;

import android.animation.StateListAnimator;
import android.content.ContentValues;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Space;
import android.widget.TextView;
import me.fliife.colbert.R;
import me.fliife.colbert.utils.DateUtils;
import me.fliife.colbert.utils.PronoteObject;
import org.apache.commons.lang3.StringEscapeUtils;

import java.util.ArrayList;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class TafAdapter extends RecyclerView.Adapter<TafAdapter.ViewHolder> {
    private PronoteObject pronoteDataset;
    private int dateCount = 0;
    private ArrayList<Integer> datePosition = new ArrayList<>();

    public TafAdapter(PronoteObject pronoteObject) {
        pronoteDataset = pronoteObject;
        String date = "00/00/000";
        dateCount = 0;
        int i = 0;
        for (ContentValues elem : pronoteDataset.getTaf()) {
            if (!DateUtils.areSameDay(date, elem.getAsString("date"))) {
                datePosition.add(i + dateCount);
                dateCount++;
            }
            date = elem.getAsString("date");
            i++;
        }
    }

    private int positionsBeforeIndex(int i) {
        int count = 0;
        for (int pos : datePosition) {
            if (pos <= i) count++;
        }
        return count;
    }

    @Override
    public TafAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.taf_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (datePosition.contains(position)) {
            // Hide unwanted entries
            holder.mDate.setVisibility(GONE);
            holder.mDM.setVisibility(GONE);
            holder.mContent.setVisibility(GONE);
            holder.mSpace.setVisibility(GONE);
            holder.mSubject.setText(DateUtils.getDay(
                    pronoteDataset.getTaf().get(
                            position - positionsBeforeIndex(position) + 1)
                            .getAsString("date")));
            holder.mCardView.setForeground(null);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                holder.mCardView.setStateListAnimator(null);
                holder.mCardView.setElevation(0);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                holder.mSubject.setTextAppearance(android.R.style.TextAppearance_Material_Medium);
                holder.mSubject.setTypeface(null, Typeface.BOLD);
            }
            holder.mCardView.setCardBackgroundColor(
                    holder.mCardView.getContext().getResources().getColor(R.color.background_light));
            return;
        }
        // Make sure everything is correctly displayed
        holder.mDate.setVisibility(VISIBLE);
        holder.mDM.setVisibility(VISIBLE);
        holder.mContent.setVisibility(VISIBLE);
        holder.mCardView.setVisibility(VISIBLE);
        holder.mSpace.setVisibility(VISIBLE);
        holder.mCardView.setCardBackgroundColor(
                holder.mCardView.getContext().getResources().getColor(android.R.color.white));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.mCardView.setElevation(2 * holder.mCardView.getContext().getResources().getDisplayMetrics().density);
            holder.mCardView.setStateListAnimator(holder.mStateListAnimator);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            holder.mSubject.setTextAppearance(android.R.style.TextAppearance_Material_Title);
            holder.mSubject.setTypeface(null, Typeface.NORMAL);
        }
        // Actual logic
        // Get the correct position, that is, remove every non-element which would shift the position
        position -= positionsBeforeIndex(position);
        ContentValues tafElement = pronoteDataset.getTaf().get(position);
        String date = tafElement.getAsString("date");
        holder.mSubject.setText(tafElement.getAsString("sub"));
        holder.mDate.setText(date);
        holder.mContent.setText(StringEscapeUtils.unescapeHtml4(tafElement.getAsString("content")));
        if (DateUtils.isPast(tafElement.getAsString("date"))) {
            holder.mCardView.setVisibility(GONE);
        }
        holder.mDM.setVisibility(GONE);
    }

    @Override
    public int getItemCount() {
        return pronoteDataset.getTaf().size() + dateCount;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        TextView mSubject;
        TextView mDate;
        TextView mContent;
        TextView mDM;
        Space mSpace;
        StateListAnimator mStateListAnimator = null;
        CardView mCardView;

        ViewHolder(View container) {
            super(container);
            mSubject = container.findViewById(R.id.taf_subject);
            mContent = container.findViewById(R.id.taf_content);
            mDate = container.findViewById(R.id.taf_date);
            mDM = container.findViewById(R.id.taf_dm);
            mSpace = container.findViewById(R.id.taf_space);
            mCardView = container.findViewById(R.id.taf_cardview);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                mStateListAnimator = mCardView.getStateListAnimator();
        }
    }
}

