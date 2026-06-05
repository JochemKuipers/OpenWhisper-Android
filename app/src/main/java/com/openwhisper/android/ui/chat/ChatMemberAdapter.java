package com.openwhisper.android.ui.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.openwhisper.android.databinding.ItemChatMemberBinding;
import com.openwhisper.android.model.ChatSummary;
import com.openwhisper.android.util.AvatarText;

import java.util.ArrayList;
import java.util.List;

public final class ChatMemberAdapter extends RecyclerView.Adapter<ChatMemberAdapter.VH> {

    public interface Listener {
        void onRemoveMember(String username);
    }

    private final Listener listener;
    private final List<ChatSummary.UserRef> members = new ArrayList<>();
    private String adminUsername = "";
    private String currentUsername = "";
    private boolean canManage;

    public ChatMemberAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setMembers(
            List<ChatSummary.UserRef> users, String adminUsername, String currentUsername, boolean canManage) {
        int oldSize = members.size();
        members.clear();
        this.adminUsername = adminUsername != null ? adminUsername : "";
        this.currentUsername = currentUsername != null ? currentUsername : "";
        this.canManage = canManage;
        if (users != null) {
            members.addAll(users);
        }
        if (oldSize != 0) {
            notifyItemRangeRemoved(0, oldSize);
        }
        if (members.size() != 0) {
            notifyItemRangeInserted(0, members.size());
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemChatMemberBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(members.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    final class VH extends RecyclerView.ViewHolder {
        private final ItemChatMemberBinding binding;

        VH(ItemChatMemberBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ChatSummary.UserRef user, Listener listener) {
            if (user == null || user.username == null) {
                return;
            }
            binding.username.setText(user.username);
            AvatarText.apply(binding.avatar, user.username);
            boolean isAdmin = user.username.equalsIgnoreCase(adminUsername);
            binding.adminBadge.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
            boolean showRemove =
                    canManage
                            && !isAdmin
                            && !user.username.equalsIgnoreCase(currentUsername);
            binding.removeButton.setVisibility(showRemove ? View.VISIBLE : View.GONE);
            binding.removeButton.setOnClickListener(v -> listener.onRemoveMember(user.username));
        }
    }
}
