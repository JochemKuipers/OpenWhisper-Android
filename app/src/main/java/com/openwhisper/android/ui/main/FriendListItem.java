package com.openwhisper.android.ui.main;

public final class FriendListItem {

    public enum Kind {
        SECTION,
        FRIEND,
        INCOMING,
        OUTGOING,
        SEARCH
    }

    public final Kind kind;
    public final String username;
    public final String sectionTitle;

    private FriendListItem(Kind kind, String username, String sectionTitle) {
        this.kind = kind;
        this.username = username != null ? username : "";
        this.sectionTitle = sectionTitle != null ? sectionTitle : "";
    }

    public static FriendListItem section(String title) {
        return new FriendListItem(Kind.SECTION, "", title);
    }

    public static FriendListItem friend(String username) {
        return new FriendListItem(Kind.FRIEND, username, "");
    }

    public static FriendListItem incoming(String username) {
        return new FriendListItem(Kind.INCOMING, username, "");
    }

    public static FriendListItem outgoing(String username) {
        return new FriendListItem(Kind.OUTGOING, username, "");
    }

    public static FriendListItem search(String username) {
        return new FriendListItem(Kind.SEARCH, username, "");
    }
}
