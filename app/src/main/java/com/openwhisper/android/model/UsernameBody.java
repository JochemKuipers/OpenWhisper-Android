package com.openwhisper.android.model;

import com.google.gson.annotations.SerializedName;

public record UsernameBody(@SerializedName("username") String username) {
}
