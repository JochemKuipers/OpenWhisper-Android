package com.openwhisper.android.data;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.openwhisper.android.BuildConfig;

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
                        .authenticator(new TokenAuthenticator(tokenStore, BuildConfig.API_BASE_URL))
                        .build();

        Retrofit retrofit =
                new Retrofit.Builder()
                        .baseUrl(BuildConfig.API_BASE_URL)
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

    /** Builds {@code ws://host/ws/chats/{id}/?token=...} from the HTTP API base URL. */
    public String webSocketUrl(int chatId) {
        HttpUrl api = HttpUrl.parse(BuildConfig.API_BASE_URL);
        if (api == null) {
            throw new IllegalStateException("Invalid API_BASE_URL");
        }
        HttpUrl siteRoot = api.resolve("/");
        if (siteRoot == null) {
            throw new IllegalStateException("Invalid API_BASE_URL");
        }
        String token = tokenStore.getAccessToken();
        HttpUrl httpUrl =
                siteRoot.newBuilder()
                        .encodedPath("/ws/chats/" + chatId + "/")
                        .addQueryParameter("token", token != null ? token : "")
                        .build();
        String url = httpUrl.toString();
        if (url.startsWith("https://")) {
            return "wss://" + url.substring(8);
        }
        if (url.startsWith("http://")) {
            return "ws://" + url.substring(7);
        }
        return url;
    }
}
