package com.openwhisper.android.ui.chat;

import java.util.Objects;

public final class ChatListItem {

    public final long messageId;
    public final boolean outgoing;
    public final String senderUsername;
    public final String text;

    public ChatListItem(long messageId, boolean outgoing, String senderUsername, String text) {
        this.messageId = messageId;
        this.outgoing = outgoing;
        this.senderUsername = senderUsername != null ? senderUsername : "";
        this.text = text != null ? text : "";
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
        return messageId == that.messageId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageId);
    }
}
