package me.fliife.colbert.utils;

import android.os.AsyncTask;

public class GetScriptAsyncTask extends AsyncTask<String, Void, String> {

    private final Callbacks.Callback<String> callback;
    private final CredentialsHolder credentialsHolder;

    public GetScriptAsyncTask(Callbacks.Callback<String> callback, CredentialsHolder credentialsHolder) {
        super();
        this.callback = callback;
        this.credentialsHolder = credentialsHolder;
    }

    @Override
    protected String doInBackground(String... paths) {
        if (paths.length == 0) return "";
        String path = paths[0];
        String script = "var username = \"" + credentialsHolder.username.replaceAll("\"", "") + "\";"
                + "var password = \"" + credentialsHolder.password.replaceAll("\"", "") + "\";"
                + AssetReader.readAsset(path, credentialsHolder.context).replaceAll("//.*\n", "");
        return script;
    }

    @Override
    protected void onPostExecute(String script) {
        callback.onCallback(script);
    }
}
