package com.openwhisper.android.data;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class NetworkModule {

    private final TokenStore tokenStore;
    private final OkHttpClient okHttpClient;
    private final ApiService apiService;
    private final Gson gson;

    public NetworkModule(Context context) {
        tokenStore = new TokenStore(context);
        gson = new GsonBuilder().create();

        okHttpClient =
                new OkHttpClient.Builder()
                        .connectTimeout(20, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .addInterceptor(new AuthInterceptor(tokenStore))
                        .authenticator(new TokenAuthenticator(tokenStore))
                        .build();

        Retrofit retrofit =
                new Retrofit.Builder()
                        .baseUrl(ApiConfig.baseUrl())
                        .client(okHttpClient)
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .build();

        apiService = retrofit.create(ApiService.class);
    }

    public TokenStore tokenStore() {
        return tokenStore;
    }

    public ApiService api() {
        return apiService;
    }

    public OkHttpClient okHttpClient() {
        return okHttpClient;
    }

    public Gson gson() {
        return gson;
    }

    /** Builds a {@code ws(s)://host/ws/chats/{id}/?token=...} URL from the HTTP API base URL. */
    public String webSocketUrl(int chatId) {
        HttpUrl siteRoot = ApiConfig.resolve("/");
        String token = tokenStore.getAccessToken();
        HttpUrl httpUrl =
                siteRoot.newBuilder()
                        .encodedPath("/ws/chats/" + chatId + "/")
                        .addQueryParameter("token", token != null ? token : "")
                        .build();
        return toWebSocketUrl(httpUrl);
    }

    private static String toWebSocketUrl(HttpUrl httpUrl) {
        String wsScheme = httpUrl.isHttps() ? "wss" : "ws";
        StringBuilder url = new StringBuilder();
        url.append(wsScheme).append(':').append('/').append('/');
        url.append(httpUrl.host());
        int port = httpUrl.port();
        int defaultPort = httpUrl.isHttps() ? 443 : 80;
        if (port != defaultPort) {
            url.append(':').append(port);
        }
        url.append(httpUrl.encodedPath());
        String query = httpUrl.encodedQuery();
        if (query != null) {
            url.append('?').append(query);
        }
        return url.toString();
    }
}
