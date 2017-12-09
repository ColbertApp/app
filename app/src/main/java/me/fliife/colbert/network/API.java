package me.fliife.colbert.network;

import android.content.Context;
import me.fliife.colbert.storage.StorageUtils;
import me.fliife.colbert.utils.Callbacks;
import me.fliife.colbert.utils.CredentialsHolder;
import me.fliife.colbert.utils.GetScriptAsyncTask;
import me.fliife.colbert.utils.PronoteObject;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class API {
    public static Context context;
    public static String COLBERT_URL = "http://etablissement.lyceecolbert-tg.org/pronote/mobile.eleve.html?fd=1";
    private static API instance = null;
    private PronoteObject lastPronoteObject;
    private CredentialsHolder credentials;
    private JSONObject lastFetched;

    private API(Context context) {
        API.context = context;
    }

    public static API getInstance(Context context) {
        return instance = instance == null ? new API(context) : instance;
    }

    public void setCredentials(CredentialsHolder credentials) {
        this.credentials = credentials;
    }

    public PronoteObject getLastPronoteObject() {
        return lastPronoteObject;
    }

    public void setLastPronoteObject(PronoteObject pronoteObject) {
        this.lastPronoteObject = pronoteObject;
    }

    public JSONObject getLastFetched() {
        return lastFetched;
    }

    public void setLastFetched(JSONObject obj) {
        this.lastFetched = obj;
    }

    public void fetch(final Callbacks.SimpleCallback simpleCallback) {
        System.out.println("Starting API fetch");
        credentials.url = credentials.url.replace("mobile.", "");
        new GetScriptAsyncTask(new Callbacks.Callback<String>() {
            @Override
            public void onCallback(String... scripts) {
                new ScriptRunner(scripts[0], credentials.url, credentials.context, new Callbacks.Callback<Object>() {
                    @Override
                    public void onCallback(Object[] args) {
                        try {
                            JSONObject result = (JSONObject) new JSONTokener(args[0].toString()).nextValue();
                            setLastFetched(result);
                            setLastPronoteObject(StorageUtils.jsonToPronoteObject(result));
                            simpleCallback.onCallback();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            simpleCallback.onFail();
                        }
                    }
                }).execute();
            }
        }, credentials).execute("app.js");
    }

    public void login(final Callbacks.Callback<Boolean> callback) {
        // Get the script
        new GetScriptAsyncTask(new Callbacks.Callback<String>() {
            @Override
            public void onCallback(String... scripts) {
                // Run the script
                new ScriptRunner(scripts[0], credentials.url, credentials.context, new Callbacks.Callback<Object>() {
                    @Override
                    public void onCallback(final Object... successArray) {
                        boolean success = Boolean.valueOf(successArray[0].toString());
                        callback.onCallback(success);
                    }
                }).execute();
            }
        }, credentials).execute("checkLogin.js");
    }
}
