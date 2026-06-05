package com.openwhisper.android.util;

import android.content.ContentValues;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.openwhisper.android.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public final class AttachmentDownloader {

    private AttachmentDownloader() {}

    public static void download(
            @NonNull Context context,
            @NonNull OkHttpClient client,
            @NonNull HttpUrl siteRoot,
            @NonNull String url,
            @Nullable Runnable onFinished) {
        String resolved = AttachmentUtils.resolveUrl(siteRoot, url);
        if (resolved.isEmpty()) {
            showToast(context, R.string.error_generic);
            runOnMain(onFinished);
            return;
        }
        new Thread(
                        () -> {
                            try {
                                Request request = new Request.Builder().url(resolved).build();
                                try (Response response = client.newCall(request).execute()) {
                                    if (!response.isSuccessful()) {
                                        showToast(context, R.string.error_generic);
                                        return;
                                    }
                                    String fileName = AttachmentUtils.fileNameFromUrl(resolved);
                                    String mimeType = mimeTypeFromResponse(response, fileName);
                                    saveToDownloads(context, response.body().byteStream(), fileName, mimeType);
                                    showToast(
                                            context,
                                            context.getString(R.string.download_complete, fileName));
                                }
                            } catch (Exception e) {
                                showToast(context, R.string.error_generic);
                            } finally {
                                runOnMain(onFinished);
                            }
                        })
                .start();
    }

    private static String mimeTypeFromResponse(@NonNull Response response, @NonNull String fileName) {
        String contentType = response.header("Content-Type");
        if (contentType != null && !contentType.isBlank()) {
            int semi = contentType.indexOf(';');
            return semi >= 0 ? contentType.substring(0, semi).trim() : contentType.trim();
        }
        return AttachmentUtils.guessMimeType(fileName);
    }

    private static void saveToDownloads(
            @NonNull Context context,
            @NonNull InputStream in,
            @NonNull String fileName,
            @NonNull String mimeType)
            throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveViaMediaStore(context, in, fileName, mimeType);
        } else {
            saveViaLegacyDownloads(context, in, fileName, mimeType);
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private static void saveViaMediaStore(
            @NonNull Context context,
            @NonNull InputStream in,
            @NonNull String fileName,
            @NonNull String mimeType)
            throws IOException {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
        values.put(MediaStore.Downloads.MIME_TYPE, mimeType);
        values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
        values.put(MediaStore.Downloads.IS_PENDING, 1);

        android.net.Uri uri =
                context.getContentResolver()
                        .insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        if (uri == null) {
            throw new IOException("Could not create download entry");
        }
        try (OutputStream out = context.getContentResolver().openOutputStream(uri)) {
            if (out == null) {
                throw new IOException("Could not open download stream");
            }
            copy(in, out);
        }
        values.clear();
        values.put(MediaStore.Downloads.IS_PENDING, 0);
        context.getContentResolver().update(uri, values, null, null);
    }

    private static void saveViaLegacyDownloads(
            @NonNull Context context,
            @NonNull InputStream in,
            @NonNull String fileName,
            @NonNull String mimeType)
            throws IOException {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Could not create Downloads folder");
        }
        File out = uniqueFile(dir, fileName);
        try (FileOutputStream fos = new FileOutputStream(out)) {
            copy(in, fos);
        }
        MediaScannerConnection.scanFile(
                context, new String[] {out.getAbsolutePath()}, new String[] {mimeType}, null);
    }

    private static File uniqueFile(@NonNull File dir, @NonNull String fileName) {
        File candidate = new File(dir, fileName);
        if (!candidate.exists()) {
            return candidate;
        }
        int dot = fileName.lastIndexOf('.');
        String base = dot > 0 ? fileName.substring(0, dot) : fileName;
        String ext = dot > 0 ? fileName.substring(dot) : "";
        for (int i = 1; i < 100; i++) {
            candidate = new File(dir, base + " (" + i + ")" + ext);
            if (!candidate.exists()) {
                return candidate;
            }
        }
        return new File(dir, base + " (" + System.currentTimeMillis() + ")" + ext);
    }

    private static void copy(@NonNull InputStream in, @NonNull OutputStream out) throws IOException {
        byte[] buffer = new byte[8192];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    private static void runOnMain(@Nullable Runnable action) {
        if (action == null) {
            return;
        }
        new Handler(Looper.getMainLooper()).post(action);
    }

    private static void showToast(@NonNull Context context, int messageRes) {
        new Handler(Looper.getMainLooper())
                .post(() -> Toast.makeText(context, messageRes, Toast.LENGTH_SHORT).show());
    }

    private static void showToast(@NonNull Context context, @NonNull String message) {
        new Handler(Looper.getMainLooper())
                .post(() -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show());
    }
}
