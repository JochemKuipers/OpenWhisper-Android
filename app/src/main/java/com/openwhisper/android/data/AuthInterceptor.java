package com.openwhisper.android.data;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

final class AuthInterceptor implements Interceptor {

    private final TokenStore tokenStore;

    AuthInterceptor(TokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request.Builder b = chain.request().newBuilder();
        String token = tokenStore.getAccessToken();
        if (token != null && !token.isEmpty()) {
            b.header("Authorization", "Bearer " + token);
        }
        return chain.proceed(b.build());
    }
}
