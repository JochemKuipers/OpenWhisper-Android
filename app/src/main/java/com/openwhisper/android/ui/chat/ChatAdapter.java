package com.openwhisper.android.ui.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.openwhisper.android.databinding.ItemDateHeaderBinding;
import com.openwhisper.android.databinding.ItemMessageReceivedBinding;
import com.openwhisper.android.databinding.ItemMessageSentBinding;
import com.openwhisper.android.util.MessageTimestamps;

import java.util.ArrayList;
import java.util.List;

public final class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_DATE = 0;
    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;

    private final List<ChatListItem> items = new ArrayList<>();

    @Override
    public int getItemViewType(int position) {
        ChatListItem item = items.get(position);
        if (item.dateHeader) {
            return TYPE_DATE;
        }
        return item.outgoing ? TYPE_SENT : TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_DATE) {
            return new DateVH(ItemDateHeaderBinding.inflate(inflater, parent, false));
        }
        if (viewType == TYPE_SENT) {
            return new SentVH(ItemMessageSentBinding.inflate(inflater, parent, false));
        }
        return new ReceivedVH(ItemMessageReceivedBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatListItem item = items.get(position);
        if (holder instanceof DateVH) {
            ((DateVH) holder).bind(item);
        } else if (holder instanceof SentVH) {
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
        items.addAll(withDateHeaders(history));
        notifyDataSetChanged();
    }

    public void append(ChatListItem item) {
        if (item.dateHeader) {
            items.add(item);
            notifyItemInserted(items.size() - 1);
            return;
        }
        String date = MessageTimestamps.formatDateLabel(item.createdAtIso);
        if (!date.isEmpty() && !date.equals(lastDateLabel())) {
            items.add(ChatListItem.dateHeader(date));
        }
        items.add(item);
        notifyDataSetChanged();
    }

    public boolean containsMessageId(long id) {
        if (id < 0) {
            return false;
        }
        for (ChatListItem it : items) {
            if (!it.dateHeader && it.messageId == id) {
                return true;
            }
        }
        return false;
    }

    private String lastDateLabel() {
        for (int i = items.size() - 1; i >= 0; i--) {
            ChatListItem item = items.get(i);
            if (item.dateHeader) {
                return item.dateLabel;
            }
            String date = MessageTimestamps.formatDateLabel(item.createdAtIso);
            if (!date.isEmpty()) {
                return date;
            }
        }
        return "";
    }

    private static List<ChatListItem> withDateHeaders(List<ChatListItem> messages) {
        List<ChatListItem> out = new ArrayList<>();
        String lastDate = null;
        for (ChatListItem message : messages) {
            if (message.dateHeader) {
                continue;
            }
            String date = MessageTimestamps.formatDateLabel(message.createdAtIso);
            if (!date.isEmpty() && !date.equals(lastDate)) {
                out.add(ChatListItem.dateHeader(date));
                lastDate = date;
            }
            out.add(message);
        }
        return out;
    }

    static final class DateVH extends RecyclerView.ViewHolder {
        private final ItemDateHeaderBinding binding;

        DateVH(ItemDateHeaderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ChatListItem item) {
            binding.dateLabel.setText(item.dateLabel);
        }
    }

    static final class SentVH extends RecyclerView.ViewHolder {
        private final ItemMessageSentBinding binding;

        SentVH(ItemMessageSentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ChatListItem item) {
            binding.bubble.setText(item.text);
            bindTimestamp(binding.timestamp, item.timestamp);
        }
    }

    static final class ReceivedVH extends RecyclerView.ViewHolder {
        private final ItemMessageReceivedBinding binding;

        ReceivedVH(ItemMessageReceivedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ChatListItem item) {
            binding.bubble.setText(item.text);
            bindTimestamp(binding.timestamp, item.timestamp);
        }
    }

    private static void bindTimestamp(TextView timestampView, String timestamp) {
        if (timestamp.isEmpty()) {
            timestampView.setVisibility(View.GONE);
        } else {
            timestampView.setVisibility(View.VISIBLE);
            timestampView.setText(timestamp);
        }
    }
}
