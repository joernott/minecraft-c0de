package de.joernott.c0de.core.line;

import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.StringValue;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The text form of the index of a LABEL, e.g. {@code [i][2]}, {@code ["diamond"]} or {@code .code}. It is
 * used by the notation and by the index editor of the LABEL dialog.
 */
public final class IndexText {
    private static final Pattern INTEGER = Pattern.compile("-?\\d+");
    private static final Pattern NAME = Pattern.compile("[A-Za-z0-9_]+");

    private IndexText() {}

    /**
     * Parses an index. Variable and element names are not checked against the naming rules.
     *
     * @throws IllegalArgumentException if the text is not an index
     */
    public static List<IndexEntry> parse(String text) {
        List<IndexEntry> index = new ArrayList<>();
        int i = 0;
        while (i < text.length()) {
            if (text.charAt(i) == '.') {
                Matcher element = NAME.matcher(text).region(i + 1, text.length());
                if (!element.lookingAt()) {
                    throw new IllegalArgumentException("Missing element name after the dot: " + text);
                }
                index.add(new IndexEntry.Element(element.group()));
                i = element.end();
            } else if (text.charAt(i) == '[') {
                int close = i + 1 < text.length() && text.charAt(i + 1) == '"'
                        ? closingQuote(text, i + 1) + 1
                        : text.indexOf(']', i);
                if (close <= i || close >= text.length() || text.charAt(close) != ']') {
                    throw new IllegalArgumentException("Missing ] in " + text);
                }
                String entry = text.substring(i + 1, close);
                if (entry.startsWith("\"")) {
                    index.add(
                            new IndexEntry.Literal(new StringValue(unescape(entry.substring(1, entry.length() - 1)))));
                } else if (INTEGER.matcher(entry).matches()) {
                    try {
                        index.add(new IndexEntry.Literal(new IntegerValue(Long.parseLong(entry))));
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("The position is too large: " + entry);
                    }
                } else if (NAME.matcher(entry).matches()) {
                    index.add(new IndexEntry.Variable(entry));
                } else {
                    throw new IllegalArgumentException("Not a position, key or variable: [" + entry + "]");
                }
                i = close + 1;
            } else {
                throw new IllegalArgumentException("An index entry starts with [ or a dot: " + text.substring(i));
            }
        }
        return index;
    }

    /** The text form of an index, the empty string for no index. */
    public static String format(List<IndexEntry> index) {
        StringBuilder text = new StringBuilder();
        for (IndexEntry entry : index) {
            switch (entry) {
                case IndexEntry.Element element -> text.append('.').append(element.name());
                case IndexEntry.Variable variable ->
                    text.append('[').append(variable.name()).append(']');
                case IndexEntry.Literal literal ->
                    text.append('[')
                            .append(Notation.formatLiteral(literal.value()))
                            .append(']');
            }
        }
        return text.toString();
    }

    /** The position of the quote that closes the string starting at {@code start}. */
    private static int closingQuote(String text, int start) {
        for (int i = start + 1; i < text.length(); i++) {
            if (text.charAt(i) == '\\') {
                i++;
            } else if (text.charAt(i) == '"') {
                return i;
            }
        }
        throw new IllegalArgumentException("Missing closing quote: " + text);
    }

    private static String unescape(String text) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\\' && i + 1 < text.length()) {
                char next = text.charAt(++i);
                result.append(next == 'n' ? '\n' : next);
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
}
