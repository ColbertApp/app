package me.fliife.colbert.utils;

import com.android.volley.VolleyError;
import org.json.JSONObject;

public class Callbacks {
    public interface SimpleCallback {
        void onCallback();

        void onFail();
    }

    public interface VolleyCallback {
        void onResponse(JSONObject response);

        void onFail(VolleyError error);
    }

    public interface FragmentCallback {
        void onCallback(PronoteObject pronoteObject);
    }

    public interface Callback<T> {
        void onCallback(T... args);
    }
}
