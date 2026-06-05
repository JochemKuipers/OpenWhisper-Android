package com.openwhisper.android.util;

import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public final class AttachmentUtils {

    public static final long MAX_ATTACHMENT_BYTES = 10L * 1024L * 1024L;

    private AttachmentUtils() {}

    public static boolean isImageUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        String lower = url.toLowerCase(Locale.ROOT);
        return lower.endsWith(".jpg")
                || lower.endsWith(".jpeg")
                || lower.endsWith(".png")
                || lower.endsWith(".gif")
                || lower.endsWith(".webp")
                || lower.contains(".jpg?")
                || lower.contains(".jpeg?")
                || lower.contains(".png?")
                || lower.contains(".gif?")
                || lower.contains(".webp?");
    }

    public static String fileNameFromUrl(String url) {
        if (url == null || url.isBlank()) {
            return "attachment";
        }
        String path = url;
        int query = path.indexOf('?');
        if (query >= 0) {
            path = path.substring(0, query);
        }
        int slash = path.lastIndexOf('/');
        if (slash >= 0 && slash < path.length() - 1) {
            return path.substring(slash + 1);
        }
        return "attachment";
    }

    public static String guessMimeType(String fileName) {
        String ext = MimeTypeMap.getFileExtensionFromUrl(fileName);
        if (ext != null) {
            String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.toLowerCase(Locale.ROOT));
            if (mime != null) {
                return mime;
            }
        }
        return "application/octet-stream";
    }

    /** Resolves relative {@code /media/...} paths to absolute download URLs. */
    public static String resolveUrl(@NonNull okhttp3.HttpUrl siteRoot, @NonNull String url) {
        if (url.isBlank()) {
            return "";
        }
        if (url.contains("://")) {
            return url;
        }
        okhttp3.HttpUrl resolved = siteRoot.resolve(url);
        return resolved != null ? resolved.toString() : url;
    }

    public static byte[] readStreamToBytes(@NonNull InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        return out.toByteArray();
    }
}
