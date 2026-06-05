package com.openwhisper.android.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AvatarTextTest {

    @Test
    public void initials_singleAndMultiWord() {
        assertEquals("?", AvatarText.initials(""));
        assertEquals("A", AvatarText.initials("alice"));
        assertEquals("SJ", AvatarText.initials("Sarah Johnson"));
    }
}
