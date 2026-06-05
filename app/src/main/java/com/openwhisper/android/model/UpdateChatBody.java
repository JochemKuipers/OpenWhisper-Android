package com.openwhisper.android.model;

import com.google.gson.annotations.SerializedName;

public final class UpdateChatBody {
    @SerializedName("title")
    public final String title;

    public UpdateChatBody(String title) {
        this.title = title;
    }
}
