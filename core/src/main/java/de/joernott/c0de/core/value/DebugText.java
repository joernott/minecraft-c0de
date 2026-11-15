package de.joernott.c0de.core.value;

import de.joernott.c0de.core.program.Tags;
import java.util.stream.Collectors;

/**
 * The text of a value in the debug output (docs/concept/20-lib-security.md): like {@link Value#text()}, but
 * sensitive values are shown as {@link #HIDDEN}: a whole value, or the elements of structs whose declaration has
 * the tag {@code security: sensitive}, also inside arrays, hashes and other structs.
 */
public final class DebugText {
    /** What the debug output shows instead of a sensitive value. */
    public static final String HIDDEN = "***";

    private DebugText() {}

    /** The text of a value; {@link #HIDDEN} if the value is sensitive. */
    public static String of(Value value, boolean sensitive) {
        return sensitive ? HIDDEN : of(value);
    }

    /** The text of a value with its sensitive struct elements hidden. */
    public static String of(Value value) {
        return switch (value) {
            case ArrayValue array ->
                array.elements().stream().map(DebugText::of).collect(Collectors.joining(", ", "[", "]"));
            case HashValue hash ->
                hash.elements().entrySet().stream()
                        .map(e -> e.getKey() + ": " + of(e.getValue()))
                        .collect(Collectors.joining(", ", "{", "}"));
            case StructValue struct ->
                struct.definition().elements().stream()
                        .map(e -> e.name() + ": " + of(struct.get(e.name()), Tags.sensitive(e.tags())))
                        .collect(Collectors.joining(", ", "{", "}"));
            default -> value.text();
        };
    }
}
