package com.openwhisper.android.testing;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingRegistry;

import com.openwhisper.android.OpenWhisperApp;
import com.openwhisper.android.data.UserSession;

import org.junit.After;
import org.junit.Before;

import okhttp3.mockwebserver.MockWebServer;

/** Starts MockWebServer, points the app at it, and syncs Espresso with OkHttp. */
public abstract class InstrumentedTestBase {

    protected MockWebServer server;
    protected MockApiDispatcher dispatcher;
    private OkHttpIdlingResource idlingResource;

    @Before
    public void baseSetUp() throws Exception {
        OpenWhisperApp app = app();
        app.settings().setCustomInstanceUrl("");
        app.network().tokenStore().clear();
        UserSession.clear();

        dispatcher = new MockApiDispatcher();
        server = new MockWebServer();
        server.setDispatcher(dispatcher);
        server.start();

        app.settings().setCustomInstanceUrl(server.url("/api/").toString());
        app.recreateNetworkModule();

        idlingResource =
                OkHttpIdlingResource.create("okhttp", app.network().okHttpClient());
        IdlingRegistry.getInstance().register(idlingResource);
    }

    @After
    public void baseTearDown() throws Exception {
        if (idlingResource != null) {
            IdlingRegistry.getInstance().unregister(idlingResource);
            idlingResource = null;
        }
        if (server != null) {
            server.shutdown();
            server = null;
        }
        OpenWhisperApp app = app();
        app.settings().setCustomInstanceUrl("");
        app.network().tokenStore().clear();
        UserSession.clear();
        app.recreateNetworkModule();
    }

    protected static OpenWhisperApp app() {
        return ApplicationProvider.getApplicationContext();
    }

    protected void seedAuthenticatedSession() {
        OpenWhisperApp app = app();
        app.network().tokenStore().saveTokens(TestFixtures.ACCESS_TOKEN, TestFixtures.REFRESH_TOKEN);
        app.network().tokenStore().saveUsername("alice");
        UserSession.setUsername("alice");
    }
}
