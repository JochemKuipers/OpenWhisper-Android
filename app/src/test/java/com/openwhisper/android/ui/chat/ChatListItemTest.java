package com.openwhisper.android.ui.chat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ChatListItemTest {

    @Test
    public void detectsImageAttachment() {
        ChatListItem item =
                new ChatListItem(
                        1L,
                        true,
                        "",
                        "12:00",
                        "2026-01-01T12:00:00Z",
                        "https://host/media/photo.jpg");
        assertTrue(item.hasAttachment());
        assertEquals(ChatListItem.AttachmentKind.IMAGE, item.attachmentKind);
        assertEquals("photo.jpg", item.attachmentLabel);
    }

    @Test
    public void detectsFileAttachment() {
        ChatListItem item =
                new ChatListItem(
                        2L,
                        false,
                        "see attached",
                        "12:01",
                        "2026-01-01T12:01:00Z",
                        "https://host/media/doc.pdf");
        assertEquals(ChatListItem.AttachmentKind.FILE, item.attachmentKind);
    }

    @Test
    public void textOnlyMessageHasNoAttachment() {
        ChatListItem item = new ChatListItem(3L, true, "Hi", "12:02", "2026-01-01T12:02:00Z", "");
        assertFalse(item.hasAttachment());
    }
}
