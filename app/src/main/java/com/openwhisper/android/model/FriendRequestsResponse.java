package com.openwhisper.android.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

@SuppressWarnings("unused")
public final class FriendRequestsResponse {
    @SerializedName("incoming")
    public List<PublicUser> incoming;

    @SerializedName("outgoing")
    public List<PublicUser> outgoing;
}
