package me.fliife.colbert.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PronoteBroadcastEventReceiver extends BroadcastReceiver {

    private Callbacks.SimpleCallback simpleCallback;

    public PronoteBroadcastEventReceiver(Callbacks.SimpleCallback singleCallback) {
        this.simpleCallback = singleCallback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean success = intent.getBooleanExtra("success", false);
        if (success) {
            simpleCallback.onCallback();
        } else {
            simpleCallback.onFail();
        }
    }
}
