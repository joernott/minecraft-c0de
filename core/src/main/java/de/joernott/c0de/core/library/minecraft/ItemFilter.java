package de.joernott.c0de.core.library.minecraft;

import de.joernott.c0de.core.error.C0deException;
import de.joernott.c0de.core.error.ErrorCode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * The filter of MOVE_ITEMS (docs/concept/12-lib-minecraft.md#filter): item IDs and search expressions like in the
 * search field of JEI. Words separated by spaces must all match, {@code |} separates alternatives; a word without
 * prefix searches the name, {@code @} the mod, {@code $} the tags, {@code &} the ID, and {@code -} negates a word.
 */
public final class ItemFilter implements Predicate<ItemFilter.Item> {
    /** What the filter knows of an item. */
    public interface Item {
        /** The ID, e.g. {@code minecraft:oak_log}. */
        String id();

        /** The name players see (the custom name or the English name). */
        String name();

        /** The IDs of its tags, e.g. {@code minecraft:logs}. */
        Collection<String> tags();
    }

    private static final Pattern ID = Pattern.compile("[a-z0-9_.-]+:[a-z0-9_./-]+");

    /** A filter that matches every item. */
    public static final ItemFilter ALL = new ItemFilter(List.of());

    private enum Field {
        NAME,
        MOD,
        TAG,
        ID,
        EXACT_ID
    }

    private record Word(Field field, String text, boolean negated) {
        boolean test(Item item) {
            boolean matches = switch (field) {
                case NAME -> lower(item.name()).contains(text);
                case MOD -> namespace(item.id()).contains(text);
                case TAG -> item.tags().stream().anyMatch(tag -> lower(tag).contains(text));
                case ID -> lower(item.id()).contains(text);
                case EXACT_ID -> item.id().equals(text);
            };
            return matches != negated;
        }
    }

    /** Alternatives, each a list of words that must all match; no alternatives match every item. */
    private final List<List<Word>> alternatives;

    private ItemFilter(List<List<Word>> alternatives) {
        this.alternatives = alternatives;
    }

    /**
     * The filter of the strings; an item matches if one of them matches. No strings, or an empty one, match every
     * item.
     *
     * @param isItem tells whether an ID is an item; an exact ID that isn't raises INVALID_ARGUMENT
     */
    public static ItemFilter of(List<String> filters, Predicate<String> isItem) {
        List<List<Word>> alternatives = new ArrayList<>();
        for (String filter : filters) {
            String trimmed = filter.trim();
            if (trimmed.isEmpty()) {
                return ALL;
            }
            if (ID.matcher(trimmed).matches()) {
                if (!isItem.test(trimmed)) {
                    throw invalid("There is no item " + trimmed);
                }
                alternatives.add(List.of(new Word(Field.EXACT_ID, trimmed, false)));
            } else {
                for (String alternative : split(trimmed)) {
                    alternatives.add(words(alternative, filter));
                }
            }
        }
        return new ItemFilter(List.copyOf(alternatives));
    }

    @Override
    public boolean test(Item item) {
        if (alternatives.isEmpty()) {
            return true;
        }
        for (List<Word> words : alternatives) {
            if (words.stream().allMatch(word -> word.test(item))) {
                return true;
            }
        }
        return false;
    }

    /** The alternatives of an expression: split at {@code |} outside of quotes. */
    private static List<String> split(String expression) {
        List<String> parts = new ArrayList<>();
        StringBuilder part = new StringBuilder();
        boolean quoted = false;
        for (char c : expression.toCharArray()) {
            if (c == '"') {
                quoted = !quoted;
            }
            if (c == '|' && !quoted) {
                parts.add(part.toString());
                part.setLength(0);
            } else {
                part.append(c);
            }
        }
        if (quoted) {
            throw invalid("The filter has a quote without its end: " + expression);
        }
        parts.add(part.toString());
        return parts;
    }

    /** The words of an alternative, separated by spaces; text in quotes is one word. */
    private static List<Word> words(String alternative, String filter) {
        List<String> texts = new ArrayList<>();
        StringBuilder text = new StringBuilder();
        boolean quoted = false;
        boolean started = false;
        for (char c : alternative.toCharArray()) {
            if (c == '"') {
                quoted = !quoted;
                started = true;
            } else if (Character.isWhitespace(c) && !quoted) {
                if (started) {
                    texts.add(text.toString());
                }
                text.setLength(0);
                started = false;
            } else {
                text.append(c);
                started = true;
            }
        }
        if (started) {
            texts.add(text.toString());
        }
        if (texts.isEmpty()) {
            throw invalid("The filter has an empty alternative: " + filter);
        }
        List<Word> words = new ArrayList<>();
        for (String word : texts) {
            words.add(word(word, filter));
        }
        return List.copyOf(words);
    }

    private static Word word(String word, String filter) {
        boolean negated = word.length() > 1 && word.startsWith("-");
        String rest = negated ? word.substring(1) : word;
        Field field = Field.NAME;
        if (!rest.isEmpty()) {
            switch (rest.charAt(0)) {
                case '@' -> field = Field.MOD;
                case '$' -> field = Field.TAG;
                case '&' -> field = Field.ID;
                case '#', '%', '^' ->
                    throw invalid("The filter word " + word + " needs the client: tooltips (#), creative tabs (%)"
                            + " and colors (^) can't be searched");
                default -> {}
            }
        }
        String text = lower(field == Field.NAME ? rest : rest.substring(1));
        if (text.isEmpty()) {
            throw invalid("The filter has an empty word: " + filter);
        }
        return new Word(field, text, negated);
    }

    private static String lower(String text) {
        return text.toLowerCase(Locale.ROOT);
    }

    private static String namespace(String id) {
        int colon = id.indexOf(':');
        return lower(colon < 0 ? "minecraft" : id.substring(0, colon));
    }

    private static C0deException invalid(String message) {
        return C0deException.of(ErrorCode.INVALID_ARGUMENT, message);
    }
}
