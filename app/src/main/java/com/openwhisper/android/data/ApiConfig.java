package com.openwhisper.android.data;

import android.content.Context;

import com.openwhisper.android.BuildConfig;

import okhttp3.HttpUrl;

/** Resolves the API base URL from settings or {@link BuildConfig#API_BASE_URL}. */
public final class ApiConfig {

    private final HttpUrl base;

    public ApiConfig(Context context) {
        SettingsStore settings = new SettingsStore(context);
        base = parseBaseUrl(settings.getCustomInstanceUrl());
    }

    private static HttpUrl parseBaseUrl(String customUrl) {
        String raw =
                customUrl == null || customUrl.isBlank()
                        ? BuildConfig.API_BASE_URL
                        : normalizeCustomUrl(customUrl);
        //noinspection HttpUrls
        HttpUrl url = HttpUrl.parse(raw);
        if (url == null) {
            throw new IllegalStateException("Invalid API base URL: " + raw);
        }
        return url;
    }

    /** Normalizes user input into a Retrofit-compatible API base URL ending with {@code /}. */
    public static String normalizeCustomUrl(String input) {
        String trimmed = input.trim();
        if (trimmed.isEmpty()) {
            return BuildConfig.API_BASE_URL;
        }
        if (!trimmed.contains("://")) {
            trimmed = "https://" + trimmed;
        }
        HttpUrl parsed = HttpUrl.parse(trimmed);
        if (parsed == null) {
            throw new IllegalArgumentException("Invalid URL");
        }
        HttpUrl.Builder builder = parsed.newBuilder();
        String path = parsed.encodedPath();
        if (path.isEmpty() || "/".equals(path)) {
            builder.encodedPath("/api/");
        } else if (!path.endsWith("/")) {
            builder.encodedPath(path + "/");
        }
        return builder.build().toString();
    }

    public HttpUrl baseUrl() {
        return base;
    }

    public HttpUrl resolve(String path) {
        HttpUrl resolved = base.resolve(path);
        if (resolved == null) {
            throw new IllegalStateException("Invalid API path: " + path);
        }
        return resolved;
    }
}
