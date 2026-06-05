package com.openwhisper.android.util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.openwhisper.android.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public final class AttachmentOpener {

    private AttachmentOpener() {}

    public static void open(
            @NonNull Context context,
            @NonNull OkHttpClient client,
            @NonNull HttpUrl siteRoot,
            @NonNull String url) {
        String resolved = resolveUrl(siteRoot, url);
        if (resolved.isEmpty()) {
            showError(context, R.string.error_generic);
            return;
        }
        new Thread(
                        () -> {
                            try {
                                Request request = new Request.Builder().url(resolved).build();
                                try (Response response = client.newCall(request).execute()) {
                                    if (!response.isSuccessful() || response.body() == null) {
                                        showError(context, R.string.error_generic);
                                        return;
                                    }
                                    String fileName = AttachmentUtils.fileNameFromUrl(resolved);
                                    File dir = new File(context.getCacheDir(), "attachments");
                                    if (!dir.exists() && !dir.mkdirs()) {
                                        showError(context, R.string.error_generic);
                                        return;
                                    }
                                    File out = new File(dir, fileName);
                                    try (InputStream in = response.body().byteStream();
                                            FileOutputStream fos = new FileOutputStream(out)) {
                                        byte[] buffer = new byte[8192];
                                        int read;
                                        while ((read = in.read(buffer)) != -1) {
                                            fos.write(buffer, 0, read);
                                        }
                                    }
                                    Uri uri =
                                            FileProvider.getUriForFile(
                                                    context,
                                                    context.getPackageName() + ".fileprovider",
                                                    out);
                                    String mimeType = mimeTypeFromResponse(response, fileName);
                                    launchViewer(context, uri, mimeType);
                                }
                            } catch (Exception e) {
                                showError(context, R.string.error_generic);
                            }
                        })
                .start();
    }

    static String resolveUrl(@NonNull HttpUrl siteRoot, @NonNull String url) {
        if (url.isBlank()) {
            return "";
        }
        if (url.contains("://")) {
            return url;
        }
        HttpUrl resolved = siteRoot.resolve(url);
        return resolved != null ? resolved.toString() : url;
    }

    private static String mimeTypeFromResponse(@NonNull Response response, @NonNull String fileName) {
        String contentType = response.header("Content-Type");
        if (contentType != null && !contentType.isBlank()) {
            int semi = contentType.indexOf(';');
            return semi >= 0 ? contentType.substring(0, semi).trim() : contentType.trim();
        }
        return AttachmentUtils.guessMimeType(fileName);
    }

    private static void launchViewer(@NonNull Context context, @NonNull Uri uri, @NonNull String mimeType) {
        new Handler(Looper.getMainLooper())
                .post(
                        () -> {
                            try {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setDataAndType(uri, mimeType);
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                                PackageManager pm = context.getPackageManager();
                                List<ResolveInfo> handlers =
                                        pm.queryIntentActivities(
                                                intent, PackageManager.MATCH_DEFAULT_ONLY);
                                if (handlers.isEmpty()) {
                                    showError(context, R.string.no_app_to_open_attachment);
                                    return;
                                }
                                for (ResolveInfo info : handlers) {
                                    context.grantUriPermission(
                                            info.activityInfo.packageName,
                                            uri,
                                            Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                }
                                context.startActivity(
                                        Intent.createChooser(
                                                intent, context.getString(R.string.open_attachment)));
                            } catch (ActivityNotFoundException e) {
                                showError(context, R.string.no_app_to_open_attachment);
                            }
                        });
    }

    private static void showError(Context context, int messageRes) {
        new Handler(Looper.getMainLooper())
                .post(() -> Toast.makeText(context, messageRes, Toast.LENGTH_SHORT).show());
    }
}
