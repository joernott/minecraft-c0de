package de.joernott.c0de.core.library.standard;

import de.joernott.c0de.core.error.C0deException;
import de.joernott.c0de.core.error.ErrorCode;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * The regular expressions of c0de/strings (REGEX_MATCH, REGEX_REPLACE; docs/concept/10-lib-strings.md): Java
 * patterns, replacements and flags like the command {@code s} of sed, and a limit for patterns that take too long.
 */
final class Regex {
    /** How often a search may read a character of the text before it is stopped. */
    static final long MAX_READS = 10_000_000;

    private Regex() {}

    /** The flags of REGEX_REPLACE. {@code number} is 0 without a number. */
    record Flags(boolean global, long number, boolean ignoreCase, boolean multiline, boolean print) {
        static Flags parse(String text) {
            boolean global = false;
            boolean ignoreCase = false;
            boolean multiline = false;
            boolean print = false;
            long number = 0;
            boolean hasNumber = false;
            int i = 0;
            while (i < text.length()) {
                char c = text.charAt(i);
                if (c >= '0' && c <= '9') {
                    if (hasNumber) {
                        throw Functions.invalid("The flags contain more than one number: " + text);
                    }
                    int start = i;
                    while (i < text.length() && text.charAt(i) >= '0' && text.charAt(i) <= '9') {
                        i++;
                    }
                    try {
                        number = Long.parseLong(text.substring(start, i));
                    } catch (NumberFormatException e) {
                        throw Functions.invalid("The number in the flags is too large: " + text);
                    }
                    if (number < 1) {
                        throw Functions.invalid("The number in the flags must be at least 1: " + text);
                    }
                    hasNumber = true;
                    continue;
                }
                switch (c) {
                    case 'g' -> global = true;
                    case 'i' -> ignoreCase = true;
                    case 'm' -> multiline = true;
                    case 'p' -> print = true;
                    default ->
                        throw Functions.invalid("Unknown flag '" + c + "'; the flags are g, i, m, p and a number");
                }
                i++;
            }
            return new Flags(global, number, ignoreCase, multiline, print);
        }

        int patternFlags() {
            return (ignoreCase ? Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE : 0)
                    | (multiline ? Pattern.MULTILINE : 0);
        }

        /** True if the match with this number (from 1) is replaced. */
        boolean replaces(long match) {
            if (number == 0) {
                return global || match == 1;
            }
            return global ? match >= number : match == number;
        }

        /** True if no later match can be replaced. */
        boolean done(long match) {
            return !global && match >= Math.max(1, number);
        }
    }

    /** The result of REGEX_REPLACE and whether something was replaced. */
    record Replaced(String text, boolean changed) {}

    static Pattern compile(String pattern, int flags) {
        try {
            return Pattern.compile(pattern, flags);
        } catch (PatternSyntaxException e) {
            throw Functions.invalid("The pattern is not a valid regular expression: " + e.getDescription()
                    + (e.getIndex() >= 0 ? " at position " + e.getIndex() : ""));
        }
    }

    /** True if the pattern matches the text or a part of it. */
    static boolean matches(String text, String pattern) {
        Matcher matcher = compile(pattern, 0).matcher(new GuardedText(text));
        try {
            return matcher.find();
        } catch (TooLong e) {
            throw tooLong();
        }
    }

    static Replaced replace(String text, String pattern, String replacement, Flags flags) {
        Pattern compiled = compile(pattern, flags.patternFlags());
        List<Object> parts = parseReplacement(replacement, compiled.matcher("").groupCount());
        Matcher matcher = compiled.matcher(new GuardedText(text));
        StringBuilder result = new StringBuilder();
        int last = 0;
        long match = 0;
        boolean changed = false;
        try {
            while (matcher.find()) {
                match++;
                if (flags.replaces(match)) {
                    result.append(text, last, matcher.start());
                    for (Object part : parts) {
                        if (part instanceof Integer group) {
                            String value = matcher.group(group);
                            if (value != null) {
                                result.append(value);
                            }
                        } else {
                            result.append((String) part);
                        }
                    }
                    last = matcher.end();
                    changed = true;
                    if (result.length() > Functions.MAX_TEXT_LENGTH) {
                        throw tooLongText();
                    }
                }
                if (flags.done(match)) {
                    break;
                }
            }
        } catch (TooLong e) {
            throw tooLong();
        }
        result.append(text, last, text.length());
        if (result.length() > Functions.MAX_TEXT_LENGTH) {
            throw tooLongText();
        }
        return new Replaced(result.toString(), changed);
    }

    /**
     * The parts of a replacement: texts and group numbers (0 for the whole match), as in sed: {@code &}, {@code \1}
     * to {@code \9}, {@code \&}, {@code \\}, {@code \n}, {@code \t}; another character after a backslash stands for
     * itself.
     */
    static List<Object> parseReplacement(String replacement, int groups) {
        List<Object> parts = new ArrayList<>();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < replacement.length(); i++) {
            char c = replacement.charAt(i);
            Integer group = null;
            if (c == '&') {
                group = 0;
            } else if (c == '\\' && i + 1 < replacement.length()) {
                char next = replacement.charAt(++i);
                if (next >= '0' && next <= '9') {
                    group = next - '0';
                    if (group > groups) {
                        throw Functions.invalid("The replacement refers to group " + group + ", but the pattern has "
                                + (groups == 1 ? "1 group" : groups + " groups"));
                    }
                } else {
                    text.append(
                            switch (next) {
                                case 'n' -> '\n';
                                case 't' -> '\t';
                                default -> next;
                            });
                }
            } else {
                text.append(c);
            }
            if (group != null) {
                if (!text.isEmpty()) {
                    parts.add(text.toString());
                    text.setLength(0);
                }
                parts.add(group);
            }
        }
        if (!text.isEmpty()) {
            parts.add(text.toString());
        }
        return parts;
    }

    private static C0deException tooLong() {
        return Functions.invalid("The search took too long: the pattern reads the text more than " + MAX_READS
                + " times. Simplify the pattern, e.g. avoid repeated groups with .* like (.*a){20}");
    }

    private static C0deException tooLongText() {
        return C0deException.of(
                ErrorCode.STRING_TOO_LONG,
                "The result would have more than " + Functions.MAX_TEXT_LENGTH + " characters");
    }

    /** Thrown inside the matcher when a search reads the text too often. */
    private static final class TooLong extends RuntimeException {
        TooLong() {
            super(null, null, false, false);
        }
    }

    /** A text that counts how often its characters are read, shared with its subsequences. */
    private static final class GuardedText implements CharSequence {
        private final String text;
        private final long[] reads;

        GuardedText(String text) {
            this(text, new long[1]);
        }

        private GuardedText(String text, long[] reads) {
            this.text = text;
            this.reads = reads;
        }

        @Override
        public int length() {
            return text.length();
        }

        @Override
        public char charAt(int index) {
            if (++reads[0] > MAX_READS) {
                throw new TooLong();
            }
            return text.charAt(index);
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return new GuardedText(text.substring(start, end), reads);
        }

        @Override
        public String toString() {
            return text;
        }
    }
}
