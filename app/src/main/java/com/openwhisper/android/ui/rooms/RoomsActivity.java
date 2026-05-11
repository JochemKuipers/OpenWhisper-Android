package com.openwhisper.android.ui.rooms;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.openwhisper.android.OpenWhisperApp;
import com.openwhisper.android.R;
import com.openwhisper.android.data.NetworkModule;
import com.openwhisper.android.data.UserSession;
import com.openwhisper.android.databinding.ActivityRoomsBinding;
import com.openwhisper.android.model.ChatSummary;
import com.openwhisper.android.ui.chat.ChatActivity;
import com.openwhisper.android.ui.login.LoginActivity;
import com.openwhisper.android.util.ApiErrors;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RoomsActivity extends AppCompatActivity implements RoomsAdapter.Listener {

    public static final String EXTRA_CHAT_ID = "chat_id";
    public static final String EXTRA_CHAT_TITLE = "chat_title";

    private ActivityRoomsBinding binding;
    private NetworkModule network;
    private RoomsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRoomsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        network = ((OpenWhisperApp) getApplication()).network();
        if (!network.tokenStore().hasAccess()) {
            navigateLogin();
            return;
        }

        setSupportActionBar(binding.toolbar);
        adapter = new RoomsAdapter(this);
        binding.recycler.setLayoutManager(new LinearLayoutManager(this));
        binding.recycler.setAdapter(adapter);

        loadChats();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!network.tokenStore().hasAccess()) {
            navigateLogin();
        }
    }

    private void loadChats() {
        binding.progress.setVisibility(View.VISIBLE);
        network.api()
                .listChats()
                .enqueue(
                        new Callback<List<ChatSummary>>() {
                            @Override
                            public void onResponse(
                                    @NonNull Call<List<ChatSummary>> call, @NonNull Response<List<ChatSummary>> response) {
                                binding.progress.setVisibility(View.GONE);
                                if (!response.isSuccessful() || response.body() == null) {
                                    Toast.makeText(RoomsActivity.this, ApiErrors.humanMessage(response), Toast.LENGTH_LONG)
                                            .show();
                                    return;
                                }
                                adapter.setItems(response.body());
                            }

                            @Override
                            public void onFailure(@NonNull Call<List<ChatSummary>> call, @NonNull Throwable t) {
                                binding.progress.setVisibility(View.GONE);
                                Toast.makeText(RoomsActivity.this, R.string.error_network, Toast.LENGTH_LONG).show();
                            }
                        });
    }

    @Override
    public void onChatClicked(ChatSummary chat) {
        int id = chat.chatId();
        if (id < 0) {
            Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent i = new Intent(this, ChatActivity.class);
        i.putExtra(EXTRA_CHAT_ID, id);
        i.putExtra(EXTRA_CHAT_TITLE, chat.displayTitle());
        startActivity(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_rooms, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        binding.progress.setVisibility(View.VISIBLE);
        network.api()
                .logout()
                .enqueue(
                        new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(
                                    @NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                                binding.progress.setVisibility(View.GONE);
                                network.tokenStore().clear();
                                UserSession.clear();
                                navigateLogin();
                            }

                            @Override
                            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                                binding.progress.setVisibility(View.GONE);
                                network.tokenStore().clear();
                                UserSession.clear();
                                navigateLogin();
                            }
                        });
    }

    private void navigateLogin() {
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
