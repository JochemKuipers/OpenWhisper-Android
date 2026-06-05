package com.openwhisper.android.util;

import androidx.annotation.Nullable;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;

public final class MessageTimestamps {

    private MessageTimestamps() {}

    public static String format(@Nullable String isoTimestamp) {
        if (isoTimestamp == null || isoTimestamp.isBlank()) {
            return "";
        }
        try {
            Instant instant = parseInstant(isoTimestamp.trim());
            return DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
                    .format(instant.atZone(ZoneId.systemDefault()));
        } catch (DateTimeParseException ignored) {
            return "";
        }
    }

    public static String formatNow() {
        return DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
                .format(Instant.now().atZone(ZoneId.systemDefault()));
    }

    public static String formatDateLabel(@Nullable String isoTimestamp) {
        if (isoTimestamp == null || isoTimestamp.isBlank()) {
            return "";
        }
        try {
            ZonedDateTime time = parseInstant(isoTimestamp.trim()).atZone(ZoneId.systemDefault());
            LocalDate date = time.toLocalDate();
            LocalDate today = LocalDate.now();
            if (date.equals(today)) {
                return "Today";
            }
            if (date.equals(today.minusDays(1))) {
                return "Yesterday";
            }
            return DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).format(time);
        } catch (DateTimeParseException ignored) {
            return "";
        }
    }

    private static Instant parseInstant(String value) {
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException e) {
            return OffsetDateTime.parse(value).toInstant();
        }
    }
}
