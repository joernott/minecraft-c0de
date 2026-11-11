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

    /** The value as a tree of maps, lists, strings, numbers and booleans, as TO_YAML writes it (program runner). */
    public static Object tree(Value value) {
        return DataFormats.toTree(value, NO_TAGS);
    }

    /**
     * Reads a value of a type from a tree, as FROM_YAML does (program runner).
     *
     * @throws de.joernott.c0de.core.error.C0deException TYPE_MISMATCH if the tree doesn't fit the type
     */
    public static Value read(Object tree, Type type) {
        return DataFormats.read(tree, type.defaultValue(), type, NO_TAGS);
    }

    /**
     * Parses YAML (or JSON) into maps, lists, strings, numbers, booleans and null.
     *
     * @throws de.joernott.c0de.core.error.C0deException INVALID_ARGUMENT for invalid YAML
     */
    public static Object parseYaml(String yaml) {
        return DataFormats.parse(yaml, DataFormats.YAML);
    }

    /** A tree as YAML in block style. */
    public static String writeYaml(Object tree) {
        return DataFormats.writeYaml(tree);
    }
}
