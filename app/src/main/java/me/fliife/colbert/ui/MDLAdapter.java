package me.fliife.colbert.ui;

import android.support.v7.widget.RecyclerView;
import android.text.Html;
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

public class MDLAdapter extends RecyclerView.Adapter<MDLAdapter.ViewHolder> {
    private DataSnapshot dataset;

    public MDLAdapter(DataSnapshot dataset) {
        this.dataset = dataset;
    }

    @Override
    public MDLAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                    int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.mdl_layout, parent, false);

        TextView content = v.findViewById(R.id.mdl_content);
        TextView status = v.findViewById(R.id.mdl_title);
        TextView date = v.findViewById(R.id.mdl_date);

        ViewHolder vh = new ViewHolder(content, status, date);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        MdLObject currentObj = Iterables.get(dataset.getChildren(), position).getValue(MdLObject.class);
        holder.content.setText(Html.fromHtml(currentObj.content_encoded));
        holder.title.setText(currentObj.title);
        try {
            Date parsed = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH).parse(currentObj.pubdate);
            DateFormat dateFormat = new SimpleDateFormat("'Le 'dd MMMM yyyy", Locale.FRENCH);
            holder.date.setText(dateFormat.format(parsed));
        } catch (ParseException e) {
            e.printStackTrace();
            holder.date.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return (int) dataset.getChildrenCount();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView content, title, date;

        public ViewHolder(TextView content, TextView title, TextView date) {
            super(content.getRootView());
            this.content = content;
            this.title = title;
            this.date = date;
        }
    }

    private static class MdLObject {
        public String content_encoded, dc_creator, title, pubdate;

        public MdLObject() {
        }

        public MdLObject(String content_encoded, String dc_creator, String title, String pubdate) {
            this.content_encoded = content_encoded;
            this.dc_creator = dc_creator;
            this.title = title;
            this.pubdate = pubdate;
        }
    }
}