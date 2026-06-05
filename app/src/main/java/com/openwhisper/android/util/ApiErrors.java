package com.openwhisper.android.util;

import androidx.annotation.NonNull;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Response;

public final class ApiErrors {

    private ApiErrors() {}

    @NonNull
    public static String humanMessage(Response<?> response) {
        ResponseBody eb = response.errorBody();
        if (eb != null) {
            try {
                String raw = eb.string();
                String parsed = parseDetailJson(raw);
                if (parsed != null && !parsed.isEmpty()) {
                    return parsed;
                }
                if (!raw.isEmpty()) {
                    return raw;
                }
            } catch (IOException ignored) {
                // fall through
            }
        }
        return response.message();
    }

    private static String parseDetailJson(String raw) {
        try {
            JsonElement el = JsonParser.parseString(raw);
            if (!el.isJsonObject()) {
                return null;
            }
            JsonObject o = el.getAsJsonObject();
            if (!o.has("detail")) {
                return null;
            }
            JsonElement d = o.get("detail");
            if (d.isJsonPrimitive()) {
                return d.getAsString();
            }
            if (d.isJsonArray() && !d.getAsJsonArray().isEmpty()) {
                JsonElement first = d.getAsJsonArray().get(0);
                if (first.isJsonObject() && first.getAsJsonObject().has("string")) {
                    return first.getAsJsonObject().get("string").getAsString();
                }
                return first.toString();
            }
            return d.toString();
        } catch (Exception e) {
            return null;
        }
    }
}
