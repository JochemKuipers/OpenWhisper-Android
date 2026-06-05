package com.openwhisper.android.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.openwhisper.android.OpenWhisperApp;
import com.openwhisper.android.R;
import com.openwhisper.android.data.SocialWebSocketManager;
import com.openwhisper.android.databinding.FragmentFriendsBinding;
import com.openwhisper.android.model.ChatSummary;
import com.openwhisper.android.model.FriendRequestsResponse;
import com.openwhisper.android.model.PublicUser;
import com.openwhisper.android.model.UsernameBody;
import com.openwhisper.android.ui.chat.ChatActivity;
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

public class FriendsFragment extends Fragment implements FriendsAdapter.Listener {

    private static final long SEARCH_DEBOUNCE_MS = 350L;

    private FragmentFriendsBinding binding;
    private FriendsAdapter adapter;
    private MainHost host;

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingSearch;

    private List<PublicUser> friends = List.of();
    private List<PublicUser> incoming = List.of();
    private List<PublicUser> outgoing = List.of();
    private List<PublicUser> searchResults = List.of();
    private String searchQuery = "";
    private SocialWebSocketManager.Listener socialListener;

    @Override
    public void onStart() {
        super.onStart();
        socialListener =
                event -> {
                    if (event.type != null && (event.type.startsWith("friend_") || "chat_updated".equals(event.type))) {
                        refresh();
                    }
                };
        ((OpenWhisperApp) requireActivity().getApplication()).socialWebSocket().addListener(socialListener);
    }

    @Override
    public void onStop() {
        if (socialListener != null) {
            ((OpenWhisperApp) requireActivity().getApplication()).socialWebSocket().removeListener(socialListener);
            socialListener = null;
        }
        super.onStop();
    }

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
    public View onCreateView(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFriendsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new FriendsAdapter(this);
        binding.recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recycler.setAdapter(adapter);

        binding.searchInput.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        scheduleSearch(s != null ? s.toString() : "");
                    }

                    @Override
                    public void afterTextChanged(Editable s) {}
                });

        refresh();
    }

    public void refresh() {
        if (binding == null) {
            return;
        }
        binding.progress.setVisibility(View.VISIBLE);
        host.network()
                .api()
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
                                if (!response.isSuccessful() || response.body() == null) {
                                    binding.progress.setVisibility(View.GONE);
                                    toast(ApiErrors.humanMessage(response));
                                    return;
                                }
                                friends = response.body();
                                loadRequests();
                            }

                            @Override
                            public void onFailure(@NonNull Call<List<PublicUser>> call, @NonNull Throwable t) {
                                if (binding == null) {
                                    return;
                                }
                                binding.progress.setVisibility(View.GONE);
                                toast(R.string.error_network);
                            }
                        });
    }

    private void loadRequests() {
        host.network()
                .api()
                .listFriendRequests()
                .enqueue(
                        new Callback<>() {
                            @Override
                            public void onResponse(
                                    @NonNull Call<FriendRequestsResponse> call,
                                    @NonNull Response<FriendRequestsResponse> response) {
                                if (binding == null) {
                                    return;
                                }
                                binding.progress.setVisibility(View.GONE);
                                if (!response.isSuccessful() || response.body() == null) {
                                    toast(ApiErrors.humanMessage(response));
                                    return;
                                }
                                FriendRequestsResponse body = response.body();
                                incoming = body.incoming != null ? body.incoming : List.of();
                                outgoing = body.outgoing != null ? body.outgoing : List.of();
                                if (searchQuery.length() >= 2) {
                                    runSearch(searchQuery);
                                } else {
                                    rebuildList();
                                }
                            }

                            @Override
                            public void onFailure(
                                    @NonNull Call<FriendRequestsResponse> call, @NonNull Throwable t) {
                                if (binding == null) {
                                    return;
                                }
                                binding.progress.setVisibility(View.GONE);
                                toast(R.string.error_network);
                            }
                        });
    }

    private void scheduleSearch(String query) {
        searchQuery = query != null ? query.trim() : "";
        if (pendingSearch != null) {
            searchHandler.removeCallbacks(pendingSearch);
        }
        if (searchQuery.length() < 2) {
            searchResults = List.of();
            rebuildList();
            return;
        }
        pendingSearch =
                () -> {
                    if (binding != null) {
                        runSearch(searchQuery);
                    }
                };
        searchHandler.postDelayed(pendingSearch, SEARCH_DEBOUNCE_MS);
    }

    private void runSearch(String query) {
        host.network()
                .api()
                .searchUsers(query)
                .enqueue(
                        new Callback<>() {
                            @Override
                            public void onResponse(
                                    @NonNull Call<List<PublicUser>> call,
                                    @NonNull Response<List<PublicUser>> response) {
                                if (binding == null) {
                                    return;
                                }
                                searchResults =
                                        response.isSuccessful() && response.body() != null
                                                ? response.body()
                                                : List.of();
                                rebuildList();
                            }

                            @Override
                            public void onFailure(@NonNull Call<List<PublicUser>> call, @NonNull Throwable t) {
                                if (binding == null) {
                                    return;
                                }
                                searchResults = List.of();
                                rebuildList();
                            }
                        });
    }

    private void rebuildList() {
        List<FriendListItem> rows = new ArrayList<>();
        Set<String> friendSet = usernames(friends);
        Set<String> incomingSet = usernames(incoming);
        Set<String> outgoingSet = usernames(outgoing);

        if (searchQuery.length() >= 2) {
            rows.add(FriendListItem.section(getString(R.string.find_people)));
            if (searchResults.isEmpty()) {
                rows.add(FriendListItem.section(getString(R.string.no_users_found)));
            } else {
                for (PublicUser user : searchResults) {
                    if (user == null || user.username == null || user.username.isBlank()) {
                        continue;
                    }
                    String name = user.username;
                    if (friendSet.contains(name.toLowerCase(Locale.ROOT))
                            || incomingSet.contains(name.toLowerCase(Locale.ROOT))
                            || outgoingSet.contains(name.toLowerCase(Locale.ROOT))) {
                        continue;
                    }
                    rows.add(FriendListItem.search(name));
                }
            }
        }

        if (!incoming.isEmpty()) {
            rows.add(FriendListItem.section(getString(R.string.incoming_requests)));
            for (PublicUser user : incoming) {
                if (user != null && user.username != null) {
                    rows.add(FriendListItem.incoming(user.username));
                }
            }
        }

        if (!outgoing.isEmpty()) {
            rows.add(FriendListItem.section(getString(R.string.outgoing_requests)));
            for (PublicUser user : outgoing) {
                if (user != null && user.username != null) {
                    rows.add(FriendListItem.outgoing(user.username));
                }
            }
        }

        rows.add(FriendListItem.section(getString(R.string.your_friends)));
        if (friends.isEmpty()) {
            rows.add(FriendListItem.section(getString(R.string.no_friends)));
        } else {
            for (PublicUser user : friends) {
                if (user != null && user.username != null) {
                    rows.add(FriendListItem.friend(user.username));
                }
            }
        }

        adapter.setItems(rows);
        binding.emptyView.setVisibility(View.GONE);
        binding.recycler.setVisibility(View.VISIBLE);
    }

    private static Set<String> usernames(List<PublicUser> users) {
        Set<String> set = new HashSet<>();
        for (PublicUser user : users) {
            if (user != null && user.username != null) {
                set.add(user.username.toLowerCase(Locale.ROOT));
            }
        }
        return set;
    }

    @Override
    public void onFriendClicked(String username) {
        binding.progress.setVisibility(View.VISIBLE);
        host.network()
                .api()
                .startDm(new UsernameBody(username))
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
                                ChatSummary chat = response.body();
                                Intent intent = new Intent(requireContext(), ChatActivity.class);
                                intent.putExtra(MainActivity.EXTRA_CHAT_ID, chat.chatId());
                                intent.putExtra(MainActivity.EXTRA_CHAT_TITLE, chat.getDisplayTitle());
                                startActivity(intent);
                            }

                            @Override
                            public void onFailure(@NonNull Call<ChatSummary> call, @NonNull Throwable t) {
                                if (binding == null) {
                                    return;
                                }
                                binding.progress.setVisibility(View.GONE);
                                toast(R.string.error_network);
                            }
                        });
    }

    @Override
    public void onFriendLongClicked(String username) {
        new AlertDialog.Builder(requireContext())
                .setMessage(getString(R.string.unfriend_confirm, username))
                .setPositiveButton(
                        R.string.unfriend,
                        (d, w) ->
                                host.network()
                                        .api()
                                        .removeFriend(username)
                                        .enqueue(
                                                new Callback<>() {
                                                    @Override
                                                    public void onResponse(
                                                            @NonNull Call<ResponseBody> call,
                                                            @NonNull Response<ResponseBody> response) {
                                                        if (response.isSuccessful()) {
                                                            refresh();
                                                        } else {
                                                            toast(ApiErrors.humanMessage(response));
                                                        }
                                                    }

                                                    @Override
                                                    public void onFailure(
                                                            @NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                                                        toast(R.string.error_network);
                                                    }
                                                }))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    @Override
    public void onAcceptRequest(String username) {
        host.network()
                .api()
                .acceptFriendRequest(username)
                .enqueue(
                        new Callback<>() {
                            @Override
                            public void onResponse(
                                    @NonNull Call<PublicUser> call, @NonNull Response<PublicUser> response) {
                                if (response.isSuccessful()) {
                                    refresh();
                                } else {
                                    toast(ApiErrors.humanMessage(response));
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<PublicUser> call, @NonNull Throwable t) {
                                toast(R.string.error_network);
                            }
                        });
    }

    @Override
    public void onDeclineRequest(String username) {
        host.network()
                .api()
                .cancelFriendRequest(username)
                .enqueue(simpleRefreshCallback());
    }

    @Override
    public void onCancelRequest(String username) {
        host.network()
                .api()
                .cancelFriendRequest(username)
                .enqueue(simpleRefreshCallback());
    }

    @Override
    public void onSendRequest(String username) {
        host.network()
                .api()
                .sendFriendRequest(new UsernameBody(username))
                .enqueue(
                        new Callback<>() {
                            @Override
                            public void onResponse(
                                    @NonNull Call<PublicUser> call, @NonNull Response<PublicUser> response) {
                                if (response.isSuccessful()) {
                                    refresh();
                                } else {
                                    toast(ApiErrors.humanMessage(response));
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<PublicUser> call, @NonNull Throwable t) {
                                toast(R.string.error_network);
                            }
                        });
    }

    private Callback<ResponseBody> simpleRefreshCallback() {
        return new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    refresh();
                } else {
                    toast(ApiErrors.humanMessage(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                toast(R.string.error_network);
            }
        };
    }

    private void toast(int resId) {
        Toast.makeText(requireContext(), resId, Toast.LENGTH_LONG).show();
    }

    private void toast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroyView() {
        if (pendingSearch != null) {
            searchHandler.removeCallbacks(pendingSearch);
        }
        binding = null;
        super.onDestroyView();
    }
}
