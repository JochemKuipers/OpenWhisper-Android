package com.openwhisper.android.util;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/** Checks that an OpenWhisper server responds at {@code /api/health/}. */
public final class InstanceUrlChecker {

    private static final OkHttpClient CLIENT =
            new OkHttpClient.Builder()
                    .connectTimeout(12, TimeUnit.SECONDS)
                    .readTimeout(12, TimeUnit.SECONDS)
                    .followRedirects(true)
                    .build();

    public interface Listener {
        void onReachable();

        void onFailure();
    }

    private InstanceUrlChecker() {}

    public static void verifyReachable(@NonNull String apiBaseUrl, @NonNull Listener listener) {
        String healthUrl = healthCheckUrl(apiBaseUrl);
        if (healthUrl == null) {
            deliverMain(listener::onFailure);
            return;
        }

        Request request = new Request.Builder().url(healthUrl).get().build();
        CLIENT.newCall(request)
                .enqueue(
                        new okhttp3.Callback() {
                            @Override
                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                deliverMain(listener::onFailure);
                            }

                            @Override
                            public void onResponse(@NonNull Call call, @NonNull Response response) {
                                try (Response r = response) {
                                    if (r.isSuccessful()) {
                                        deliverMain(listener::onReachable);
                                    } else {
                                        deliverMain(listener::onFailure);
                                    }
                                }
                            }
                        });
    }

    @Nullable
    static String healthCheckUrl(@NonNull String apiBaseUrl) {
        HttpUrl base = HttpUrl.parse(apiBaseUrl);
        if (base == null) {
            return null;
        }
        HttpUrl health = base.resolve("health/");
        return health != null ? health.toString() : null;
    }

    private static void deliverMain(@NonNull Runnable action) {
        try {
            new Handler(Looper.getMainLooper()).post(action);
        } catch (RuntimeException e) {
            action.run();
        }
    }
}
