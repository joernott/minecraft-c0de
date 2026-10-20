package de.joernott.c0de.core.program;

import de.joernott.c0de.core.value.Value;
import java.util.List;

/**
 * The code block that initializes an ARRAY, HASH or STRUCT (see docs/concept/04-data.md, "Initializing
 * complex values"). Whether the entries fit the type is checked when the declaration is executed.
 *
 * @param beginLine z coordinate of the BEGIN line
 * @param entries the content lines
 * @param endLine z coordinate of the END line
 */
public record Initializer(int beginLine, List<Entry> entries, int endLine) {
    public Initializer {
        entries = List.copyOf(entries);
    }

    /** A content line of an initializer. */
    public sealed interface Entry {
        int line();
    }

    /** A line of an array initializer with one or more literals. */
    public record Values(int line, List<Value> values) implements Entry {
        public Values {
            values = List.copyOf(values);
        }
    }

    /** A nested array element: a line with only a BEGIN block, the content and a line with only END. */
    public record Nested(int line, Initializer content) implements Entry {}

    /** A line of a hash or struct initializer: key or element name and a literal. */
    public record Keyed(int line, String key, Value value) implements Entry {}

    /** A nested hash or struct element: a line with only the key, followed by a nested code block. */
    public record KeyedNested(int line, String key, Initializer content) implements Entry {}
}
