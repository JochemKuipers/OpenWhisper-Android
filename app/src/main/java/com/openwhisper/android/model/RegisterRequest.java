package com.openwhisper.android.model;

public final class RegisterRequest {
    public final String username;
    public final String email;
    public final String password;
    public final String password_confirm;

    public RegisterRequest(String username, String email, String password, String passwordConfirm) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.password_confirm = passwordConfirm;
    }
}
