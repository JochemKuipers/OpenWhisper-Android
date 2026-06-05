package com.openwhisper.android.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

public class InstanceUrlCheckerTest {

    private MockWebServer server;

    @Before
    public void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
    }

    @After
    public void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    public void healthCheckUrl_appendsHealthToApiBase() {
        assertEquals(
                "http://localhost:8000/api/health/",
                InstanceUrlChecker.healthCheckUrl("http://localhost:8000/api/"));
    }

    @Test
    public void healthCheckUrl_returnsNullForInvalidBase() {
        assertNull(InstanceUrlChecker.healthCheckUrl("not a url"));
    }

    @Test
    public void verifyReachable_succeedsWhenHealthReturnsOk() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{\"status\":\"ok\"}"));
        AtomicBoolean reachable = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);

        InstanceUrlChecker.verifyReachable(
                server.url("/api/").toString(),
                new InstanceUrlChecker.Listener() {
                    @Override
                    public void onReachable() {
                        reachable.set(true);
                        latch.countDown();
                    }

                    @Override
                    public void onFailure() {
                        latch.countDown();
                    }
                });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertTrue(reachable.get());

        RecordedRequest request = server.takeRequest(1, TimeUnit.SECONDS);
        Assert.assertNotNull(request);
        assertEquals("/api/health/", request.getPath());
    }

    @Test
    public void verifyReachable_failsWhenHealthReturnsError() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(503));
        AtomicBoolean failed = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);

        InstanceUrlChecker.verifyReachable(
                server.url("/api/").toString(),
                new InstanceUrlChecker.Listener() {
                    @Override
                    public void onReachable() {
                        latch.countDown();
                    }

                    @Override
                    public void onFailure() {
                        failed.set(true);
                        latch.countDown();
                    }
                });

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertTrue(failed.get());
    }

    @Test
    public void verifyReachable_failsWhenServerUnreachable() throws Exception {
        String deadUrl = "http://127.0.0.1:" + server.getPort() + "/api/";
        server.shutdown();

        AtomicBoolean failed = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);

        InstanceUrlChecker.verifyReachable(
                deadUrl,
                new InstanceUrlChecker.Listener() {
                    @Override
                    public void onReachable() {
                        latch.countDown();
                    }

                    @Override
                    public void onFailure() {
                        failed.set(true);
                        latch.countDown();
                    }
                });

        assertTrue(latch.await(15, TimeUnit.SECONDS));
        assertTrue(failed.get());
    }
}
