package com.openwhisper.android.ui.chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.openwhisper.android.OpenWhisperApp;
import com.openwhisper.android.R;
import com.openwhisper.android.data.NetworkModule;
import com.openwhisper.android.data.UserSession;
import com.openwhisper.android.databinding.BottomSheetChatInfoBinding;
import com.openwhisper.android.model.ChatSummary;
import com.openwhisper.android.model.PublicUser;
import com.openwhisper.android.model.UpdateChatBody;
import com.openwhisper.android.model.UsernameBody;
import com.openwhisper.android.util.ApiErrors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatInfoBottomSheet extends BottomSheetDialogFragment implements ChatMemberAdapter.Listener {

    private static final String ARG_CHAT_ID = "chat_id";

    public interface Host {
        void onChatInfoUpdated(@NonNull ChatSummary chat);
    }

    public static ChatInfoBottomSheet newInstance(int chatId) {
        ChatInfoBottomSheet sheet = new ChatInfoBottomSheet();
        Bundle args = new Bundle();
        args.putInt(ARG_CHAT_ID, chatId);
        sheet.setArguments(args);
        return sheet;
    }

    private BottomSheetChatInfoBinding binding;
    private NetworkModule network;
    private ChatMemberAdapter memberAdapter;
    private int chatId = -1;
    private ChatSummary chat;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetChatInfoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        network = ((OpenWhisperApp) requireActivity().getApplication()).network();
        if (getArguments() != null) {
            chatId = getArguments().getInt(ARG_CHAT_ID, -1);
        }
        memberAdapter = new ChatMemberAdapter(this);
        binding.membersRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.membersRecycler.setAdapter(memberAdapter);

        binding.saveTitleButton.setOnClickListener(v -> saveTitle());
        binding.inviteButton.setOnClickListener(v -> showInviteDialog());
        loadChat();
    }

    public void reload() {
        if (binding != null) {
            loadChat();
        }
    }

    private void loadChat() {
        binding.progress.setVisibility(View.VISIBLE);
        network.api()
                .getChat(chatId)
                .enqueue(
                        new Callback<>() {
                            @Override
                            public void onResponse(
                                    @NonNull Call<ChatSummary> call, @NonNull Response<ChatSummary> response) {
                                if (binding == null) {
                                    return;
                                }
                                binding.progress.setVisibility(View.GONE);
                                if (!response.isSuccessful() || response.body() == null) {
                                    toast(ApiErrors.humanMessage(response));
                                    return;
                                }
                                bindChat(response.body());
                            }

                            @Override
                            public void onFailure(@NonNull Call<ChatSummary> call, @NonNull Throwable t) {
                                if (binding == null) {
                                    return;
                                }
                                binding.progress.setVisibility(View.GONE);
                                toast(getString(R.string.error_network));
                            }
                        });
    }

    private void bindChat(ChatSummary summary) {
        chat = summary;
        binding.titleInput.setText(summary.title != null ? summary.title : "");
        boolean admin = summary.isAdmin;
        binding.titleInput.setEnabled(admin);
        binding.titleLayout.setEnabled(admin);
        binding.saveTitleButton.setVisibility(admin ? View.VISIBLE : View.GONE);
        binding.inviteButton.setVisibility(admin ? View.VISIBLE : View.GONE);
        String me = UserSession.getUsername();
        memberAdapter.setMembers(summary.users, summary.adminUsername, me != null ? me : "", admin);
        notifyHost(summary);
    }

    private void saveTitle() {
        if (chat == null || !chat.isAdmin) {
            return;
        }
        String title =
                binding.titleInput.getText() != null ? binding.titleInput.getText().toString().trim() : "";
        binding.progress.setVisibility(View.VISIBLE);
        network.api()
                .updateChat(chatId, new UpdateChatBody(title))
                .enqueue(
                        new Callback<>() {
                            @Override
                            public void onResponse(
                                    @NonNull Call<ChatSummary> call, @NonNull Response<ChatSummary> response) {
                                if (binding == null) {
                                    return;
                                }
                                binding.progress.setVisibility(View.GONE);
                                if (!response.isSuccessful() || response.body() == null) {
                                    toast(ApiErrors.humanMessage(response));
                                    return;
                                }
                                bindChat(response.body());
                                toast(getString(R.string.chat_title_saved));
                            }

                            @Override
                            public void onFailure(@NonNull Call<ChatSummary> call, @NonNull Throwable t) {
                                if (binding == null) {
                                    return;
                                }
                                binding.progress.setVisibility(View.GONE);
                                toast(getString(R.string.error_network));
                            }
                        });
    }

    private void showInviteDialog() {
        binding.progress.setVisibility(View.VISIBLE);
        network.api()
                .listFriends()
                .enqueue(
                        new Callback<>() {
                            @Override
                            public void onResponse(
                                    @NonNull Call<List<PublicUser>> call,
                                    @NonNull Response<List<PublicUser>> response) {
                                if (binding == null) {
                                    return;
                                }
                                binding.progress.setVisibility(View.GONE);
                                if (!response.isSuccessful() || response.body() == null) {
                                    toast(ApiErrors.humanMessage(response));
                                    return;
                                }
                                List<String> candidates = inviteCandidates(response.body());
                                if (candidates.isEmpty()) {
                                    toast(getString(R.string.no_friends_to_invite));
                                    return;
                                }
                                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                                        .setTitle(R.string.invite_friend)
                                        .setItems(
                                                candidates.toArray(new String[0]),
                                                (d, which) -> inviteUser(candidates.get(which)))
                                        .show();
                            }

                            @Override
                            public void onFailure(@NonNull Call<List<PublicUser>> call, @NonNull Throwable t) {
                                if (binding == null) {
                                    return;
                                }
                                binding.progress.setVisibility(View.GONE);
                                toast(getString(R.string.error_network));
                            }
                        });
    }

    private List<String> inviteCandidates(List<PublicUser> friends) {
        Set<String> inChat = new HashSet<>();
        if (chat != null && chat.users != null) {
            for (ChatSummary.UserRef user : chat.users) {
                if (user != null && user.username != null) {
                    inChat.add(user.username.toLowerCase(Locale.ROOT));
                }
            }
        }
        List<String> result = new ArrayList<>();
        for (PublicUser friend : friends) {
            if (friend == null || friend.username == null || friend.username.isBlank()) {
                continue;
            }
            if (!inChat.contains(friend.username.toLowerCase(Locale.ROOT))) {
                result.add(friend.username);
            }
        }
        return result;
    }

    private void inviteUser(String username) {
        binding.progress.setVisibility(View.VISIBLE);
        network.api()
                .inviteToChat(chatId, new UsernameBody(username))
                .enqueue(
                        new Callback<>() {
                            @Override
                            public void onResponse(
                                    @NonNull Call<ChatSummary> call, @NonNull Response<ChatSummary> response) {
                                if (binding == null) {
                                    return;
                                }
                                binding.progress.setVisibility(View.GONE);
                                if (!response.isSuccessful() || response.body() == null) {
                                    toast(ApiErrors.humanMessage(response));
                                    return;
                                }
                                bindChat(response.body());
                            }

                            @Override
                            public void onFailure(@NonNull Call<ChatSummary> call, @NonNull Throwable t) {
                                if (binding == null) {
                                    return;
                                }
                                binding.progress.setVisibility(View.GONE);
                                toast(getString(R.string.error_network));
                            }
                        });
    }

    @Override
    public void onRemoveMember(String username) {
        binding.progress.setVisibility(View.VISIBLE);
        network.api()
                .removeMember(chatId, username)
                .enqueue(
                        new Callback<>() {
                            @Override
                            public void onResponse(
                                    @NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                                if (binding == null) {
                                    return;
                                }
                                if (response.isSuccessful()) {
                                    loadChat();
                                } else {
                                    binding.progress.setVisibility(View.GONE);
                                    toast(ApiErrors.humanMessage(response));
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                                if (binding == null) {
                                    return;
                                }
                                binding.progress.setVisibility(View.GONE);
                                toast(getString(R.string.error_network));
                            }
                        });
    }

    private void notifyHost(ChatSummary summary) {
        if (requireActivity() instanceof Host host) {
            host.onChatInfoUpdated(summary);
        }
    }

    private void toast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
