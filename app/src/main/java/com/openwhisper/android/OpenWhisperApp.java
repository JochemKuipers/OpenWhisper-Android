package com.openwhisper.android;

import android.app.Application;

import com.openwhisper.android.data.NetworkModule;

public class OpenWhisperApp extends Application {

    private NetworkModule networkModule;

    @Override
    public void onCreate() {
        super.onCreate();
        networkModule = new NetworkModule(this);
    }

    public NetworkModule network() {
        return networkModule;
    }
}
