package de.joernott.c0de.core.edit;

import de.joernott.c0de.core.line.Block;
import de.joernott.c0de.core.line.IndexEntry;
import de.joernott.c0de.core.line.IndexText;
import de.joernott.c0de.core.line.Keyword;
import de.joernott.c0de.core.program.Names;
import de.joernott.c0de.core.value.Literals;
import de.joernott.c0de.core.value.Type;
import de.joernott.c0de.core.value.Value;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * The rules for the properties that are entered in the dialogs of LABEL, LITERAL, COMMENT and TAG blocks.
 * Each check returns a problem in English, or an empty optional if the input is valid. The dialog only
 * enables its OK button for valid input, and the server checks the input again before it changes a block.
 */
public final class PropertyRules {
    /** The maximum number of index entries of a label. */
    public static final int MAX_INDEX_ENTRIES = 8;

    /** The maximum length of names, values and comments in characters. */
    public static final int MAX_TEXT_LENGTH = 1000;

    /** The type names of LITERAL blocks in the order of the dialog. */
    public static final List<String> LITERAL_TYPES = List.of("BOOLEAN", "INTEGER", "FLOAT", "CHAR", "STRING", "TIME");

    /** The languages of TAG blocks. */
    public static final List<String> TAG_LANGUAGES = List.of("json", "yaml");

    private PropertyRules() {}

    /** Checks the name and the index text of a label in a position with the given dialog layout. */
    public static Optional<String> label(LabelMode mode, String name, String indexText) {
        if (name == null || name.isEmpty()) {
            return Optional.of("The name is missing");
        }
        if (name.length() > MAX_TEXT_LENGTH) {
            return Optional.of("The name is too long");
        }
        boolean valid = switch (mode) {
            case NEW_NAME, CALL_PARAMETER, CALL_RESULT, TARGET -> Names.isValid(name);
            // Struct types of libraries have qualified names, e.g. c0de/minecraft/POSITION
            case STRUCT_TYPE -> Names.isValidQualified(name);
            case LIBRARY -> Names.isValidLibrary(name);
            case FUNCTION, REFERENCE -> Names.isValidQualified(name);
        };
        if (!valid) {
            return Optional.of(
                    Keyword.isReserved(name)
                            ? name + " is a keyword"
                            : "A name consists of letters, digits and underscores and starts with a letter");
        }
        if (indexText == null || indexText.isEmpty()) {
            return Optional.empty();
        }
        if (!mode.allowsIndex()) {
            return Optional.of("A label in this position has no index");
        }
        return index(indexText).problem();
    }

    /** The result of parsing the index text of a label: the entries or a problem. */
    public record IndexResult(List<IndexEntry> entries, Optional<String> problem) {}

    /** Parses and checks the index text of a label. */
    public static IndexResult index(String text) {
        List<IndexEntry> entries;
        try {
            entries = IndexText.parse(text == null ? "" : text);
        } catch (IllegalArgumentException e) {
            return new IndexResult(List.of(), Optional.of(e.getMessage()));
        }
        if (entries.size() > MAX_INDEX_ENTRIES) {
            return new IndexResult(List.of(), Optional.of("An index has at most " + MAX_INDEX_ENTRIES + " entries"));
        }
        for (IndexEntry entry : entries) {
            String name = switch (entry) {
                case IndexEntry.Variable variable -> variable.name();
                case IndexEntry.Element element -> element.name();
                case IndexEntry.Literal literal -> null;
            };
            if (name != null && !Names.isValid(name)) {
                return new IndexResult(List.of(), Optional.of("Not a valid name in the index: " + name));
            }
        }
        return new IndexResult(entries, Optional.empty());
    }

    /**
     * Splits a function name chosen in the dialog into parent and name: {@code c0de/math/SIN} has the parent
     * {@code c0de/math}, a built-in function the empty parent and a function of the program the program name.
     *
     * @return the parent and the name
     */
    public static List<String> splitFunction(String qualified, Set<String> builtins, String programName) {
        int slash = qualified.lastIndexOf('/');
        if (slash >= 0) {
            return List.of(qualified.substring(0, slash), qualified.substring(slash + 1));
        }
        return List.of(builtins.contains(qualified) ? "" : programName, qualified);
    }

    /** The type of a LITERAL by its name in {@link #LITERAL_TYPES}. */
    public static Optional<Type> literalType(String name) {
        int i = LITERAL_TYPES.indexOf(name);
        return i < 0 ? Optional.empty() : Optional.of(Literals.TYPES.get(i));
    }

    /** Parses the value of a LITERAL. */
    public static Optional<Value> literalValue(String typeName, String text, ZoneId zone) {
        try {
            return literalType(typeName).map(type -> Literals.parse(type, text, zone));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /** Checks the type and value of a LITERAL. */
    public static Optional<String> literal(String typeName, String text, ZoneId zone) {
        Optional<Type> type = literalType(typeName);
        if (type.isEmpty()) {
            return Optional.of("A literal has one of the types " + String.join(", ", LITERAL_TYPES));
        }
        if (text == null || text.length() > MAX_TEXT_LENGTH) {
            return Optional.of("The value is too long");
        }
        try {
            Literals.parse(type.get(), text, zone);
            return Optional.empty();
        } catch (IllegalArgumentException e) {
            return Optional.of(e.getMessage());
        }
    }

    /** Checks the text of a COMMENT. It may be empty. */
    public static Optional<String> comment(String text) {
        return text != null && text.length() <= MAX_TEXT_LENGTH
                ? Optional.empty()
                : Optional.of("A comment has at most " + MAX_TEXT_LENGTH + " characters");
    }

    /**
     * True if a TAG at the position x, z belongs to a VARIABLE or CONSTANT line, whose tags are paths. Tags in
     * other lines belong to struct elements.
     */
    public static boolean isPathTag(SheetView sheet, int x, int z) {
        return sheet.firstBlock(z) instanceof Block.KeywordBlock(Keyword keyword)
                && (keyword == Keyword.VARIABLE || keyword == Keyword.CONSTANT);
    }

    /**
     * Checks the language and name of a TAG. The tag of a variable or constant is a path of keys separated by
     * dots; the tag of a struct element is a single key without a dot, or {@code -} to skip the element.
     */
    public static Optional<String> tag(String language, String name, boolean path) {
        if (!TAG_LANGUAGES.contains(language)) {
            return Optional.of("The language of a tag is json or yaml");
        }
        if (name == null || name.isEmpty()) {
            return Optional.of("The name is missing");
        }
        if (name.length() > MAX_TEXT_LENGTH) {
            return Optional.of("The name is too long");
        }
        if (path) {
            for (String key : name.split("\\.", -1)) {
                if (key.isEmpty()) {
                    return Optional.of("A key of the path is empty");
                }
            }
        } else if (name.contains(".")) {
            return Optional.of("The tag of a struct element contains no dot");
        }
        return Optional.empty();
    }
}
