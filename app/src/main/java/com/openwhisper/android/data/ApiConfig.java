package com.openwhisper.android.data;

import com.openwhisper.android.BuildConfig;

import okhttp3.HttpUrl;

/** Resolves the API base URL from {@link BuildConfig#API_BASE_URL}. */
public final class ApiConfig {

    private static final HttpUrl BASE = parseBaseUrl();

    private ApiConfig() {}

    private static HttpUrl parseBaseUrl() {
        //noinspection HttpUrls
        HttpUrl url = HttpUrl.parse(BuildConfig.API_BASE_URL);
        if (url == null) {
            throw new IllegalStateException("Invalid API_BASE_URL");
        }
        return url;
    }

    public static HttpUrl baseUrl() {
        return BASE;
    }

    public static HttpUrl resolve(String path) {
        HttpUrl resolved = BASE.resolve(path);
        if (resolved == null) {
            throw new IllegalStateException("Invalid API path: " + path);
        }
        return resolved;
    }
}
