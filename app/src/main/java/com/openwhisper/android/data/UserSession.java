package com.openwhisper.android.data;

import androidx.annotation.Nullable;

/**
 * Holds the signed-in username in memory (from {@code GET /api/users/me/}) for message alignment.
 */
public final class UserSession {

    @Nullable
    private static volatile String currentUsername;

    private UserSession() {}

    public static void setUsername(@Nullable String username) {
        currentUsername = username;
    }

    @Nullable
    public static String getUsername() {
        return currentUsername;
    }

    public static void clear() {
        currentUsername = null;
    }
}
