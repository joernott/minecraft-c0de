package de.joernott.c0de.core.library.standard;

import static de.joernott.c0de.core.library.standard.Functions.invalid;

import de.joernott.c0de.core.error.C0deException;
import de.joernott.c0de.core.error.ErrorCode;
import de.joernott.c0de.core.value.BooleanValue;
import de.joernott.c0de.core.value.CharValue;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.TimeValue;
import de.joernott.c0de.core.value.Value;
import de.joernott.c0de.core.value.Values;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.IsoFields;
import java.util.IllegalFormatException;
import java.util.Locale;

/**
 * The function FORMAT of c0de/strings (docs/concept/10-lib-strings.md, "FORMAT"): printf-like conversions,
 * all applied to the same value. Most conversions are passed to {@link String#format}; {@code %g}, {@code %n},
 * {@code %tu} and {@code %tV} are implemented here.
 */
final class Format {
    private static final String FLAGS = "-0+ ,";

    private Format() {}

    /**
     * Formats a value.
     *
     * @throws C0deException TYPE_MISMATCH if a conversion doesn't fit the type of the value, INVALID_ARGUMENT if
     *     the format string is invalid
     */
    static String format(Value value, String format, ZoneId zone) {
        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < format.length()) {
            char c = format.charAt(i);
            if (c != '%') {
                result.append(c);
                i++;
                continue;
            }
            int start = i++;
            while (i < format.length() && FLAGS.indexOf(format.charAt(i)) >= 0) {
                i++;
            }
            String flags = format.substring(start + 1, i);
            int widthStart = i;
            while (i < format.length() && Character.isDigit(format.charAt(i))) {
                i++;
            }
            String width = format.substring(widthStart, i);
            String precision = "";
            if (i < format.length() && format.charAt(i) == '.') {
                int precisionStart = ++i;
                while (i < format.length() && Character.isDigit(format.charAt(i))) {
                    i++;
                }
                precision = format.substring(precisionStart, i);
                if (precision.isEmpty()) {
                    throw invalid("A precision needs digits: " + format.substring(start, i));
                }
            }
            if (i >= format.length()) {
                throw invalid("Incomplete conversion at the end of the format: " + format.substring(start));
            }
            char conversion = format.charAt(i++);
            String time = "";
            if (conversion == 't' || conversion == 'T') {
                if (i >= format.length()) {
                    throw invalid("A time conversion needs a letter after %t");
                }
                time = String.valueOf(format.charAt(i++));
            }
            Spec spec = new Spec(format.substring(start, i), flags, width, precision, conversion, time);
            result.append(convert(spec, value, zone));
        }
        return result.toString();
    }

    private record Spec(String text, String flags, String width, String precision, char conversion, String time) {
        /** The conversion with only the given flags. */
        String with(String allowedFlags, String conversionText) {
            StringBuilder spec = new StringBuilder("%");
            for (char flag : flags.toCharArray()) {
                if (allowedFlags.indexOf(flag) < 0) {
                    throw invalid("The flag '" + flag + "' can't be used with %" + conversion + time);
                }
                spec.append(flag);
            }
            spec.append(width);
            if (!precision.isEmpty()) {
                spec.append('.').append(precision);
            }
            return spec.append(conversionText).toString();
        }

        /** Pads a text to the width (left aligned with the flag '-'). */
        String pad(String text) {
            if (width.isEmpty() || text.length() >= Integer.parseInt(width)) {
                return text;
            }
            String padding = " ".repeat(Integer.parseInt(width) - text.length());
            return flags.contains("-") ? text + padding : padding + text;
        }
    }

    private static String convert(Spec spec, Value value, ZoneId zone) {
        try {
            return switch (spec.conversion()) {
                case '%' -> spec.pad("%");
                case 'n' -> "\n";
                case 'd', 'x', 'X', 'o' -> {
                    long number = requireInteger(spec, value);
                    String flags = spec.conversion() == 'd' ? "-0+ ," : "-0";
                    if (!spec.precision().isEmpty()) {
                        throw invalid("%" + spec.conversion() + " has no precision");
                    }
                    yield String.format(Locale.ROOT, spec.with(flags, String.valueOf(spec.conversion())), number);
                }
                case 'f', 'e' ->
                    String.format(
                            Locale.ROOT,
                            spec.with("-0+ ,", String.valueOf(spec.conversion())),
                            requireNumber(spec, value));
                case 'g' -> {
                    // %f or %e, whichever is shorter; the width is applied afterwards (padding with spaces)
                    double number = requireNumber(spec, value);
                    spec.with("-0+ ,", "g");
                    String flags = without(spec.flags(), "-0");
                    String precision = spec.precision().isEmpty() ? "" : "." + spec.precision();
                    String fixed = String.format(Locale.ROOT, "%" + flags + precision + "f", number);
                    String scientific = String.format(Locale.ROOT, "%" + without(flags, ",") + precision + "e", number);
                    yield spec.pad(fixed.length() <= scientific.length() ? fixed : scientific);
                }
                case 'c' -> {
                    if (!(value instanceof CharValue c)) {
                        throw mismatch(spec, value);
                    }
                    yield String.format(Locale.ROOT, spec.with("-", "c"), c.codePoint());
                }
                case 'b' -> {
                    if (!(value instanceof BooleanValue b)) {
                        throw mismatch(spec, value);
                    }
                    yield String.format(Locale.ROOT, spec.with("-", "b"), b.value());
                }
                case 's' -> String.format(Locale.ROOT, spec.with("-", "s"), value.text());
                case 't', 'T' -> time(spec, value, zone);
                default -> throw invalid("Unknown conversion " + spec.text());
            };
        } catch (IllegalFormatException e) {
            throw invalid("Invalid conversion " + spec.text() + ": " + e.getMessage());
        }
    }

    private static String time(Spec spec, Value value, ZoneId zone) {
        if (!(value instanceof TimeValue time)) {
            throw mismatch(spec, value);
        }
        if (!spec.precision().isEmpty()) {
            throw invalid("Time conversions have no precision: " + spec.text());
        }
        ZonedDateTime dateTime = Instant.ofEpochMilli(time.millis()).atZone(zone);
        return switch (spec.time()) {
            case "u" -> spec.pad(Integer.toString(dateTime.getDayOfWeek().getValue()));
            case "V" -> spec.pad(String.format(Locale.ROOT, "%02d", dateTime.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)));
            case "Y",
                    "y",
                    "m",
                    "B",
                    "b",
                    "d",
                    "e",
                    "j",
                    "A",
                    "a",
                    "H",
                    "I",
                    "p",
                    "M",
                    "S",
                    "L",
                    "Z",
                    "z",
                    "s",
                    "Q",
                    "F",
                    "T",
                    "R" -> String.format(Locale.ENGLISH, spec.with("-", "t" + spec.time()), dateTime);
            default -> throw invalid("Unknown time conversion " + spec.text());
        };
    }

    private static String without(String flags, String removed) {
        StringBuilder result = new StringBuilder();
        for (char flag : flags.toCharArray()) {
            if (removed.indexOf(flag) < 0) {
                result.append(flag);
            }
        }
        return result.toString();
    }

    private static long requireInteger(Spec spec, Value value) {
        if (!(value instanceof IntegerValue integer)) {
            throw mismatch(spec, value);
        }
        return integer.value();
    }

    private static double requireNumber(Spec spec, Value value) {
        if (!Values.isNumber(value)) {
            throw mismatch(spec, value);
        }
        return Values.toDouble(value);
    }

    private static C0deException mismatch(Spec spec, Value value) {
        return C0deException.of(
                ErrorCode.TYPE_MISMATCH, "The conversion " + spec.text() + " can't format a " + value.type());
    }
}
