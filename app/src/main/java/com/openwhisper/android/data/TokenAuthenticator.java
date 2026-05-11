package com.openwhisper.android.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.Collections;

import okhttp3.Authenticator;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;
import com.openwhisper.android.model.TokenResponse;

final class TokenAuthenticator implements Authenticator {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final TokenStore tokenStore;
    private final String apiBaseUrl;
    private final Gson gson = new Gson();

    TokenAuthenticator(TokenStore tokenStore, String apiBaseUrl) {
        this.tokenStore = tokenStore;
        this.apiBaseUrl = apiBaseUrl;
    }

    @Nullable
    @Override
    public Request authenticate(@Nullable Route route, @NonNull Response response) throws IOException {
        if (response.request().url().encodedPath().contains("token/refresh")) {
            tokenStore.clear();
            UserSession.clear();
            return null;
        }
        if (responseCount(response) > 2) {
            return null;
        }

        String refresh = tokenStore.getRefreshToken();
        if (refresh == null || refresh.isEmpty()) {
            tokenStore.clear();
            UserSession.clear();
            return null;
        }

        String json = gson.toJson(Collections.singletonMap("refresh", refresh));
        Request refreshReq =
                new Request.Builder()
                        .url(apiBaseUrl + "token/refresh/")
                        .post(RequestBody.create(json, JSON))
                        .header("Accept", "application/json")
                        .build();

        OkHttpClient plain = new OkHttpClient();
        try (Response r = plain.newCall(refreshReq).execute()) {
            if (!r.isSuccessful() || r.body() == null) {
                tokenStore.clear();
                UserSession.clear();
                return null;
            }
            TokenResponse tr = gson.fromJson(r.body().string(), TokenResponse.class);
            if (tr == null || tr.access == null || tr.access.isEmpty()) {
                tokenStore.clear();
                UserSession.clear();
                return null;
            }
            if (tr.refresh != null && !tr.refresh.isEmpty()) {
                tokenStore.saveTokens(tr.access, tr.refresh);
            } else {
                tokenStore.updateAccessToken(tr.access);
            }
        }

        return response.request().newBuilder().header("Authorization", "Bearer " + tokenStore.getAccessToken()).build();
    }

    private static int responseCount(Response response) {
        int c = 1;
        Response p = response.priorResponse();
        while (p != null) {
            c++;
            p = p.priorResponse();
        }
        return c;
    }
}
