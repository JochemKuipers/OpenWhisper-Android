package com.openwhisper.android;

import android.app.Application;

import com.openwhisper.android.data.ApiConfig;
import com.openwhisper.android.data.NetworkModule;
import com.openwhisper.android.data.SettingsStore;
import com.openwhisper.android.data.UserSession;
import com.openwhisper.android.util.AppTheme;

public class OpenWhisperApp extends Application {

    private SettingsStore settingsStore;
    private NetworkModule networkModule;

    @Override
    public void onCreate() {
        super.onCreate();
        settingsStore = new SettingsStore(this);
        AppTheme.applyNightMode(settingsStore);
        recreateNetworkModule();
    }

    public SettingsStore settings() {
        return settingsStore;
    }

    public NetworkModule network() {
        return networkModule;
    }

    public void recreateNetworkModule() {
        ApiConfig apiConfig = new ApiConfig(this);
        networkModule = new NetworkModule(this, apiConfig);
        String username = networkModule.tokenStore().getUsername();
        if (username != null) {
            UserSession.setUsername(username);
        }
    }

    public void applyThemeSettings() {
        AppTheme.applyNightMode(settingsStore);
    }
}
