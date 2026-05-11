package com.openwhisper.android.data;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

/**
 * Persists JWT access and refresh tokens per project specification (SharedPreferences).
 */
public final class TokenStore {

    private static final String PREFS = "openwhisper_auth";
    private static final String KEY_ACCESS = "access_token";
    private static final String KEY_REFRESH = "refresh_token";

    private final SharedPreferences prefs;

    public TokenStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void saveTokens(String access, String refresh) {
        prefs.edit().putString(KEY_ACCESS, access).putString(KEY_REFRESH, refresh).apply();
    }

    public void updateAccessToken(String access) {
        prefs.edit().putString(KEY_ACCESS, access).apply();
    }

    @Nullable
    public String getAccessToken() {
        return prefs.getString(KEY_ACCESS, null);
    }

    @Nullable
    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH, null);
    }

    public void clear() {
        prefs.edit().remove(KEY_ACCESS).remove(KEY_REFRESH).apply();
    }

    public boolean hasAccess() {
        String a = getAccessToken();
        return a != null && !a.isEmpty();
    }
}
