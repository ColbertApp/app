package me.fliife.colbert.ui.fragments;


import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.graphics.drawable.Animatable2Compat;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import me.fliife.colbert.R;
import me.fliife.colbert.ui.adapters.MenuAdapter;
import org.json.JSONArray;

/**
 * A simple {@link Fragment} subclass.
 */
public class MenuFragment extends Fragment {


    public MenuFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_menu, container, false);

        final ImageView loader = rootView.findViewById(R.id.menu_loader);
        final AnimatedVectorDrawableCompat loading = AnimatedVectorDrawableCompat.create(getContext(), R.drawable.loading);
        loader.setImageDrawable(loading);
        loading.registerAnimationCallback(new Animatable2Compat.AnimationCallback() {
            @Override
            public void onAnimationEnd(Drawable drawable) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        loading.start();
                    }
                });
            }
        });
        loading.start();

        final RecyclerView rv = rootView.findViewById(R.id.menu_rv);

        RequestQueue queue = Volley.newRequestQueue(getContext());
        JsonArrayRequest menuRequest = new JsonArrayRequest(Request.Method.GET, "https://colbert-app.firebaseapp.com/res.json", null, new Response.Listener<JSONArray>(){
            @Override
            public void onResponse(JSONArray response) {
                rv.setLayoutManager(new LinearLayoutManager(getContext()));
                rv.setAdapter(new MenuAdapter(response));
                loader.setVisibility(View.GONE);
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO: Error message
            }
        });
        queue.add(menuRequest);

        return rootView;
    }

}
