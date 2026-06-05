package com.openwhisper.android.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class NetworkModuleTest {

    private NetworkModule network;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        context.getSharedPreferences("openwhisper_settings", Context.MODE_PRIVATE).edit().clear().apply();
        context.getSharedPreferences("openwhisper_auth", Context.MODE_PRIVATE).edit().clear().apply();
        ApiConfig apiConfig = new ApiConfig(context);
        network = new NetworkModule(context, apiConfig);
    }

    @Test
    public void webSocketUrl_usesWsSchemeForHttpBase() {
        network.tokenStore().saveTokens("tok", "ref");
        String url = network.webSocketUrl(7);
        assertTrue(url.startsWith("ws://"));
        assertTrue(url.contains("/ws/chats/7/"));
        assertTrue(url.contains("token=tok"));
    }

    @Test
    public void socialWebSocketUrl_includesSocialPath() {
        network.tokenStore().saveTokens("social-tok", "ref");
        String url = network.socialWebSocketUrl();
        assertTrue(url.contains("/ws/social/"));
        assertTrue(url.contains("token=social-tok"));
    }

    @Test
    public void resolveAttachmentUrl_resolvesRelativeMediaPath() {
        String resolved = network.apiConfig().resolveAttachmentUrl("/media/file.jpg");
        assertEquals(true, resolved.contains("/media/file.jpg"));
    }
}
