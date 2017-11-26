package me.fliife.colbert.utils;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.util.concurrent.TimeUnit;

public class VolleySingleton {
    private static VolleySingleton instance;
    private RequestQueue mRequestQueue;

    private VolleySingleton() {
        mRequestQueue = getRequestQueue();
    }

    public static VolleySingleton getInstance() {
        return instance = instance == null ? new VolleySingleton() : instance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // Get application's context in order not to make tons of different instances
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        //Disable caching. The api, by its nature is subject to potential change on each request.
        req.setShouldCache(false);
        req.setRetryPolicy(new DefaultRetryPolicy((int) TimeUnit.SECONDS.toMillis(45), 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //Add the request to queue
        getRequestQueue().add(req);
    }

}