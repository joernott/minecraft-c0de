package de.joernott.c0de.core.library.standard;

import de.joernott.c0de.core.value.Type;
import de.joernott.c0de.core.value.Value;

/**
 * Values as JSON, with the same conversions as TO_JSON and FROM_JSON but without tags. Variable and constant
 * items store their values in this form.
 */
public final class JsonValues {
    /** A language without tags, so that struct elements always use their names. */
    private static final String NO_TAGS = "c0de";

    private JsonValues() {}

    /** The value as compact JSON. */
    public static String write(Value value) {
        return DataFormats.writeJson(DataFormats.toTree(value, NO_TAGS), 0);
    }

    /**
     * Parses JSON into maps, lists, strings, numbers, booleans and null.
     *
     * @throws de.joernott.c0de.core.error.C0deException INVALID_ARGUMENT for invalid JSON
     */
    public static Object parse(String json) {
        return DataFormats.parse(json, DataFormats.JSON);
    }

    /**
     * Reads a value of a type from JSON.
     *
     * @throws de.joernott.c0de.core.error.C0deException INVALID_ARGUMENT for invalid JSON, TYPE_MISMATCH if the
     *     document doesn't fit the type
     */
    public static Value read(String json, Type type) {
        Object tree = DataFormats.parse(json, DataFormats.JSON);
        return DataFormats.read(tree, type.defaultValue(), type, NO_TAGS);
    }
}
