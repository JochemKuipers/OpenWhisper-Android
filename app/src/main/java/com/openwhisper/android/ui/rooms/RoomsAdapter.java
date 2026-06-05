package com.openwhisper.android.ui.rooms;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.openwhisper.android.databinding.ItemRoomBinding;
import com.openwhisper.android.model.ChatSummary;
import com.openwhisper.android.util.AvatarText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class RoomsAdapter extends RecyclerView.Adapter<RoomsAdapter.VH> {

    public interface Listener {
        void onChatClicked(ChatSummary chat);
    }

    private final List<ChatSummary> allItems = new ArrayList<>();
    private final List<ChatSummary> visibleItems = new ArrayList<>();
    private final Listener listener;
    private final String youLabel;
    private String query = "";

    public RoomsAdapter(@NonNull Listener listener, @NonNull String youLabel) {
        this.listener = listener;
        this.youLabel = youLabel;
    }

    public void setItems(List<ChatSummary> next) {
        allItems.clear();
        if (next != null) {
            allItems.addAll(next);
        }
        applyFilter();
    }

    public void filter(String searchQuery) {
        query = searchQuery != null ? searchQuery.trim().toLowerCase(Locale.ROOT) : "";
        applyFilter();
    }

    public boolean isFiltering() {
        return !query.isEmpty();
    }

    private void applyFilter() {
        int oldSize = visibleItems.size();
        visibleItems.clear();
        if (query.isEmpty()) {
            visibleItems.addAll(allItems);
        } else {
            for (ChatSummary chat : allItems) {
                String title = chat.getDisplayTitle().toLowerCase(Locale.ROOT);
                String subtitle = chat.getMemberSubtitle().toLowerCase(Locale.ROOT);
                if (title.contains(query) || subtitle.contains(query)) {
                    visibleItems.add(chat);
                }
            }
        }
        notifyRangeReplaced(oldSize, visibleItems.size());
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemRoomBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(visibleItems.get(position), listener, youLabel);
    }

    @Override
    public int getItemCount() {
        return visibleItems.size();
    }

    public static final class VH extends RecyclerView.ViewHolder {
        private final ItemRoomBinding binding;

        VH(ItemRoomBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ChatSummary chat, Listener listener, String youLabel) {
            String title = chat.getDisplayTitle();
            binding.title.setText(title);
            CharSequence subtitle = chat.getMemberSubtitleStyled(youLabel);
            binding.subtitle.setText(subtitle);
            binding.subtitle.setVisibility(TextUtils.isEmpty(subtitle) ? android.view.View.GONE : android.view.View.VISIBLE);
            AvatarText.apply(binding.avatar, title);
            binding.roomContent.setOnClickListener(v -> listener.onChatClicked(chat));
        }
    }

    private void notifyRangeReplaced(int oldSize, int newSize) {
        if (oldSize != 0) {
            notifyItemRangeRemoved(0, oldSize);
        }
        if (newSize != 0) {
            notifyItemRangeInserted(0, newSize);
        }
    }
}
