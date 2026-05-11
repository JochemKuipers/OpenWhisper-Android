package com.openwhisper.android.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ChatSummary {
    private static final Pattern CHAT_ID = Pattern.compile("/chats/(\\d+)/?$");

    @SerializedName("url")
    public String url;

    @SerializedName("title")
    public String title;

    @SerializedName("users")
    public List<UserRef> users;

    public int chatId() {
        if (url == null) {
            return -1;
        }
        Matcher m = CHAT_ID.matcher(url);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        return -1;
    }

    public String displayTitle() {
        if (title != null && !title.isBlank()) {
            return title.trim();
        }
        return "Chat";
    }

    public String memberSubtitle() {
        if (users == null || users.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < users.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            UserRef u = users.get(i);
            if (u != null && u.username != null) {
                sb.append(u.username);
            }
        }
        return sb.toString();
    }

    public static final class UserRef {
        @SerializedName("username")
        public String username;
    }
}
