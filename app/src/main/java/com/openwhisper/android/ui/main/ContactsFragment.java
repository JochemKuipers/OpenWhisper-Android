package com.openwhisper.android.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.openwhisper.android.R;
import com.openwhisper.android.data.UserSession;
import com.openwhisper.android.databinding.FragmentContactsBinding;
import com.openwhisper.android.model.ChatSummary;
import com.openwhisper.android.util.ApiErrors;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContactsFragment extends Fragment {

    private FragmentContactsBinding binding;
    private ContactsAdapter adapter;
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
        binding = FragmentContactsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new ContactsAdapter();
        binding.recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recycler.setAdapter(adapter);
        refresh();
    }

    public void refresh() {
        if (binding == null) {
            return;
        }
        binding.progress.setVisibility(View.VISIBLE);
        host.network()
                .api()
                .listChats()
                .enqueue(
                        new Callback<>() {
                            @Override
                            public void onResponse(
                                    @NonNull Call<List<ChatSummary>> call, @NonNull Response<List<ChatSummary>> response) {
                                if (binding == null) {
                                    return;
                                }
                                binding.progress.setVisibility(View.GONE);
                                if (!response.isSuccessful() || response.body() == null) {
                                    android.widget.Toast.makeText(
                                                    requireContext(), ApiErrors.humanMessage(response), android.widget.Toast.LENGTH_LONG)
                                            .show();
                                    return;
                                }
                                adapter.setItems(extractContacts(response.body()));
                                updateEmptyState();
                            }

                            @Override
                            public void onFailure(@NonNull Call<List<ChatSummary>> call, @NonNull Throwable t) {
                                if (binding == null) {
                                    return;
                                }
                                binding.progress.setVisibility(View.GONE);
                                android.widget.Toast.makeText(requireContext(), R.string.error_network, android.widget.Toast.LENGTH_LONG)
                                        .show();
                            }
                        });
    }

    private List<String> extractContacts(List<ChatSummary> chats) {
        String me = UserSession.getUsername();
        TreeSet<String> sorted = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (ChatSummary chat : chats) {
            if (chat.users == null) {
                continue;
            }
            for (ChatSummary.UserRef user : chat.users) {
                if (user == null || user.username == null || user.username.isBlank()) {
                    continue;
                }
                if (me != null && me.equalsIgnoreCase(user.username)) {
                    continue;
                }
                sorted.add(user.username);
            }
        }
        return new ArrayList<>(sorted);
    }

    private void updateEmptyState() {
        boolean empty = adapter.getItemCount() == 0;
        binding.emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.recycler.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
