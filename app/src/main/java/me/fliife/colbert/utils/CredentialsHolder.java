package me.fliife.colbert.utils;

import android.content.Context;

public class CredentialsHolder {
    public Context context;
    public String username;
    public String password;
    public String url;

    public CredentialsHolder(Context context, String username, String password, String url) {
        this.context = context;
        this.username = username;
        this.password = password;
        this.url = url;
    }
}
