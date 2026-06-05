package com.openwhisper.android.ui.main;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.openwhisper.android.databinding.ItemContactBinding;
import com.openwhisper.android.util.AvatarText;

import java.util.ArrayList;
import java.util.List;

public final class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.VH> {

    private final List<String> items = new ArrayList<>();

    public void setItems(List<String> usernames) {
        int oldSize = items.size();
        items.clear();
        if (usernames != null) {
            items.addAll(usernames);
        }
        notifyRangeReplaced(oldSize, items.size());
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemContactBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static final class VH extends RecyclerView.ViewHolder {
        private final ItemContactBinding binding;

        VH(ItemContactBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(String username) {
            binding.username.setText(username);
            AvatarText.apply(binding.avatar, username);
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
