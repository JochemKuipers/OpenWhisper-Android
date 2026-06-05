package com.openwhisper.android.data;

import android.content.Context;
import android.content.SharedPreferences;

public final class SettingsStore {

    public static final String THEME_SYSTEM = "system";
    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";

    private static final String PREFS = "openwhisper_settings";
    private static final String KEY_THEME_MODE = "theme_mode";
    private static final String KEY_DYNAMIC_COLORS = "dynamic_colors";
    private static final String KEY_CUSTOM_INSTANCE_URL = "custom_instance_url";

    private final SharedPreferences prefs;

    public SettingsStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public String getThemeMode() {
        return prefs.getString(KEY_THEME_MODE, THEME_SYSTEM);
    }

    public void setThemeMode(String mode) {
        prefs.edit().putString(KEY_THEME_MODE, mode).apply();
    }

    public boolean isDynamicColorsEnabled() {
        return prefs.getBoolean(KEY_DYNAMIC_COLORS, false);
    }

    public void setDynamicColorsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_DYNAMIC_COLORS, enabled).apply();
    }

    public String getCustomInstanceUrl() {
        return prefs.getString(KEY_CUSTOM_INSTANCE_URL, "");
    }

    public void setCustomInstanceUrl(String url) {
        prefs.edit().putString(KEY_CUSTOM_INSTANCE_URL, url != null ? url.trim() : "").apply();
    }
}
