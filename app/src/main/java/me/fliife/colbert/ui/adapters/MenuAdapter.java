package me.fliife.colbert.ui.adapters;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.common.collect.Iterables;
import com.google.firebase.database.DataSnapshot;
import me.fliife.colbert.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.ViewHolder> {
    private JSONArray dataset;

    public MenuAdapter(JSONArray dataset) {
        this.dataset = dataset;
    }

    @Override
    public MenuAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_layout, parent, false);

        TextView content = v.findViewById(R.id.menu_content);
        TextView date = v.findViewById(R.id.menu_date);
        CardView card = v.findViewById(R.id.card_menu);

        ViewHolder vh = new ViewHolder(content, card, date);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        try {
            final JSONObject currentObj = dataset.getJSONObject(position);
            holder.date.setText(currentObj.getString("title"));
            String content = currentObj.getString("content");
            holder.content.setText(content);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return dataset.length();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView content, date;
        public CardView card;

        public ViewHolder(TextView content, CardView card, TextView date) {
            super(content.getRootView());
            this.content = content;
            this.card = card;
            this.date = date;
        }
    }
}