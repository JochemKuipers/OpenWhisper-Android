package com.openwhisper.android.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.text.style.StyleSpan;

import com.google.gson.Gson;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@RunWith(RobolectricTestRunner.class)
public class ChatSummaryTest {

    private final Gson gson = new Gson();

    @Test
    public void deserializesDisplayFieldsFromFixture() throws Exception {
        ChatSummary chat = loadFixture("fixtures/chat_dm.json", ChatSummary.class);
        assertEquals("bob", chat.displayTitle);
        assertEquals("", chat.memberSubtitle);
        assertEquals("bob", chat.getDisplayTitle());
    }

    @Test
    public void groupFixture_hasMemberSubtitle() throws Exception {
        ChatSummary chat = loadFixture("fixtures/chat_group.json", ChatSummary.class);
        assertEquals("New group chat", chat.getDisplayTitle());
        assertEquals("You, bob, carol", chat.getMemberSubtitle());
    }

    @Test
    public void getDisplayTitle_fallsBackToStoredTitle() {
        ChatSummary chat = new ChatSummary();
        chat.title = "Custom name";
        assertEquals("Custom name", chat.getDisplayTitle());
    }

    @Test
    public void getMemberSubtitleStyled_boldsYouLabel() {
        ChatSummary chat = new ChatSummary();
        chat.memberSubtitle = "You, bob";
        CharSequence styled = chat.getMemberSubtitleStyled("You");
        StyleSpan[] spans =
                ((android.text.Spannable) styled).getSpans(0, styled.length(), StyleSpan.class);
        assertTrue(spans.length > 0);
    }

    @Test
    public void chatId_parsedFromUrl() {
        ChatSummary chat = new ChatSummary();
        chat.url = "http://example.com/api/chats/42/";
        assertEquals(42, chat.chatId());
    }

    private <T> T loadFixture(String path, Class<T> type) throws Exception {
        try (InputStreamReader reader =
                new InputStreamReader(
                        Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(path)),
                        StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, type);
        }
    }
}
