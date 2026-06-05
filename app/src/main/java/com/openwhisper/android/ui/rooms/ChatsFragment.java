package com.openwhisper.android.ui.rooms;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.openwhisper.android.R;
import com.openwhisper.android.databinding.FragmentChatsBinding;
import com.openwhisper.android.model.ChatSummary;
import com.openwhisper.android.ui.chat.ChatActivity;
import com.openwhisper.android.ui.main.MainActivity;
import com.openwhisper.android.ui.main.MainHost;
import com.openwhisper.android.util.ApiErrors;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatsFragment extends Fragment implements RoomsAdapter.Listener {

    private FragmentChatsBinding binding;
    private RoomsAdapter adapter;
    private MainHost host;

    @Override
    public void onAttach(@NonNull android.content.Context context) {
        super.onAttach(context);
        if (!(context instanceof MainHost)) {
            throw new IllegalStateException("Host must implement MainHost");
        }
        host = (MainHost) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChatsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new RoomsAdapter(this);
        binding.recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recycler.setAdapter(adapter);

        binding.searchInput.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        adapter.filter(s != null ? s.toString() : "");
                        updateEmptyState();
                    }

                    @Override
                    public void afterTextChanged(Editable s) {}
                });

        binding.menuButton.setOnClickListener(this::showMenu);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadChats();
    }

    private void showMenu(View anchor) {
        PopupMenu menu = new PopupMenu(requireContext(), anchor);
        menu.getMenuInflater().inflate(R.menu.menu_rooms, menu.getMenu());
        menu.setOnMenuItemClickListener(
                item -> {
                    if (item.getItemId() == R.id.action_logout) {
                        host.logout();
                        return true;
                    }
                    return false;
                });
        menu.show();
    }

    private void loadChats() {
        if (binding == null) {
            return;
        }
        binding.progress.setVisibility(View.VISIBLE);
        host.network()
                .api()
                .listChats()
                .enqueue(
                        new Callback<List<ChatSummary>>() {
                            @Override
                            public void onResponse(
                                    @NonNull Call<List<ChatSummary>> call, @NonNull Response<List<ChatSummary>> response) {
                                if (binding == null) {
                                    return;
                                }
                                binding.progress.setVisibility(View.GONE);
                                if (!response.isSuccessful() || response.body() == null) {
                                    Toast.makeText(requireContext(), ApiErrors.humanMessage(response), Toast.LENGTH_LONG)
                                            .show();
                                    return;
                                }
                                adapter.setItems(response.body());
                                updateEmptyState();
                            }

                            @Override
                            public void onFailure(@NonNull Call<List<ChatSummary>> call, @NonNull Throwable t) {
                                if (binding == null) {
                                    return;
                                }
                                binding.progress.setVisibility(View.GONE);
                                Toast.makeText(requireContext(), R.string.error_network, Toast.LENGTH_LONG).show();
                            }
                        });
    }

    private void updateEmptyState() {
        boolean empty = adapter.getItemCount() == 0;
        binding.emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.recycler.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (empty) {
            binding.emptyView.setText(
                    adapter.isFiltering() ? R.string.no_chats_found : R.string.no_chats);
        }
    }

    @Override
    public void onChatClicked(ChatSummary chat) {
        int id = chat.chatId();
        if (id < 0) {
            Toast.makeText(requireContext(), R.string.error_generic, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent i = new Intent(requireContext(), ChatActivity.class);
        i.putExtra(MainActivity.EXTRA_CHAT_ID, id);
        i.putExtra(MainActivity.EXTRA_CHAT_TITLE, chat.displayTitle());
        startActivity(i);
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
