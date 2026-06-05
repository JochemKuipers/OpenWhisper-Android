package com.openwhisper.android.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class ApiErrorsTest {

    @Test
    public void humanMessage_parsesDetailString() {
        ResponseBody body =
                ResponseBody.create(
                        "{\"detail\":\"Invalid credentials.\"}",
                        MediaType.parse("application/json"));
        Response<Void> response = Response.error(401, body);
        assertEquals("Invalid credentials.", ApiErrors.humanMessage(response));
    }

    @Test
    public void humanMessage_fallsBackToMessageWhenNoBody() {
        Response<Void> response = Response.error(500, ResponseBody.create("", MediaType.parse("text/plain")));
        assertEquals("Response.error()", ApiErrors.humanMessage(response));
    }
}
