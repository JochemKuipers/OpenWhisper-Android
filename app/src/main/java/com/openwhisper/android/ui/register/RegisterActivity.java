package com.openwhisper.android.ui.register;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import com.openwhisper.android.ui.base.BaseActivity;
import com.openwhisper.android.OpenWhisperApp;
import com.openwhisper.android.R;
import com.openwhisper.android.data.NetworkModule;
import com.openwhisper.android.databinding.ActivityRegisterBinding;
import com.openwhisper.android.model.RegisterRequest;
import com.openwhisper.android.util.ApiErrors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends BaseActivity {

    private ActivityRegisterBinding binding;
    private NetworkModule network;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupEdgeToEdge();
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        int contentPadding = (int) (24 * getResources().getDisplayMetrics().density);
        applyRootSystemBarPadding(binding.getRoot(), contentPadding);

        network = ((OpenWhisperApp) getApplication()).network();

        binding.registerButton.setOnClickListener(v -> attemptRegister());
        binding.goLoginButton.setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        String username = text(binding.username);
        String email = text(binding.email);
        String password = text(binding.password);
        String confirm = text(binding.passwordConfirm);
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirm)) {
            Toast.makeText(this, R.string.password_mismatch, Toast.LENGTH_SHORT).show();
            return;
        }

        setBusy(true);
        network.api()
                .register(new RegisterRequest(username, email, password, confirm))
                .enqueue(
                        new Callback<>() {
                            @Override
                            public void onResponse(
                                    @NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                                setBusy(false);
                                if (response.isSuccessful()) {
                                    Toast.makeText(RegisterActivity.this, R.string.registration_ok, Toast.LENGTH_SHORT)
                                            .show();
                                    finish();
                                    return;
                                }
                                Toast.makeText(
                                                RegisterActivity.this,
                                                ApiErrors.humanMessage(response),
                                                Toast.LENGTH_LONG)
                                        .show();
                            }

                            @Override
                            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                                setBusy(false);
                                Toast.makeText(RegisterActivity.this, R.string.error_network, Toast.LENGTH_LONG).show();
                            }
                        });
    }

    private void setBusy(boolean busy) {
        binding.progress.setVisibility(busy ? View.VISIBLE : View.GONE);
        binding.registerButton.setEnabled(!busy);
        binding.goLoginButton.setEnabled(!busy);
    }

    private static String text(com.google.android.material.textfield.TextInputEditText et) {
        if (et.getText() == null) {
            return "";
        }
        return et.getText().toString().trim();
    }
}
