package com.openwhisper.android.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ApiConfigTest {

    @Test
    public void normalizeCustomUrl_addsHttpsAndApiPath() {
        String url = ApiConfig.normalizeCustomUrl("example.com");
        assertTrue(url.startsWith("https://"));
        assertTrue(url.endsWith("/api/"));
    }

    @Test
    public void normalizeCustomUrl_preservesExistingPath() {
        String url = ApiConfig.normalizeCustomUrl("https://chat.example.com/custom/");
        assertEquals("https://chat.example.com/custom/", url);
    }

    @Test
    public void normalizeCustomUrl_emptyUsesBuildConfigDefault() {
        String url = ApiConfig.normalizeCustomUrl("");
        assertTrue(url.contains("/api/"));
    }
}
