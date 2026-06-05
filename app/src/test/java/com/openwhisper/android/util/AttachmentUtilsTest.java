package com.openwhisper.android.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import okhttp3.HttpUrl;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 35)
public class AttachmentUtilsTest {

    @Test
    public void isImageUrl_detectsCommonExtensions() {
        assertTrue(AttachmentUtils.isImageUrl("https://host/media/a.jpg"));
        assertTrue(AttachmentUtils.isImageUrl("https://host/media/a.png?token=1"));
        assertFalse(AttachmentUtils.isImageUrl("https://host/media/doc.pdf"));
        assertFalse(AttachmentUtils.isImageUrl(null));
    }

    @Test
    public void fileNameFromUrl_stripsQueryAndPath() {
        assertEquals("photo.png", AttachmentUtils.fileNameFromUrl("https://host/x/photo.png?v=1"));
        assertEquals("attachment", AttachmentUtils.fileNameFromUrl(""));
    }

    @Test
    public void guessMimeType_returnsKnownTypes() {
        assertEquals("image/png", AttachmentUtils.guessMimeType("file.png"));
        assertEquals("application/octet-stream", AttachmentUtils.guessMimeType("unknown"));
    }

    @Test
    public void resolveUrl_handlesRelativeMediaPaths() {
        HttpUrl siteRoot = Objects.requireNonNull(HttpUrl.parse("https://example.com/"));
        assertEquals(
                "https://example.com/media/a.jpg",
                AttachmentUtils.resolveUrl(siteRoot, "/media/a.jpg"));
        assertEquals(
                "https://cdn.example/file.pdf",
                AttachmentUtils.resolveUrl(siteRoot, "https://cdn.example/file.pdf"));
    }

    @Test
    public void readStreamToBytes_readsAllBytes() throws Exception {
        byte[] input = "hello".getBytes(StandardCharsets.UTF_8);
        byte[] out =
                AttachmentUtils.readStreamToBytes(new ByteArrayInputStream(input));
        assertEquals("hello", new String(out, StandardCharsets.UTF_8));
    }
}
