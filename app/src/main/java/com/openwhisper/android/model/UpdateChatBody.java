package com.openwhisper.android.model;

import com.google.gson.annotations.SerializedName;

public record UpdateChatBody(@SerializedName("title") String title) {
}
