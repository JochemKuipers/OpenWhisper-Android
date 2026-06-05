package com.openwhisper.android.model;

import static org.junit.Assert.assertEquals;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.junit.Test;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

public class ApiMessageTest {

    @Test
    public void deserializesAndExtractsIds() throws Exception {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<ApiMessage>>() {}.getType();
        try (InputStreamReader reader =
                new InputStreamReader(
                        Objects.requireNonNull(
                                getClass().getClassLoader().getResourceAsStream("fixtures/messages.json")),
                        StandardCharsets.UTF_8)) {
            List<ApiMessage> messages = gson.fromJson(reader, listType);
            ApiMessage message = messages.get(0);
            assertEquals(10L, message.messageId());
            assertEquals("bob", message.senderUsername());
            assertEquals("Hello there", message.content);
        }
    }
}
