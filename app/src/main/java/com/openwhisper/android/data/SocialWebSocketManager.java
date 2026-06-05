package com.openwhisper.android.data;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.openwhisper.android.model.WsSocialEvent;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public final class SocialWebSocketManager {

    public interface Listener {
        void onSocialEvent(@NonNull WsSocialEvent event);
    }

    private final NetworkModule networkModule;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final List<Listener> listeners = new CopyOnWriteArrayList<>();

    private WebSocket webSocket;
    private boolean started;
    private int reconnectAttempt;

    public SocialWebSocketManager(NetworkModule networkModule) {
        this.networkModule = networkModule;
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public void start() {
        if (started) {
            return;
        }
        started = true;
        reconnectAttempt = 0;
        connect();
    }

    public void stop() {
        started = false;
        reconnectAttempt = 0;
        if (webSocket != null) {
            webSocket.close(1000, null);
            webSocket = null;
        }
    }

    private void connect() {
        if (!started || !networkModule.tokenStore().hasAccess()) {
            return;
        }
        OkHttpClient client = networkModule.okHttpClient();
        Request request = new Request.Builder().url(networkModule.socialWebSocketUrl()).build();
        webSocket =
                client.newWebSocket(
                        request,
                        new WebSocketListener() {
                            @Override
                            public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
                                reconnectAttempt = 0;
                            }

                            @Override
                            public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
                                handleMessage(text);
                            }

                            @Override
                            public void onMessage(@NonNull WebSocket webSocket, @NonNull ByteString bytes) {}

                            @Override
                            public void onFailure(
                                    @NonNull WebSocket webSocket, @NonNull Throwable t, @Nullable Response response) {
                                scheduleReconnect();
                            }

                            @Override
                            public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                                if (started) {
                                    scheduleReconnect();
                                }
                            }
                        });
    }

    private void handleMessage(String text) {
        try {
            WsSocialEvent event = networkModule.gson().fromJson(text, WsSocialEvent.class);
            if (event == null || event.type == null || event.type.isBlank()) {
                return;
            }
            mainHandler.post(
                    () -> {
                        for (Listener listener : listeners) {
                            listener.onSocialEvent(event);
                        }
                    });
        } catch (Exception ignored) {
            // ignore malformed frames
        }
    }

    private void scheduleReconnect() {
        if (!started) {
            return;
        }
        webSocket = null;
        reconnectAttempt++;
        long delayMs = Math.min(30_000L, 1_000L * (1L << Math.min(reconnectAttempt, 5)));
        mainHandler.postDelayed(this::connect, delayMs);
    }
}
