package de.joernott.c0de.core.value;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

/** The values of LITERAL blocks: their types and how they are parsed from text. */
public final class Literals {
    /** The types a LITERAL can have. */
    public static final List<Type> TYPES = List.of(
            Type.Simple.BOOLEAN,
            Type.Simple.INTEGER,
            Type.Simple.FLOAT,
            Type.Simple.CHAR,
            Type.Str.UNLIMITED,
            Type.Simple.TIME);

    private Literals() {}

    /**
     * Parses the text of a literal of a type, as entered in the dialog of a LITERAL block.
     *
     * @param zone the time zone for TIME values without an offset (the time zone of the server)
     * @throws IllegalArgumentException if the text is not a valid value of the type
     */
    public static Value parse(Type type, String text, ZoneId zone) {
        return switch (type) {
            case Type.Simple simple -> parseSimple(simple, text.trim(), zone);
            case Type.Str str -> new StringValue(text);
            default -> throw new IllegalArgumentException("A literal can't have the type " + type);
        };
    }

    private static Value parseSimple(Type.Simple type, String text, ZoneId zone) {
        return switch (type) {
            case BOOLEAN ->
                switch (text.toLowerCase(Locale.ROOT)) {
                    case "true" -> BooleanValue.TRUE;
                    case "false" -> BooleanValue.FALSE;
                    default -> throw new IllegalArgumentException("A BOOLEAN is true or false: " + text);
                };
            case INTEGER -> {
                try {
                    yield new IntegerValue(Long.parseLong(text));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Not an INTEGER: " + text);
                }
            }
            case FLOAT -> {
                try {
                    double value = Double.parseDouble(text);
                    if (Double.isNaN(value) || Double.isInfinite(value) || text.matches(".*[a-zA-Z&&[^eE]].*")) {
                        throw new NumberFormatException();
                    }
                    yield new FloatValue(value);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Not a FLOAT: " + text);
                }
            }
            case CHAR -> {
                if (text.codePointCount(0, text.length()) != 1) {
                    throw new IllegalArgumentException("A CHAR is exactly one character: " + text);
                }
                yield new CharValue(text.codePointAt(0));
            }
            case TIME -> new TimeValue(parseTime(text, zone));
        };
    }

    /** Milliseconds of an ISO 8601 date and time with optional milliseconds and optional offset. */
    public static long parseTime(String text, ZoneId zone) {
        try {
            return OffsetDateTime.parse(text).toInstant().toEpochMilli();
        } catch (DateTimeParseException withoutOffset) {
            try {
                return LocalDateTime.parse(text).atZone(zone).toInstant().toEpochMilli();
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Not an ISO 8601 date and time: " + text);
            }
        }
    }
}
