package com.openwhisper.android.model;

import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

    public boolean isDirectChat() {
        return users != null && users.size() == 2;
    }

    @Nullable
    public String otherUsername(@Nullable String currentUsername) {
        if (!isDirectChat() || currentUsername == null) {
            return null;
        }
        for (UserRef user : users) {
            if (user != null
                    && user.username != null
                    && !user.username.equalsIgnoreCase(currentUsername)) {
                return user.username;
            }
        }
        return null;
    }

    public String displayTitle(@Nullable String currentUsername) {
        if (title != null && !title.isBlank()) {
            return title.trim();
        }
        String other = otherUsername(currentUsername);
        if (other != null) {
            return other;
        }
        return "Chat";
    }

    /** Plain-text member list for search; empty for 1:1 chats. */
    public String memberSubtitlePlain(@Nullable String currentUsername, @NonNull String youLabel) {
        if (isDirectChat() || users == null || users.isEmpty()) {
            return "";
        }
        return joinMemberNames(currentUsername, youLabel, false).toString();
    }

    /** Styled member list for the chat list; empty for 1:1 chats. */
    public CharSequence memberSubtitleDisplay(@Nullable String currentUsername, @NonNull String youLabel) {
        if (isDirectChat() || users == null || users.isEmpty()) {
            return "";
        }
        return joinMemberNames(currentUsername, youLabel, true);
    }

    private CharSequence joinMemberNames(
            @Nullable String currentUsername, @NonNull String youLabel, boolean boldYou) {
        SpannableStringBuilder sb = new SpannableStringBuilder();
        boolean first = true;
        for (UserRef user : users) {
            if (user == null || user.username == null) {
                continue;
            }
            if (!first) {
                sb.append(", ");
            }
            first = false;
            if (currentUsername != null && user.username.equalsIgnoreCase(currentUsername)) {
                int start = sb.length();
                sb.append(youLabel);
                if (boldYou) {
                    sb.setSpan(
                            new StyleSpan(Typeface.BOLD),
                            start,
                            sb.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            } else {
                sb.append(user.username);
            }
        }
        return sb;
    }

    public static final class UserRef {
        @SerializedName("username")
        public String username;
    }
}
