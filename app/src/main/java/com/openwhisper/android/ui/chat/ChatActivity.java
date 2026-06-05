package com.openwhisper.android.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.openwhisper.android.OpenWhisperApp;
import com.openwhisper.android.R;
import com.openwhisper.android.ui.base.BaseActivity;
import com.openwhisper.android.data.NetworkModule;
import com.openwhisper.android.data.UserSession;
import com.openwhisper.android.databinding.ActivityChatBinding;
import com.openwhisper.android.model.ApiMessage;
import com.openwhisper.android.model.SendMessageBody;
import com.openwhisper.android.model.WsChatEvent;
import com.openwhisper.android.ui.main.MainActivity;
import com.openwhisper.android.util.ApiErrors;
import com.openwhisper.android.util.MessageTimestamps;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import retrofit2.Call;
import retrofit2.Callback;

public class ChatActivity extends BaseActivity {

    private ActivityChatBinding binding;
    private NetworkModule network;
    private ChatAdapter adapter;
    private int chatId = -1;
    private String chatTitle = "";
    private WebSocket webSocket;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupEdgeToEdge();
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applyImeOnlyPadding(binding.inputBar);
        applyBottomNavInsets(binding.bottomNav);

        network = ((OpenWhisperApp) getApplication()).network();
        chatId = getIntent().getIntExtra(MainActivity.EXTRA_CHAT_ID, -1);
        chatTitle = getIntent().getStringExtra(MainActivity.EXTRA_CHAT_TITLE);
        if (chatTitle == null) {
            chatTitle = getString(R.string.chats);
        }
        if (chatId < 0) {
            Toast.makeText(this, R.string.error_generic, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setSupportActionBar(binding.toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle(chatTitle);
            ab.setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        binding.bottomNav.setSelectedItemId(R.id.nav_chats);
        binding.bottomNav.setOnItemSelectedListener(this::onBottomNavSelected);

        adapter = new ChatAdapter();
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        binding.recycler.setLayoutManager(llm);
        binding.recycler.setAdapter(adapter);

        binding.sendButton.setOnClickListener(v -> sendMessage());

        loadHistory();
    }

    private boolean onBottomNavSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_chats) {
            finish();
            return true;
        }
        if (item.getItemId() == R.id.nav_contacts) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(MainActivity.EXTRA_TAB, MainActivity.TAB_CONTACTS);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
            return true;
        }
        return false;
    }

    private void loadHistory() {
        binding.progress.setVisibility(View.VISIBLE);
        network.api()
                .listMessages(chatId)
                .enqueue(
                        new Callback<List<ApiMessage>>() {
                            @Override
                            public void onResponse(
                                    @NonNull Call<List<ApiMessage>> call,
                                    @NonNull retrofit2.Response<List<ApiMessage>> response) {
                                binding.progress.setVisibility(View.GONE);
                                if (!response.isSuccessful() || response.body() == null) {
                                    Toast.makeText(ChatActivity.this, ApiErrors.humanMessage(response), Toast.LENGTH_LONG)
                                            .show();
                                    return;
                                }
                                List<ApiMessage> raw = response.body();
                                Collections.reverse(raw);
                                List<ChatListItem> mapped = new ArrayList<>();
                                for (ApiMessage m : raw) {
                                    mapped.add(mapMessage(m));
                                }
                                adapter.setHistory(mapped);
                                binding.recycler.scrollToPosition(Math.max(0, adapter.getItemCount() - 1));
                                openWebSocket();
                            }

                            @Override
                            public void onFailure(@NonNull Call<List<ApiMessage>> call, @NonNull Throwable t) {
                                binding.progress.setVisibility(View.GONE);
                                Toast.makeText(ChatActivity.this, R.string.error_network, Toast.LENGTH_LONG).show();
                            }
                        });
    }

    private ChatListItem mapMessage(ApiMessage m) {
        String me = UserSession.getUsername();
        boolean mine = me != null && me.equalsIgnoreCase(m.senderUsername());
        String body = m.content != null ? m.content.trim() : "";
        if (body.isEmpty() && !TextUtils.isEmpty(m.attachmentUrl)) {
            body = "[Attachment]";
        }
        return new ChatListItem(
                m.messageId(),
                mine,
                m.senderUsername(),
                body,
                MessageTimestamps.format(m.createdAt),
                m.createdAt);
    }

    private void mapWsToUi(WsChatEvent ev) {
        if (!"message.created".equals(ev.type)) {
            return;
        }
        if (ev.chatId != chatId) {
            return;
        }
        if (adapter.containsMessageId(ev.messageId)) {
            return;
        }
        String me = UserSession.getUsername();
        boolean mine = me != null && me.equalsIgnoreCase(ev.senderUsername);
        String body = ev.content != null ? ev.content.trim() : "";
        if (body.isEmpty() && !TextUtils.isEmpty(ev.attachmentUrl)) {
            body = "[Attachment]";
        }
        String createdAtIso = ev.createdAt != null && !ev.createdAt.isBlank()
                ? ev.createdAt
                : Instant.now().toString();
        String timestamp = MessageTimestamps.format(createdAtIso);
        if (timestamp.isEmpty()) {
            timestamp = MessageTimestamps.formatNow();
        }
        ChatListItem item =
                new ChatListItem(ev.messageId, mine, ev.senderUsername, body, timestamp, createdAtIso);
        mainHandler.post(
                () -> {
                    adapter.append(item);
                    binding.recycler.scrollToPosition(Math.max(0, adapter.getItemCount() - 1));
                });
    }

    private void openWebSocket() {
        if (webSocket != null) {
            return;
        }
        OkHttpClient client = network.okHttpClient();
        Request request = new Request.Builder().url(network.webSocketUrl(chatId)).build();
        webSocket =
                client.newWebSocket(
                        request,
                        new WebSocketListener() {
                            @Override
                            public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
                                try {
                                    WsChatEvent ev = network.gson().fromJson(text, WsChatEvent.class);
                                    if (ev != null) {
                                        mapWsToUi(ev);
                                    }
                                } catch (Exception ignored) {
                                    // ignore non-chat frames (e.g. pong)
                                }
                            }

                            @Override
                            public void onMessage(@NonNull WebSocket webSocket, @NonNull ByteString bytes) {}

                            @Override
                            public void onFailure(
                                    @NonNull WebSocket webSocket, @NonNull Throwable t, Response response) {
                                // Realtime is optional; REST still works.
                            }
                        });
    }

    private void sendMessage() {
        if (binding.messageInput.getText() == null) {
            return;
        }
        String text = binding.messageInput.getText().toString().trim();
        if (text.isEmpty()) {
            return;
        }
        binding.sendButton.setEnabled(false);
        network.api()
                .postMessage(chatId, new SendMessageBody(text))
                .enqueue(
                        new Callback<ApiMessage>() {
                            @Override
                            public void onResponse(
                                    @NonNull Call<ApiMessage> call,
                                    @NonNull retrofit2.Response<ApiMessage> response) {
                                binding.sendButton.setEnabled(true);
                                if (!response.isSuccessful() || response.body() == null) {
                                    Toast.makeText(ChatActivity.this, ApiErrors.humanMessage(response), Toast.LENGTH_LONG)
                                            .show();
                                    return;
                                }
                                binding.messageInput.setText("");
                                ApiMessage m = response.body();
                                if (!adapter.containsMessageId(m.messageId())) {
                                    adapter.append(mapMessage(m));
                                    binding.recycler.scrollToPosition(Math.max(0, adapter.getItemCount() - 1));
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<ApiMessage> call, @NonNull Throwable t) {
                                binding.sendButton.setEnabled(true);
                                Toast.makeText(ChatActivity.this, R.string.error_network, Toast.LENGTH_LONG).show();
                            }
                        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webSocket != null) {
            webSocket.close(1000, null);
            webSocket = null;
        }
    }
}
