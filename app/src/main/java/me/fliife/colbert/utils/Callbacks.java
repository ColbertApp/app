package me.fliife.colbert.utils;

public class Callbacks {
    public interface SimpleCallback {
        void onCallback();

        void onFail();
    }

    public interface Callback<T> {
        void onCallback(T... args);
    }
}
