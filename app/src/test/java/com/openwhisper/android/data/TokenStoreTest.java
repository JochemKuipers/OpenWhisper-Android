package com.openwhisper.android.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 35)
public class TokenStoreTest {

    private TokenStore tokenStore;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        tokenStore = new TokenStore(context);
        tokenStore.clear();
    }

    @Test
    public void saveAndReadTokens() {
        tokenStore.saveTokens("access", "refresh");
        assertEquals("access", tokenStore.getAccessToken());
        assertEquals("refresh", tokenStore.getRefreshToken());
        assertTrue(tokenStore.hasAccess());
    }

    @Test
    public void clearRemovesTokens() {
        tokenStore.saveTokens("access", "refresh");
        tokenStore.clear();
        assertFalse(tokenStore.hasAccess());
    }
}
