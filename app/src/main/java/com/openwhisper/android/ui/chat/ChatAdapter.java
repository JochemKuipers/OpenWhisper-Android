package com.openwhisper.android.ui.chat;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.openwhisper.android.databinding.ItemMessageReceivedBinding;
import com.openwhisper.android.databinding.ItemMessageSentBinding;

import java.util.ArrayList;
import java.util.List;

public final class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;

    private final List<ChatListItem> items = new ArrayList<>();

    @Override
    public int getItemViewType(int position) {
        return items.get(position).outgoing ? TYPE_SENT : TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_SENT) {
            return new SentVH(ItemMessageSentBinding.inflate(inflater, parent, false));
        }
        return new ReceivedVH(ItemMessageReceivedBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatListItem item = items.get(position);
        if (holder instanceof SentVH) {
            ((SentVH) holder).bind(item);
        } else if (holder instanceof ReceivedVH) {
            ((ReceivedVH) holder).bind(item);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setHistory(List<ChatListItem> history) {
        items.clear();
        items.addAll(history);
        notifyDataSetChanged();
    }

    public void append(ChatListItem item) {
        items.add(item);
        notifyItemInserted(items.size() - 1);
    }

    public boolean containsMessageId(long id) {
        if (id < 0) {
            return false;
        }
        for (ChatListItem it : items) {
            if (it.messageId == id) {
                return true;
            }
        }
        return false;
    }

    static final class SentVH extends RecyclerView.ViewHolder {
        private final ItemMessageSentBinding binding;

        SentVH(ItemMessageSentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ChatListItem item) {
            binding.bubble.setText(item.text);
        }
    }

    static final class ReceivedVH extends RecyclerView.ViewHolder {
        private final ItemMessageReceivedBinding binding;

        ReceivedVH(ItemMessageReceivedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ChatListItem item) {
            binding.sender.setText(item.senderUsername);
            binding.bubble.setText(item.text);
        }
    }
}
