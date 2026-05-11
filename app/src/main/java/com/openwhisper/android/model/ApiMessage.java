package com.openwhisper.android.model;

import com.google.gson.annotations.SerializedName;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ApiMessage {
    private static final Pattern MSG_ID = Pattern.compile("/messages/(\\d+)/?$");

    @SerializedName("url")
    public String url;

    @SerializedName("sender")
    public Sender sender;

    @SerializedName("content")
    public String content;

    @SerializedName("attachment_url")
    public String attachmentUrl;

    @SerializedName("created_at")
    public String createdAt;

    public long messageId() {
        if (url == null) {
            return -1L;
        }
        Matcher m = MSG_ID.matcher(url);
        if (m.find()) {
            return Long.parseLong(m.group(1));
        }
        return -1L;
    }

    public String senderUsername() {
        return sender != null && sender.username != null ? sender.username : "";
    }

    public static final class Sender {
        @SerializedName("username")
        public String username;
    }
}
