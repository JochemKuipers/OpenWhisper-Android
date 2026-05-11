package com.openwhisper.android.model;

import com.google.gson.annotations.SerializedName;

public final class WsChatEvent {
    @SerializedName("type")
    public String type;

    @SerializedName("message_id")
    public long messageId;

    @SerializedName("chat_id")
    public long chatId;

    @SerializedName("sender_username")
    public String senderUsername;

    @SerializedName("content")
    public String content;

    @SerializedName("attachment_url")
    public String attachmentUrl;
}
