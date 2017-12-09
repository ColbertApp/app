package me.fliife.colbert.utils;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import me.fliife.colbert.network.API;
import me.fliife.colbert.storage.StorageUtils;
import org.json.JSONObject;

public class PronoteService extends IntentService {

    public static String BROADCAST_SERVICE_FINISHED = "xyz.fliife.pronote.BROADCAST_SERVICE_FINISHED";
    public static String SERVICE_ACTION_FETCH = "xyz.fliife.pronote.SERVICE_ACTION_FETCH";
    public static String SERVICE_ACTION_GET_FROM_DATABASE = "xyz.fliife.pronote.SERVICE_ACTION_GET_FROM_DATABASE";

    public PronoteService() {
        super("PronoteService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getStringExtra("action");
        System.out.println("Action is " + action);
        if (action.equals(SERVICE_ACTION_FETCH)) doFetch();
        if (action.equals(SERVICE_ACTION_GET_FROM_DATABASE)) getFromDB();
    }

    private void getFromDB() {
        System.out.println("Getting data from database");
        final Context context = getApplicationContext();
        CredentialsHolder credentials = StorageUtils.getCredentials(context);
        if (!credentials.username.equals("")) {
            final API api = API.getInstance(context);
            api.setCredentials(credentials);
            api.setLastPronoteObject(StorageUtils.getPronoteObjectFromDatabase(context));
            // Broadcast the success
            Intent endIntent = new Intent(BROADCAST_SERVICE_FINISHED);
            endIntent.putExtra("success", true);
            // Let random readers know.
            System.out.println("Broadcasting intent");
            LocalBroadcastManager.getInstance(context).sendBroadcast(endIntent);
        }
    }

    private void doFetch() {
        System.out.println("Fetching");
        final Context context = getApplicationContext();
        CredentialsHolder credentials = StorageUtils.getCredentials(context);
        if (!credentials.username.equals("")) {
            final API api = API.getInstance(context);
            api.setCredentials(credentials);
            api.fetch(new Callbacks.SimpleCallback() {
                @Override
                public void onCallback() {
                    JSONObject lastFetched = api.getLastFetched();

                    // Broadcast the success
                    Intent endIntent = new Intent(BROADCAST_SERVICE_FINISHED);
                    endIntent.putExtra("success", lastFetched != null);

                    // Let random readers know.
                    System.out.println("Broadcasting intent");
                    LocalBroadcastManager.getInstance(context).sendBroadcast(endIntent);

                    // Put the fetched content into the database
                    StorageUtils.clearDatabase(context);
                    StorageUtils.putPronoteObjectIntoDatabase(context, api.getLastPronoteObject());
                }

                @Override
                public void onFail() {
                    // Spread the failure !
                    Intent endIntent = new Intent(BROADCAST_SERVICE_FINISHED);
                    endIntent.putExtra("success", false);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(endIntent);
                }
            });
        }
    }
}
