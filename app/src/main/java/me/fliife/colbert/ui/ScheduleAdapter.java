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
import java.util.Objects;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {
    private PronoteObject pronoteDataset;
    private int dateCount = 0;
    private ArrayList<Integer> datePosition = new ArrayList<>();

    public ScheduleAdapter(PronoteObject pronoteObject) {
        pronoteDataset = pronoteObject;
        String date = "00/00/000";
        dateCount = 0;
        int i = 0;
        for (ContentValues elem : pronoteDataset.getSchedule()) {
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
    public ScheduleAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sched_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (datePosition.contains(position)) {
            // Hide unwanted entries
            holder.mClassroom.setVisibility(GONE);
            holder.mNotice.setVisibility(GONE);
            holder.mSubject.setVisibility(GONE);
            holder.mSpace.setVisibility(GONE);
            holder.mHour.setText(DateUtils.getDay(
                    pronoteDataset.getSchedule().get(
                            position - positionsBeforeIndex(position) + 1)
                            .getAsString("date")));
            holder.mCardView.setForeground(null);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                holder.mCardView.setStateListAnimator(null);
                holder.mCardView.setElevation(0);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                holder.mHour.setTextAppearance(android.R.style.TextAppearance_Material_Medium);
                holder.mHour.setTypeface(null, Typeface.BOLD);
            }
            holder.mCardView.setCardBackgroundColor(
                    holder.mCardView.getContext().getResources().getColor(R.color.background_light));
            return;
        }
        // Make sure everything is correctly displayed
        holder.mClassroom.setVisibility(VISIBLE);
        holder.mNotice.setVisibility(VISIBLE);
        holder.mSubject.setVisibility(VISIBLE);
        holder.mCardView.setVisibility(VISIBLE);
        holder.mSpace.setVisibility(VISIBLE);
        holder.mCardView.setCardBackgroundColor(
                holder.mCardView.getContext().getResources().getColor(android.R.color.white));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.mCardView.setElevation(2 * holder.mCardView.getContext().getResources().getDisplayMetrics().density);
            holder.mCardView.setStateListAnimator(holder.mStateListAnimator);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            holder.mHour.setTextAppearance(android.R.style.TextAppearance_Material_Title);
            holder.mHour.setTypeface(null, Typeface.NORMAL);
        }
        // Actual logic
        // Get the correct position, that is, remove every non-element which would shift the position
        position -= positionsBeforeIndex(position);
        ContentValues scheduleElement = pronoteDataset.getSchedule().get(position);
        String date = scheduleElement.getAsString("date");
        holder.mHour.setText(scheduleElement.getAsString("hour"));
        holder.mClassroom.setText(scheduleElement.getAsString("classroom") + " - " + date);
        holder.mSubject.setText(scheduleElement.getAsString("teacherName") +
                " - " + StringEscapeUtils.unescapeHtml4(scheduleElement.getAsString("sub")));
        if (Objects.equals(scheduleElement.getAsString("notice"), "")) {
            holder.mNotice.setVisibility(GONE);
        } else {
            holder.mNotice.setVisibility(VISIBLE);
            holder.mNotice.setText(scheduleElement.getAsString("notice"));
        }
        if (DateUtils.isPast(scheduleElement.getAsString("date"), scheduleElement.getAsString("hour"))) {
            holder.mCardView.setVisibility(GONE);
        }
    }

    @Override
    public int getItemCount() {
        return pronoteDataset.getSchedule().size() + dateCount;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        TextView mHour;
        TextView mClassroom;
        TextView mSubject;
        TextView mNotice;
        Space mSpace;
        StateListAnimator mStateListAnimator = null;
        CardView mCardView;

        ViewHolder(View container) {
            super(container);
            mHour = container.findViewById(R.id.sched_hour);
            mSubject = container.findViewById(R.id.sched_content);
            mClassroom = container.findViewById(R.id.sched_classroom);
            mNotice = container.findViewById(R.id.sched_missing);
            mSpace = container.findViewById(R.id.sched_space);
            mCardView = container.findViewById(R.id.sched_card);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                mStateListAnimator = mCardView.getStateListAnimator();
        }
    }
}

