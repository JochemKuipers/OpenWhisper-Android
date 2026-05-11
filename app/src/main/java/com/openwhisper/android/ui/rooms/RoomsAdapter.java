package com.openwhisper.android.ui.rooms;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.openwhisper.android.databinding.ItemRoomBinding;
import com.openwhisper.android.model.ChatSummary;

import java.util.ArrayList;
import java.util.List;

public final class RoomsAdapter extends RecyclerView.Adapter<RoomsAdapter.VH> {

    public interface Listener {
        void onChatClicked(ChatSummary chat);
    }

    private final List<ChatSummary> items = new ArrayList<>();
    private final Listener listener;

    public RoomsAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<ChatSummary> next) {
        items.clear();
        if (next != null) {
            items.addAll(next);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemRoomBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class VH extends RecyclerView.ViewHolder {
        private final ItemRoomBinding binding;

        VH(ItemRoomBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ChatSummary chat, Listener listener) {
            binding.title.setText(chat.displayTitle());
            String sub = chat.memberSubtitle();
            binding.subtitle.setText(sub);
            binding.getRoot().setOnClickListener(v -> listener.onChatClicked(chat));
        }
    }
}
