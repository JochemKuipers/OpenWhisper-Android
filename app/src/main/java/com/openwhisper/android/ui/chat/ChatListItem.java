package com.openwhisper.android.ui.chat;

import com.openwhisper.android.util.AttachmentUtils;

import java.util.Objects;

public final class ChatListItem {

    public enum AttachmentKind {
        NONE,
        IMAGE,
        FILE
    }

    public final long messageId;
    public final boolean dateHeader;
    public final boolean outgoing;
    public final String text;
    public final String timestamp;
    public final String createdAtIso;
    public final String dateLabel;
    public final String attachmentUrl;
    public final AttachmentKind attachmentKind;
    public final String attachmentLabel;

    public ChatListItem(
            long messageId,
            boolean outgoing,
            String text,
            String timestamp,
            String createdAtIso,
            String attachmentUrl) {
        this.messageId = messageId;
        this.dateHeader = false;
        this.outgoing = outgoing;
        this.text = text != null ? text : "";
        this.timestamp = timestamp != null ? timestamp : "";
        this.createdAtIso = createdAtIso != null ? createdAtIso : "";
        this.dateLabel = "";
        this.attachmentUrl = attachmentUrl != null ? attachmentUrl : "";
        if (this.attachmentUrl.isEmpty()) {
            this.attachmentKind = AttachmentKind.NONE;
            this.attachmentLabel = "";
        } else if (AttachmentUtils.isImageUrl(this.attachmentUrl)) {
            this.attachmentKind = AttachmentKind.IMAGE;
            this.attachmentLabel = AttachmentUtils.fileNameFromUrl(this.attachmentUrl);
        } else {
            this.attachmentKind = AttachmentKind.FILE;
            this.attachmentLabel = AttachmentUtils.fileNameFromUrl(this.attachmentUrl);
        }
    }

    private ChatListItem(String dateLabel) {
        this.messageId = -1L;
        this.dateHeader = true;
        this.outgoing = false;
        this.text = "";
        this.timestamp = "";
        this.createdAtIso = "";
        this.dateLabel = dateLabel != null ? dateLabel : "";
        this.attachmentUrl = "";
        this.attachmentKind = AttachmentKind.NONE;
        this.attachmentLabel = "";
    }

    public static ChatListItem dateHeader(String label) {
        return new ChatListItem(label);
    }

    public boolean hasAttachment() {
        return attachmentKind != AttachmentKind.NONE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChatListItem that)) {
            return false;
        }
        return messageId == that.messageId && dateHeader == that.dateHeader;
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageId, dateHeader);
    }
}
