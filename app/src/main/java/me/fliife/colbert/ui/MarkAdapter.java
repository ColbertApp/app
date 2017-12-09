package me.fliife.colbert.ui;

import android.content.ContentValues;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import me.fliife.colbert.R;
import me.fliife.colbert.utils.PronoteObject;
import org.apache.commons.lang3.StringEscapeUtils;

import java.util.ArrayList;

public class MarkAdapter extends RecyclerView.Adapter<MarkAdapter.ViewHolder> {
    private ArrayList<ContentValues> pronoteDataset;

    public MarkAdapter(PronoteObject pronoteObject, String subject) {
        pronoteDataset = pronoteObject.getMarksBySubject(subject);
    }

    @Override
    public MarkAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mark_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MarkAdapter.ViewHolder holder, int position) {
        ContentValues markElement = pronoteDataset.get(position);
        holder.mTitle.setText(StringEscapeUtils.unescapeHtml4(markElement.getAsString("title")));
        holder.mValue.setText(markElement.getAsString("value") + "/" + markElement.getAsString("bareme") + " x" + markElement.getAsString("coef"));
    }

    @Override
    public int getItemCount() {
        return pronoteDataset.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        TextView mTitle;
        TextView mValue;
        Context mContext;

        ViewHolder(View container) {
            super(container);
            mTitle = container.findViewById(R.id.mark_title);
            mValue = container.findViewById(R.id.mark_value);
            mContext = container.getContext();
        }
    }
}

