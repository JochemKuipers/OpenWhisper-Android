package com.openwhisper.android.model;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public final class WsSocialEvent {
    @SerializedName("type")
    public String type;

    @SerializedName("username")
    public String username;

    @SerializedName("chat_id")
    public Integer chatId;
}
