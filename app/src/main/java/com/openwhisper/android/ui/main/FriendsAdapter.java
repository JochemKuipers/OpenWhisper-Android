package com.openwhisper.android.ui.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.openwhisper.android.databinding.ItemFriendRowBinding;
import com.openwhisper.android.databinding.ItemFriendSectionBinding;
import com.openwhisper.android.util.AvatarText;

import java.util.ArrayList;
import java.util.List;

public final class FriendsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VT_SECTION = 0;
    private static final int VT_ROW = 1;

    public interface Listener {
        void onFriendClicked(String username);

        void onFriendLongClicked(String username);

        void onAcceptRequest(String username);

        void onDeclineRequest(String username);

        void onCancelRequest(String username);

        void onSendRequest(String username);
    }

    private final Listener listener;
    private final List<FriendListItem> items = new ArrayList<>();

    public FriendsAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<FriendListItem> rows) {
        int oldSize = items.size();
        items.clear();
        if (rows != null) {
            items.addAll(rows);
        }
        if (oldSize != 0) {
            notifyItemRangeRemoved(0, oldSize);
        }
        if (items.size() != 0) {
            notifyItemRangeInserted(0, items.size());
        }
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).kind == FriendListItem.Kind.SECTION ? VT_SECTION : VT_ROW;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VT_SECTION) {
            return new SectionVH(ItemFriendSectionBinding.inflate(inflater, parent, false));
        }
        return new RowVH(ItemFriendRowBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        FriendListItem item = items.get(position);
        if (holder instanceof SectionVH sectionVH) {
            sectionVH.bind(item.sectionTitle);
        } else if (holder instanceof RowVH rowVH) {
            rowVH.bind(item, listener);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class SectionVH extends RecyclerView.ViewHolder {
        private final ItemFriendSectionBinding binding;

        SectionVH(ItemFriendSectionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(String title) {
            binding.sectionTitle.setText(title);
        }
    }

    static final class RowVH extends RecyclerView.ViewHolder {
        private final ItemFriendRowBinding binding;

        RowVH(ItemFriendRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(FriendListItem item, Listener listener) {
            binding.username.setText(item.username);
            AvatarText.apply(binding.avatar, item.username);
            binding.primaryAction.setVisibility(View.GONE);
            binding.secondaryAction.setVisibility(View.GONE);
            binding.getRoot().setOnClickListener(null);
            binding.getRoot().setOnLongClickListener(null);

            switch (item.kind) {
                case FRIEND -> {
                    binding.getRoot().setOnClickListener(v -> listener.onFriendClicked(item.username));
                    binding.getRoot().setOnLongClickListener(
                            v -> {
                                listener.onFriendLongClicked(item.username);
                                return true;
                            });
                }
                case INCOMING -> {
                    binding.primaryAction.setVisibility(View.VISIBLE);
                    binding.secondaryAction.setVisibility(View.VISIBLE);
                    binding.primaryAction.setText(com.openwhisper.android.R.string.accept);
                    binding.secondaryAction.setText(com.openwhisper.android.R.string.decline);
                    binding.primaryAction.setOnClickListener(v -> listener.onAcceptRequest(item.username));
                    binding.secondaryAction.setOnClickListener(v -> listener.onDeclineRequest(item.username));
                }
                case OUTGOING -> {
                    binding.primaryAction.setVisibility(View.VISIBLE);
                    binding.primaryAction.setText(com.openwhisper.android.R.string.cancel);
                    binding.primaryAction.setOnClickListener(v -> listener.onCancelRequest(item.username));
                }
                case SEARCH -> {
                    binding.primaryAction.setVisibility(View.VISIBLE);
                    binding.primaryAction.setText(com.openwhisper.android.R.string.add_friend);
                    binding.primaryAction.setOnClickListener(v -> listener.onSendRequest(item.username));
                }
                default -> {}
            }
        }
    }
}
