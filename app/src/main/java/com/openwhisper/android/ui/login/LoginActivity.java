package com.openwhisper.android.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import com.openwhisper.android.ui.base.BaseActivity;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.openwhisper.android.OpenWhisperApp;
import com.openwhisper.android.R;
import com.openwhisper.android.data.NetworkModule;
import com.openwhisper.android.data.UserSession;
import com.openwhisper.android.databinding.ActivityLoginBinding;
import com.openwhisper.android.model.LoginRequest;
import com.openwhisper.android.model.TokenResponse;
import com.openwhisper.android.model.UserProfile;
import com.openwhisper.android.ui.register.RegisterActivity;
import com.openwhisper.android.ui.main.MainActivity;
import com.openwhisper.android.util.ApiErrors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends BaseActivity {

    private ActivityLoginBinding binding;
    private NetworkModule network;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupEdgeToEdge();
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        int contentPadding = (int) (24 * getResources().getDisplayMetrics().density);
        applyRootSystemBarPadding(binding.getRoot(), contentPadding);

        network = ((OpenWhisperApp) getApplication()).network();
        if (network.tokenStore().hasAccess()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        binding.loginButton.setOnClickListener(v -> attemptLogin());
        binding.goRegisterButton.setOnClickListener(
                v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void attemptLogin() {
        String username = text(binding.username);
        String password = text(binding.password);
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show();
            return;
        }

        setBusy(true);
        network.api()
                .login(new LoginRequest(username, password))
                .enqueue(
                        new Callback<TokenResponse>() {
                            @Override
                            public void onResponse(
                                    @NonNull Call<TokenResponse> call, @NonNull Response<TokenResponse> response) {
                                if (!response.isSuccessful() || response.body() == null) {
                                    setBusy(false);
                                    Toast.makeText(
                                                    LoginActivity.this,
                                                    ApiErrors.humanMessage(response),
                                                    Toast.LENGTH_LONG)
                                            .show();
                                    return;
                                }
                                TokenResponse body = response.body();
                                if (body.access == null
                                        || body.access.isEmpty()
                                        || body.refresh == null
                                        || body.refresh.isEmpty()) {
                                    setBusy(false);
                                    Toast.makeText(LoginActivity.this, R.string.error_generic, Toast.LENGTH_LONG)
                                            .show();
                                    return;
                                }
                                network.tokenStore().saveTokens(body.access, body.refresh);
                                try {
                                    DecodedJWT jwt = JWT.decode(body.access);
                                    if (jwt.getExpiresAt() != null) {
                                        jwt.getExpiresAt().getTime();
                                    }
                                } catch (Exception ignored) {
                                    // optional client-side decode
                                }
                                loadMeThenProceed();
                            }

                            @Override
                            public void onFailure(@NonNull Call<TokenResponse> call, @NonNull Throwable t) {
                                setBusy(false);
                                Toast.makeText(LoginActivity.this, R.string.error_network, Toast.LENGTH_LONG).show();
                            }
                        });
    }

    private void loadMeThenProceed() {
        network.api()
                .me()
                .enqueue(
                        new Callback<UserProfile>() {
                            @Override
                            public void onResponse(@NonNull Call<UserProfile> call, @NonNull Response<UserProfile> response) {
                                setBusy(false);
                                if (!response.isSuccessful() || response.body() == null) {
                                    Toast.makeText(
                                                    LoginActivity.this,
                                                    ApiErrors.humanMessage(response),
                                                    Toast.LENGTH_LONG)
                                            .show();
                                    return;
                                }
                                UserProfile p = response.body();
                                UserSession.setUsername(p.username);
                                network.tokenStore().saveUsername(p.username);
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            }

                            @Override
                            public void onFailure(@NonNull Call<UserProfile> call, @NonNull Throwable t) {
                                setBusy(false);
                                Toast.makeText(LoginActivity.this, R.string.error_network, Toast.LENGTH_LONG).show();
                            }
                        });
    }

    private void setBusy(boolean busy) {
        binding.progress.setVisibility(busy ? View.VISIBLE : View.GONE);
        binding.loginButton.setEnabled(!busy);
        binding.goRegisterButton.setEnabled(!busy);
    }

    private static String text(com.google.android.material.textfield.TextInputEditText et) {
        if (et.getText() == null) {
            return "";
        }
        return et.getText().toString().trim();
    }
}
