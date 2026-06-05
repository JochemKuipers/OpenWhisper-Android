package com.openwhisper.android.util;

import android.content.Context;
import android.os.Build;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.color.DynamicColors;
import com.openwhisper.android.data.SettingsStore;

public final class AppTheme {

    private AppTheme() {}

    public static void applyNightMode(SettingsStore settings) {
        String mode = settings.getThemeMode();
        int nightMode =
                switch (mode) {
                    case SettingsStore.THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO;
                    case SettingsStore.THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES;
                    default -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                };
        AppCompatDelegate.setDefaultNightMode(nightMode);
    }

    /** Must run before {@code super.onCreate()} in each activity. */
    public static void applyToActivityIfEnabled(AppCompatActivity activity) {
        if (shouldUseDynamicColors(activity)) {
            DynamicColors.applyToActivityIfAvailable(activity);
        }
    }

    public static boolean shouldUseDynamicColors(Context context) {
        return isDynamicColorAvailable()
                && new SettingsStore(context).isDynamicColorsEnabled();
    }

    public static boolean isDynamicColorAvailable() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S;
    }
}
