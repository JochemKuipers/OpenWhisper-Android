package com.openwhisper.android.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class MessageTimestampsTest {

    @Test
    public void format_returnsEmptyForInvalidInput() {
        assertEquals("", MessageTimestamps.format(null));
        assertEquals("", MessageTimestamps.format("not-a-date"));
    }

    @Test
    public void formatDateLabel_todayAndYesterday() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        String todayIso = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(now);
        assertEquals("Today", MessageTimestamps.formatDateLabel(todayIso));

        LocalDate yesterday = LocalDate.now().minusDays(1);
        ZonedDateTime yTime = yesterday.atTime(12, 0).atZone(ZoneId.systemDefault());
        String yesterdayIso = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(yTime);
        assertEquals("Yesterday", MessageTimestamps.formatDateLabel(yesterdayIso));
    }

    @Test
    public void formatDateLabel_olderDateIsNonEmpty() {
        assertFalse(MessageTimestamps.formatDateLabel("2020-06-01T12:00:00Z").isEmpty());
    }
}
