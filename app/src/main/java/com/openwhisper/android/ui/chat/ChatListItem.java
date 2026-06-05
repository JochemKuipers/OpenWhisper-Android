package com.openwhisper.android.ui.chat;

import java.util.Objects;

public final class ChatListItem {

    public final long messageId;
    public final boolean dateHeader;
    public final boolean outgoing;
    public final String senderUsername;
    public final String text;
    public final String timestamp;
    public final String createdAtIso;
    public final String dateLabel;

    public ChatListItem(
            long messageId,
            boolean outgoing,
            String senderUsername,
            String text,
            String timestamp,
            String createdAtIso) {
        this.messageId = messageId;
        this.dateHeader = false;
        this.outgoing = outgoing;
        this.senderUsername = senderUsername != null ? senderUsername : "";
        this.text = text != null ? text : "";
        this.timestamp = timestamp != null ? timestamp : "";
        this.createdAtIso = createdAtIso != null ? createdAtIso : "";
        this.dateLabel = "";
    }

    private ChatListItem(String dateLabel) {
        this.messageId = -1L;
        this.dateHeader = true;
        this.outgoing = false;
        this.senderUsername = "";
        this.text = "";
        this.timestamp = "";
        this.createdAtIso = "";
        this.dateLabel = dateLabel != null ? dateLabel : "";
    }

    public static ChatListItem dateHeader(String label) {
        return new ChatListItem(label);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChatListItem)) {
            return false;
        }
        ChatListItem that = (ChatListItem) o;
        return messageId == that.messageId && dateHeader == that.dateHeader;
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageId, dateHeader);
    }
}
