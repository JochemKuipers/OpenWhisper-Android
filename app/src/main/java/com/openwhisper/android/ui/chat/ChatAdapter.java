package com.openwhisper.android.ui.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.openwhisper.android.R;
import com.openwhisper.android.databinding.ItemDateHeaderBinding;
import com.openwhisper.android.databinding.ItemMessageReceivedBinding;
import com.openwhisper.android.databinding.ItemMessageSentBinding;
import com.openwhisper.android.util.AttachmentDownloader;
import com.openwhisper.android.util.AttachmentOpener;
import com.openwhisper.android.util.MessageTimestamps;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import coil.ImageLoader;
import coil.request.ImageRequest;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

public final class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_DATE = 0;
    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;

    private final OkHttpClient okHttpClient;
    private final HttpUrl attachmentSiteRoot;
    private final ImageLoader imageLoader;
    private final List<ChatListItem> items = new ArrayList<>();
    private final Set<String> activeDownloads = new HashSet<>();

    public ChatAdapter(OkHttpClient okHttpClient, HttpUrl attachmentSiteRoot) {
        this.okHttpClient = okHttpClient;
        this.attachmentSiteRoot = attachmentSiteRoot;
        this.imageLoader = null;
    }

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
        int oldSize = items.size();
        items.clear();
        items.addAll(withDateHeaders(history));
        notifyRangeReplaced(oldSize, items.size());
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
            notifyItemInserted(items.size() - 1);
        }
        items.add(item);
        notifyItemInserted(items.size() - 1);
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

    private ImageLoader imageLoaderFor(View view) {
        if (imageLoader != null) {
            return imageLoader;
        }
        return new ImageLoader.Builder(view.getContext()).okHttpClient(okHttpClient).build();
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

    private void notifyRangeReplaced(int oldSize, int newSize) {
        if (oldSize != 0) {
            notifyItemRangeRemoved(0, oldSize);
        }
        if (newSize != 0) {
            notifyItemRangeInserted(0, newSize);
        }
    }

    private void bindMessageContent(
            View root,
            TextView bubble,
            View attachmentContainer,
            ImageView attachmentImage,
            TextView attachmentFile,
            ImageButton attachmentDownload,
            ProgressBar attachmentDownloadProgress,
            ChatListItem item,
            int position) {
        bindTimestamp(root.findViewById(com.openwhisper.android.R.id.timestamp), item.timestamp);

        if (!item.hasAttachment()) {
            attachmentContainer.setVisibility(View.GONE);
            attachmentImage.setVisibility(View.GONE);
            attachmentFile.setVisibility(View.GONE);
            if (item.text.isEmpty()) {
                bubble.setVisibility(View.GONE);
            } else {
                bubble.setVisibility(View.VISIBLE);
                bubble.setText(item.text);
            }
            return;
        }

        attachmentContainer.setVisibility(View.VISIBLE);
        setDownloadLoading(
                attachmentDownload,
                attachmentDownloadProgress,
                activeDownloads.contains(item.attachmentUrl));
        attachmentDownload.setOnClickListener(
                v -> {
                    if (activeDownloads.contains(item.attachmentUrl)) {
                        return;
                    }
                    activeDownloads.add(item.attachmentUrl);
                    setDownloadLoading(attachmentDownload, attachmentDownloadProgress, true);
                    Toast.makeText(v.getContext(), R.string.download_started, Toast.LENGTH_SHORT).show();
                    AttachmentDownloader.download(
                            v.getContext(),
                            okHttpClient,
                            attachmentSiteRoot,
                            item.attachmentUrl,
                            () -> {
                                activeDownloads.remove(item.attachmentUrl);
                                if (position != RecyclerView.NO_POSITION) {
                                    notifyItemChanged(position);
                                }
                            });
                });

        if (item.attachmentKind == ChatListItem.AttachmentKind.IMAGE) {
            attachmentImage.setVisibility(View.VISIBLE);
            attachmentFile.setVisibility(View.GONE);
            imageLoaderFor(root)
                    .enqueue(
                            new ImageRequest.Builder(root.getContext())
                                    .data(item.attachmentUrl)
                                    .crossfade(true)
                                    .target(attachmentImage)
                                    .build());
            attachmentImage.setOnClickListener(
                    v ->
                            AttachmentOpener.open(
                                    v.getContext(),
                                    okHttpClient,
                                    attachmentSiteRoot,
                                    item.attachmentUrl));
        } else {
            attachmentImage.setVisibility(View.GONE);
            attachmentFile.setVisibility(View.VISIBLE);
            attachmentFile.setText(item.attachmentLabel);
            attachmentFile.setOnClickListener(
                    v ->
                            AttachmentOpener.open(
                                    v.getContext(),
                                    okHttpClient,
                                    attachmentSiteRoot,
                                    item.attachmentUrl));
        }

        if (item.text.isEmpty()) {
            bubble.setVisibility(View.GONE);
        } else {
            bubble.setVisibility(View.VISIBLE);
            bubble.setText(item.text);
        }
    }

    private static void setDownloadLoading(
            ImageButton downloadButton, ProgressBar progressBar, boolean loading) {
        downloadButton.setEnabled(!loading);
        downloadButton.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
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

    final class SentVH extends RecyclerView.ViewHolder {
        private final ItemMessageSentBinding binding;

        SentVH(ItemMessageSentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ChatListItem item) {
            bindMessageContent(
                    binding.getRoot(),
                    binding.bubble,
                    binding.attachmentContainer,
                    binding.attachmentImage,
                    binding.attachmentFile,
                    binding.attachmentDownload,
                    binding.attachmentDownloadProgress,
                    item,
                    getBindingAdapterPosition());
        }
    }

    final class ReceivedVH extends RecyclerView.ViewHolder {
        private final ItemMessageReceivedBinding binding;

        ReceivedVH(ItemMessageReceivedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ChatListItem item) {
            bindMessageContent(
                    binding.getRoot(),
                    binding.bubble,
                    binding.attachmentContainer,
                    binding.attachmentImage,
                    binding.attachmentFile,
                    binding.attachmentDownload,
                    binding.attachmentDownloadProgress,
                    item,
                    getBindingAdapterPosition());
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
