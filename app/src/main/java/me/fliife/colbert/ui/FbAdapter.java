package me.fliife.colbert.ui;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.common.collect.Iterables;
import com.google.firebase.database.DataSnapshot;
import me.fliife.colbert.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FbAdapter extends RecyclerView.Adapter<FbAdapter.ViewHolder> {
    private DataSnapshot dataset;

    public FbAdapter(DataSnapshot dataset) {
        this.dataset = dataset;
    }

    @Override
    public FbAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fb_layout, parent, false);

        TextView content = v.findViewById(R.id.fb_content);
        TextView status = v.findViewById(R.id.fb_status);
        CardView card = v.findViewById(R.id.card_fb);
        TextView date = v.findViewById(R.id.fb_date);

        ViewHolder vh = new ViewHolder(content, status, card, date);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final FbObject currentObj = Iterables.get(dataset.getChildren(), position).getValue(FbObject.class);
        holder.content.setText(currentObj.message);
        if (currentObj.story != null && currentObj.story != "") {
            holder.status.setText(currentObj.story);
            holder.status.setVisibility(View.VISIBLE);
        } else {
            holder.status.setVisibility(View.GONE);
        }
        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://facebook.com/" + currentObj.id));
                view.getContext().startActivity(i);
            }
        });
        try {
            Date parsed = new SimpleDateFormat("yyyy-MM-dd").parse(currentObj.created_time.split("T")[0]);
            DateFormat dateFormat = new SimpleDateFormat("'Le 'dd MMMM yyyy", Locale.FRENCH);
            holder.date.setText(dateFormat.format(parsed));
            holder.date.setVisibility(View.VISIBLE);
        } catch (ParseException e) {
            e.printStackTrace();
            holder.date.setText("");
            holder.date.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return (int) dataset.getChildrenCount();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView content, status, date;
        public CardView card;

        public ViewHolder(TextView content, TextView status, CardView card, TextView date) {
            super(content.getRootView());
            this.content = content;
            this.status = status;
            this.card = card;
            this.date = date;
        }
    }

    private static class FbObject {
        public String message, id, created_time, story;

        public FbObject() {
        }

        public FbObject(String message, String id, String created_time, String story) {
            this.message = message;
            this.created_time = created_time;
            this.story = story;
            this.id = id;
        }
    }
}