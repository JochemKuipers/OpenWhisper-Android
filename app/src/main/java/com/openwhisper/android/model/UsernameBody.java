package com.openwhisper.android.model;

import com.google.gson.annotations.SerializedName;

public final class UsernameBody {
    @SerializedName("username")
    public final String username;

    public UsernameBody(String username) {
        this.username = username;
    }
}
