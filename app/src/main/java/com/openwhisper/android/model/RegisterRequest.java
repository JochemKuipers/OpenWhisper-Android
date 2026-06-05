package com.openwhisper.android.model;

public record RegisterRequest(String username, String email, String password,
                              String password_confirm) {
}
