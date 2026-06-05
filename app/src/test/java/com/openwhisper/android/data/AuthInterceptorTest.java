package com.openwhisper.android.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

@RunWith(RobolectricTestRunner.class)
public class AuthInterceptorTest {

    private MockWebServer server;
    private Context context;

    @Before
    public void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
        context = ApplicationProvider.getApplicationContext();
    }

    @Test
    public void addsAuthorizationHeaderWhenTokenPresent() throws Exception {
        server.enqueue(new MockResponse().setBody("ok"));
        TokenStore tokenStore = new TokenStore(context);
        tokenStore.clear();
        tokenStore.saveTokens("access-123", "refresh-456");

        OkHttpClient client =
                new OkHttpClient.Builder()
                        .addInterceptor(new AuthInterceptor(tokenStore))
                        .build();

        client.newCall(new Request.Builder().url(server.url("/test")).build()).execute().close();

        RecordedRequest request = server.takeRequest(1, TimeUnit.SECONDS);
        assertEquals("Bearer access-123", request.getHeader("Authorization"));
    }

    @Test
    public void omitsAuthorizationWhenNoToken() throws Exception {
        server.enqueue(new MockResponse().setBody("ok"));
        TokenStore tokenStore = new TokenStore(context);
        tokenStore.clear();

        OkHttpClient client =
                new OkHttpClient.Builder()
                        .addInterceptor(new AuthInterceptor(tokenStore))
                        .build();

        client.newCall(new Request.Builder().url(server.url("/test")).build()).execute().close();

        RecordedRequest request = server.takeRequest(1, TimeUnit.SECONDS);
        assertNull(request.getHeader("Authorization"));
    }
}
