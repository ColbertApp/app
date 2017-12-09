package me.fliife.colbert.ui.fragments;

import android.graphics.drawable.Animatable2;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.google.firebase.database.*;
import me.fliife.colbert.R;
import me.fliife.colbert.ui.MDLAdapter;

public class MDLFragment extends Fragment {

    public MDLFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_fb_feed, container, false);

        final RecyclerView recyclerView = rootView.findViewById(R.id.fb_rv);

        //volleySingleton.addToRequestQueue();

        final ImageView loader = rootView.findViewById(R.id.fb_loader);
        final AnimatedVectorDrawable loading = (AnimatedVectorDrawable) loader.getDrawable();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            loading.registerAnimationCallback(new Animatable2.AnimationCallback() {
                @Override
                public void onAnimationEnd(Drawable drawable) {
                    ((Animatable2) drawable).start();
                }
            });
        }
        loading.start();

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference("mdl");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                MDLAdapter adapter = new MDLAdapter(dataSnapshot);
                recyclerView.setAdapter(adapter);
                loading.stop();
                loader.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println(databaseError.getMessage());
            }
        });

        return rootView;
    }

}
