package com.openwhisper.android.testing;

import androidx.annotation.NonNull;
import androidx.test.espresso.IdlingResource;

import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.OkHttpClient;

/** Espresso sync for in-flight OkHttp calls (androidx-compatible). */
public final class OkHttpIdlingResource implements IdlingResource {

    private final String name;
    private final OkHttpClient client;
    private volatile ResourceCallback callback;
    private final AtomicBoolean idle = new AtomicBoolean(true);

    private OkHttpIdlingResource(String name, OkHttpClient client) {
        this.name = name;
        this.client = client;
        client.dispatcher().setIdleCallback(this::onDispatcherIdle);
    }

    public static OkHttpIdlingResource create(@NonNull String name, @NonNull OkHttpClient client) {
        return new OkHttpIdlingResource(name, client);
    }

    private void onDispatcherIdle() {
        idle.set(true);
        ResourceCallback cb = callback;
        if (cb != null) {
            cb.onTransitionToIdle();
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isIdleNow() {
        boolean running =
                client.dispatcher().runningCallsCount() > 0
                        || client.dispatcher().queuedCallsCount() > 0;
        idle.set(!running);
        return !running;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
        callback = resourceCallback;
    }
}
