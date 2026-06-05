package com.openwhisper.android.testing;

import androidx.annotation.NonNull;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;

/** Routes common OpenWhisper REST endpoints to canned JSON responses. */
public final class MockApiDispatcher extends Dispatcher {

    public int loginStatusCode = 200;
    public String loginBody = TestFixtures.tokenResponseJson();
    public String meBody = TestFixtures.userMeJson();
    public String chatsBody = TestFixtures.chatsListJson();
    public String friendRequestsBody = TestFixtures.friendRequestsJson();
    public String friendsBody = TestFixtures.friendsListJson();

    @NonNull
    @Override
    public MockResponse dispatch(@NonNull RecordedRequest request) {
        String path = request.getPath();
        String method = request.getMethod();
        if (path == null || method == null) {
            return notFound();
        }

        if (path.contains("/token/refresh")) {
            return json(401, "{\"detail\":\"expired\"}");
        }

        if (path.contains("/token/") && "POST".equals(method)) {
            return json(loginStatusCode, loginBody);
        }

        if (path.contains("/users/me/friend-requests/") && "GET".equals(method)) {
            return json(200, friendRequestsBody);
        }

        if (path.contains("/users/me/friends/") && "GET".equals(method)) {
            return json(200, friendsBody);
        }

        if (path.contains("/users/me/") && "GET".equals(method)) {
            return json(200, meBody);
        }

        if (path.endsWith("/chats/") && "GET".equals(method)) {
            return json(200, chatsBody);
        }

        if (path.contains("/logout") && "POST".equals(method)) {
            return new MockResponse().setResponseCode(204);
        }

        return notFound();
    }

    private static MockResponse json(int code, String body) {
        return new MockResponse()
                .setResponseCode(code)
                .setBody(body)
                .addHeader("Content-Type", "application/json");
    }

    private static MockResponse notFound() {
        return new MockResponse().setResponseCode(404).setBody("{\"detail\":\"not found\"}");
    }
}
