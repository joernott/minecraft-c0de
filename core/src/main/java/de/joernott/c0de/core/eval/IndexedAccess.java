package de.joernott.c0de.core.eval;

import de.joernott.c0de.core.error.C0deException;
import de.joernott.c0de.core.error.ErrorCode;
import de.joernott.c0de.core.line.IndexEntry;
import de.joernott.c0de.core.program.LabelRef;
import de.joernott.c0de.core.value.ArrayValue;
import de.joernott.c0de.core.value.CharValue;
import de.joernott.c0de.core.value.HashValue;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.StringValue;
import de.joernott.c0de.core.value.StructDefinition;
import de.joernott.c0de.core.value.StructValue;
import de.joernott.c0de.core.value.Type;
import de.joernott.c0de.core.value.Value;
import de.joernott.c0de.core.value.Values;
import java.util.List;

/**
 * Reading and writing variables through labels with an index, e.g. {@code matrix[i][2]},
 * {@code inventory["diamond"]} or {@code error.code} (see docs/concept/02-language-basics.md).
 */
public final class IndexedAccess {
    /** Maximum number of index entries of a label. */
    public static final int MAX_INDEX = 8;

    private IndexedAccess() {}

    /**
     * The value a label refers to. Arrays, hashes and structs are returned without copying.
     *
     * @throws C0deException UNKNOWN_NAME, INDEX_OUT_OF_BOUNDS, UNKNOWN_KEY, UNKNOWN_STRUCT_ENTITY or
     *     TYPE_MISMATCH
     */
    public static Value read(LabelRef label, Scope scope) {
        checkIndexLength(label);
        Value value = scope.resolve(label.name()).value();
        for (IndexEntry entry : label.index()) {
            value = element(value, key(entry, scope));
        }
        return value;
    }

    /**
     * Assigns a value to the variable or element a label refers to, following the rules of the ASSIGN
     * statement. Assigning to the index of an array that equals its length appends an element; assigning to
     * a missing key of a hash adds it. Strings can't be changed through an index.
     *
     * @throws C0deException TYPE_MISMATCH, STRING_TOO_LONG, INDEX_OUT_OF_BOUNDS, UNKNOWN_STRUCT_ENTITY or
     *     UNKNOWN_NAME. Constants are not checked here.
     */
    public static void write(LabelRef label, Scope scope, Value newValue) {
        checkIndexLength(label);
        Variable variable = scope.resolve(label.name());
        if (label.index().isEmpty()) {
            variable.set(newValue);
            return;
        }
        // Evaluate the keys first, so that an error leaves the variable unchanged
        List<Object> keys =
                label.index().stream().map(entry -> key(entry, scope)).toList();
        Value container = variable.value();
        for (int i = 0; i < keys.size() - 1; i++) {
            container = element(container, keys.get(i));
        }
        store(container, keys.get(keys.size() - 1), newValue);
    }

    private static void checkIndexLength(LabelRef label) {
        if (label.index().size() > MAX_INDEX) {
            throw C0deException.of(ErrorCode.TYPE_MISMATCH, "A label can have at most " + MAX_INDEX + " index entries");
        }
    }

    /** The key of an index entry: a Long position, a String key or an {@link ElementName}. */
    private static Object key(IndexEntry entry, Scope scope) {
        return switch (entry) {
            case IndexEntry.Element element -> new ElementName(element.name());
            case IndexEntry.Literal literal -> keyOf(literal.value());
            case IndexEntry.Variable variable ->
                keyOf(scope.resolve(variable.name()).value());
        };
    }

    private static Object keyOf(Value value) {
        return switch (value) {
            case IntegerValue integer -> integer.value();
            case StringValue string -> string.value();
            case CharValue c -> c.text();
            default ->
                throw C0deException.of(
                        ErrorCode.TYPE_MISMATCH, "An index must be an INTEGER or a STRING, not a " + value.type());
        };
    }

    /** The name of a struct element, written as {@code .name}. */
    private record ElementName(String name) {}

    private static Value element(Value container, Object key) {
        return switch (container) {
            case ArrayValue array -> {
                int index = position(key, array.elements().size(), false);
                yield array.elements().get(index);
            }
            case HashValue hash -> {
                String name = hashKey(key);
                Value value = hash.elements().get(name);
                if (value == null) {
                    throw C0deException.of(ErrorCode.UNKNOWN_KEY, "The hash has no key \"" + name + "\"");
                }
                yield value;
            }
            case StructValue struct -> struct.get(elementName(struct, key));
            case StringValue string -> {
                int index = position(key, string.length(), false);
                yield new CharValue(string.value().codePointAt(string.value().offsetByCodePoints(0, index)));
            }
            default ->
                throw C0deException.of(ErrorCode.TYPE_MISMATCH, "A " + container.type() + " can't have an index");
        };
    }

    private static void store(Value container, Object key, Value newValue) {
        switch (container) {
            case ArrayValue array -> {
                int index = position(key, array.elements().size(), true);
                Value converted = Values.convert(newValue, array.elementType());
                if (index == array.elements().size()) {
                    array.elements().add(converted);
                } else {
                    array.elements().set(index, converted);
                }
            }
            case HashValue hash -> hash.elements().put(hashKey(key), Values.convert(newValue, hash.elementType()));
            case StructValue struct -> {
                String name = elementName(struct, key);
                Type type = struct.definition().element(name).orElseThrow().type();
                struct.set(name, Values.convert(newValue, type));
            }
            case StringValue string ->
                throw C0deException.of(
                        ErrorCode.TYPE_MISMATCH, "The characters of a STRING can't be changed through an index");
            default ->
                throw C0deException.of(ErrorCode.TYPE_MISMATCH, "A " + container.type() + " can't have an index");
        }
    }

    private static int position(Object key, int size, boolean append) {
        if (!(key instanceof Long position)) {
            throw C0deException.of(ErrorCode.TYPE_MISMATCH, "The index of an array or string must be an INTEGER");
        }
        long limit = append ? size : size - 1;
        if (position < 0 || position > limit) {
            throw C0deException.of(
                    ErrorCode.INDEX_OUT_OF_BOUNDS, "The index " + position + " is outside of 0 to " + limit);
        }
        return position.intValue();
    }

    private static String hashKey(Object key) {
        return switch (key) {
            case String string -> string;
            case ElementName element -> element.name();
            default -> throw C0deException.of(ErrorCode.TYPE_MISMATCH, "The key of a hash must be a STRING");
        };
    }

    private static String elementName(StructValue struct, Object key) {
        String name = switch (key) {
            case String string -> string;
            case ElementName element -> element.name();
            default ->
                throw C0deException.of(ErrorCode.TYPE_MISMATCH, "The element of a struct is selected by its name");
        };
        StructDefinition definition = struct.definition();
        if (definition.element(name).isEmpty()) {
            throw C0deException.of(
                    ErrorCode.UNKNOWN_STRUCT_ENTITY, "The struct " + definition.name() + " has no element " + name);
        }
        return name;
    }
}
