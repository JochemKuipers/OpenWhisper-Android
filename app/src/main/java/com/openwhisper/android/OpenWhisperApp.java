package com.openwhisper.android;

import android.app.Application;

import com.openwhisper.android.data.NetworkModule;
import com.openwhisper.android.data.UserSession;

public class OpenWhisperApp extends Application {

    private NetworkModule networkModule;

    @Override
    public void onCreate() {
        super.onCreate();
        networkModule = new NetworkModule(this);
        String username = networkModule.tokenStore().getUsername();
        if (username != null) {
            UserSession.setUsername(username);
        }
    }

    public NetworkModule network() {
        return networkModule;
    }
}
