package de.joernott.c0de.core.value;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/** A TIME value: milliseconds since 1970-01-01 00:00:00 UTC. */
public record TimeValue(long millis) implements Value {
    /** ISO 8601 in UTC with milliseconds, e.g. 2026-10-19T12:05:09.250Z. */
    public static final DateTimeFormatter TEXT_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);

    @Override
    public Type type() {
        return Type.Simple.TIME;
    }

    @Override
    public String text() {
        return TEXT_FORMAT.format(Instant.ofEpochMilli(millis));
    }
}
