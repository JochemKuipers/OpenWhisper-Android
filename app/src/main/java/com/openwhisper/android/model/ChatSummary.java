package com.openwhisper.android.model;

import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public final class ChatSummary {
    private static final Pattern CHAT_ID = Pattern.compile("/chats/(\\d+)/?$");

    @SerializedName("url")
    public String url;

    @SerializedName("title")
    public String title;

    @SerializedName("display_title")
    public String displayTitle;

    @SerializedName("member_subtitle")
    public String memberSubtitle;

    @SerializedName("users")
    public List<UserRef> users;

    @SerializedName("admin_username")
    public String adminUsername;

    @SerializedName("is_admin")
    public boolean isAdmin;

    public int chatId() {
        if (url == null) {
            return -1;
        }
        Matcher m = CHAT_ID.matcher(url);
        if (m.find()) {
            return Integer.parseInt(Objects.requireNonNull(m.group(1)));
        }
        return -1;
    }

    /** Resolved list/header title from the API (per authenticated user). */
    public String getDisplayTitle() {
        if (displayTitle != null && !displayTitle.isBlank()) {
            return displayTitle.trim();
        }
        if (title != null && !title.isBlank()) {
            return title.trim();
        }
        return "Chat";
    }

    /** Plain member subline for groups; empty for 1:1 chats. */
    public String getMemberSubtitle() {
        return memberSubtitle != null ? memberSubtitle : "";
    }

    /** Same as {@link #getMemberSubtitle()} with the requester's "You" label in bold. */
    public CharSequence getMemberSubtitleStyled(@NonNull String youLabel) {
        String plain = getMemberSubtitle();
        if (plain.isEmpty()) {
            return "";
        }
        SpannableStringBuilder styled = new SpannableStringBuilder(plain);
        int index = 0;
        while ((index = plain.indexOf(youLabel, index)) >= 0) {
            styled.setSpan(
                    new StyleSpan(Typeface.BOLD),
                    index,
                    index + youLabel.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            index += youLabel.length();
        }
        return styled;
    }

    public static final class UserRef {
        @SerializedName("username")
        public String username;
    }
}
