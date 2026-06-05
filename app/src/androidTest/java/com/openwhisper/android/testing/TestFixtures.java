package com.openwhisper.android.testing;

/** Shared JSON bodies and tokens for instrumented tests. */
public final class TestFixtures {

    public static final String ACCESS_TOKEN =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIn0.test";
    public static final String REFRESH_TOKEN = "refresh-token-value";

    private TestFixtures() {}

    public static String tokenResponseJson() {
        return "{\"access\":\"" + ACCESS_TOKEN + "\",\"refresh\":\"" + REFRESH_TOKEN + "\"}";
    }

    public static String userMeJson() {
        return "{\"username\":\"alice\"}";
    }

    public static String chatsListJson() {
        return "["
                + "{"
                + "\"url\":\"http://example.com/api/chats/1/\","
                + "\"title\":\"\","
                + "\"display_title\":\"bob\","
                + "\"member_subtitle\":\"\","
                + "\"users\":[{\"username\":\"alice\"},{\"username\":\"bob\"}],"
                + "\"admin_username\":\"alice\","
                + "\"is_admin\":true"
                + "},"
                + "{"
                + "\"url\":\"http://example.com/api/chats/2/\","
                + "\"title\":\"\","
                + "\"display_title\":\"New group chat\","
                + "\"member_subtitle\":\"You, bob, carol\","
                + "\"users\":[{\"username\":\"alice\"},{\"username\":\"bob\"},{\"username\":\"carol\"}],"
                + "\"admin_username\":\"alice\","
                + "\"is_admin\":true"
                + "}"
                + "]";
    }

    public static String friendRequestsJson() {
        return "{\"incoming\":[{\"username\":\"eve\"}],\"outgoing\":[{\"username\":\"frank\"}]}";
    }

    public static String friendsListJson() {
        return "[{\"username\":\"bob\"}]";
    }

    public static String loginErrorJson() {
        return "{\"detail\":\"Invalid credentials\"}";
    }
}
