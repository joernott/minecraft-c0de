package de.joernott.c0de.core.library.standard;

import static de.joernott.c0de.core.library.standard.Functions.invalid;

import de.joernott.c0de.core.error.C0deException;
import de.joernott.c0de.core.error.ErrorCode;
import de.joernott.c0de.core.value.ArrayValue;
import de.joernott.c0de.core.value.BooleanValue;
import de.joernott.c0de.core.value.CharValue;
import de.joernott.c0de.core.value.FloatValue;
import de.joernott.c0de.core.value.HashValue;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.Literals;
import de.joernott.c0de.core.value.StringValue;
import de.joernott.c0de.core.value.StructDefinition;
import de.joernott.c0de.core.value.StructValue;
import de.joernott.c0de.core.value.TimeValue;
import de.joernott.c0de.core.value.Type;
import de.joernott.c0de.core.value.Value;
import de.joernott.c0de.core.value.Values;
import java.math.BigInteger;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.exceptions.YamlEngineException;

/**
 * Conversion of values to and from JSON and YAML (docs/concept/10-lib-strings.md, "JSON and YAML functions").
 * Values are converted to a tree of maps, lists and scalars first; JSON is written by this class, YAML by
 * SnakeYAML Engine, which also parses both languages (YAML 1.2 is a superset of JSON).
 */
final class DataFormats {
    static final String JSON = "json";
    static final String YAML = "yaml";

    private DataFormats() {}

    // -------------------------------------------------------------------------------------------- writing

    /**
     * The tree of a value. Struct elements are named by their tags for the language; elements with the tag
     * name {@code -} are left out.
     */
    static Object toTree(Value value, String language) {
        return switch (value) {
            case BooleanValue b -> b.value();
            case IntegerValue i -> i.value();
            case FloatValue f -> {
                if (Double.isNaN(f.value()) || Double.isInfinite(f.value())) {
                    throw invalid("NaN and infinite numbers can't be written as "
                            + language.toUpperCase(java.util.Locale.ROOT));
                }
                yield f.value();
            }
            case CharValue c -> c.text();
            case TimeValue t -> t.text();
            case StringValue s -> s.value();
            case ArrayValue array -> {
                List<Object> list = new ArrayList<>();
                for (Value element : array.elements()) {
                    list.add(toTree(element, language));
                }
                yield list;
            }
            case HashValue hash -> {
                Map<String, Object> map = new LinkedHashMap<>();
                hash.elements().forEach((key, element) -> map.put(key, toTree(element, language)));
                yield map;
            }
            case StructValue struct -> {
                Map<String, Object> map = new LinkedHashMap<>();
                for (StructDefinition.Element element : struct.definition().elements()) {
                    String key = key(element, language);
                    if (key != null) {
                        map.put(key, toTree(struct.get(element.name()), language));
                    }
                }
                yield map;
            }
        };
    }

    /** The key of a struct element in a language, or null if the element is skipped (tag name "-"). */
    private static String key(StructDefinition.Element element, String language) {
        String tag = element.tags().get(language);
        if (tag == null) {
            return element.name();
        }
        return tag.equals("-") ? null : tag;
    }

    /**
     * Puts a tree at the path of a variable tag, e.g. {@code server.port}: keys create maps, numeric keys
     * create lists whose earlier elements are null.
     */
    static Object wrap(Object tree, String path) {
        Object result = tree;
        String[] keys = path.split("\\.");
        for (int i = keys.length - 1; i >= 0; i--) {
            String key = keys[i];
            if (key.chars().allMatch(Character::isDigit)) {
                List<Object> list = new ArrayList<>();
                int index = Integer.parseInt(key);
                for (int j = 0; j < index; j++) {
                    list.add(null);
                }
                list.add(result);
                result = list;
            } else {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put(key, result);
                result = map;
            }
        }
        return result;
    }

    /** A tree as JSON; with indent 0 in a single line, otherwise indented by that many spaces. */
    static String writeJson(Object tree, int indent) {
        StringBuilder out = new StringBuilder();
        writeJson(tree, indent, 0, out);
        return out.toString();
    }

    private static void writeJson(Object tree, int indent, int level, StringBuilder out) {
        switch (tree) {
            case null -> out.append("null");
            case Map<?, ?> map -> {
                if (map.isEmpty()) {
                    out.append("{}");
                    return;
                }
                out.append('{');
                boolean first = true;
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    out.append(first ? "" : ",");
                    first = false;
                    newLine(indent, level + 1, out);
                    string(String.valueOf(entry.getKey()), out);
                    out.append(indent > 0 ? ": " : ":");
                    writeJson(entry.getValue(), indent, level + 1, out);
                }
                newLine(indent, level, out);
                out.append('}');
            }
            case List<?> list -> {
                if (list.isEmpty()) {
                    out.append("[]");
                    return;
                }
                out.append('[');
                boolean first = true;
                for (Object element : list) {
                    out.append(first ? "" : ",");
                    first = false;
                    newLine(indent, level + 1, out);
                    writeJson(element, indent, level + 1, out);
                }
                newLine(indent, level, out);
                out.append(']');
            }
            case String s -> string(s, out);
            default -> out.append(tree);
        }
    }

    private static void newLine(int indent, int level, StringBuilder out) {
        if (indent > 0) {
            out.append('\n').append(" ".repeat(indent * level));
        }
    }

    private static void string(String text, StringBuilder out) {
        out.append('"');
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                case '\b' -> out.append("\\b");
                case '\f' -> out.append("\\f");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        out.append('"');
    }

    /** A tree as YAML in block style with an indentation of two spaces. */
    static String writeYaml(Object tree) {
        DumpSettings settings = DumpSettings.builder()
                .setDefaultFlowStyle(FlowStyle.BLOCK)
                .setIndent(2)
                .build();
        return new Dump(settings).dumpToString(tree);
    }

    // -------------------------------------------------------------------------------------------- reading

    /**
     * Parses the first document of a JSON or YAML text.
     *
     * @throws C0deException INVALID_ARGUMENT if the text is malformed
     */
    static Object parse(String text, String language) {
        try {
            Iterator<Object> documents = new Load(LoadSettings.builder().build())
                    .loadAllFromString(text)
                    .iterator();
            return documents.hasNext() ? documents.next() : null;
        } catch (YamlEngineException e) {
            throw invalid("The text is not valid " + language.toUpperCase(java.util.Locale.ROOT) + ": "
                    + firstLine(e.getMessage()));
        }
    }

    private static String firstLine(String message) {
        return message == null ? "" : message.lines().findFirst().orElse("");
    }

    /**
     * The part of a tree at the path of a variable tag.
     *
     * @throws C0deException UNKNOWN_KEY if there is no value at the path
     */
    static Object select(Object tree, String path) {
        Object node = tree;
        for (String key : path.split("\\.")) {
            if (node instanceof Map<?, ?> map && map.containsKey(key)) {
                node = map.get(key);
            } else if (node instanceof List<?> list
                    && key.chars().allMatch(Character::isDigit)
                    && key.length() < 10
                    && Integer.parseInt(key) < list.size()) {
                node = list.get(Integer.parseInt(key));
            } else {
                throw C0deException.of(ErrorCode.UNKNOWN_KEY, "The document has no value at " + path);
            }
        }
        return node;
    }

    /**
     * Converts a tree to a value of a type, starting from the current value: elements of structs that are
     * missing or null keep their current values, arrays and hashes are replaced completely.
     *
     * @throws C0deException TYPE_MISMATCH if the tree doesn't fit the type
     */
    static Value read(Object node, Value current, Type type, String language) {
        if (node == null) {
            return current;
        }
        return switch (type) {
            case Type.Simple simple -> simple(node, simple);
            case Type.Str str -> {
                if (!(node instanceof String s)) {
                    throw mismatch(node, type);
                }
                yield Values.convert(new StringValue(s), str);
            }
            case Type.Array array -> {
                if (!(node instanceof List<?> list)) {
                    throw mismatch(node, type);
                }
                ArrayValue result = new ArrayValue(array.element());
                for (Object element : list) {
                    result.elements().add(read(element, array.element().defaultValue(), array.element(), language));
                }
                yield result;
            }
            case Type.Hash hash -> {
                if (!(node instanceof Map<?, ?> map)) {
                    throw mismatch(node, type);
                }
                HashValue result = new HashValue(hash.element());
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    result.elements()
                            .put(
                                    String.valueOf(entry.getKey()),
                                    read(entry.getValue(), hash.element().defaultValue(), hash.element(), language));
                }
                yield result;
            }
            case Type.Struct struct -> {
                if (!(node instanceof Map<?, ?> map)) {
                    throw mismatch(node, type);
                }
                StructValue result =
                        current instanceof StructValue s && s.definition().equals(struct.definition())
                                ? s.copy()
                                : new StructValue(struct.definition());
                for (StructDefinition.Element element : struct.definition().elements()) {
                    String key = key(element, language);
                    if (key != null && map.get(key) != null) {
                        Value value = read(map.get(key), result.get(element.name()), element.type(), language);
                        result.set(element.name(), Values.convert(value, element.type()));
                    }
                }
                yield result;
            }
        };
    }

    private static Value simple(Object node, Type.Simple type) {
        return switch (type) {
            case BOOLEAN -> {
                if (!(node instanceof Boolean b)) {
                    throw mismatch(node, type);
                }
                yield BooleanValue.of(b);
            }
            case INTEGER -> new IntegerValue(integer(node, type));
            case FLOAT -> {
                if (!(node instanceof Number number)) {
                    throw mismatch(node, type);
                }
                yield new FloatValue(number.doubleValue());
            }
            case CHAR -> {
                if (!(node instanceof String s) || s.codePointCount(0, s.length()) != 1) {
                    throw mismatch(node, type);
                }
                yield new CharValue(s.codePointAt(0));
            }
            case TIME -> {
                if (node instanceof String s) {
                    try {
                        yield new TimeValue(Literals.parseTime(s, ZoneOffset.UTC));
                    } catch (IllegalArgumentException e) {
                        throw mismatch(node, type);
                    }
                }
                yield new TimeValue(integer(node, type));
            }
        };
    }

    private static long integer(Object node, Type type) {
        return switch (node) {
            case Integer i -> i;
            case Long l -> l;
            case BigInteger big when big.bitLength() < 64 -> big.longValue();
            default -> throw mismatch(node, type);
        };
    }

    private static C0deException mismatch(Object node, Type type) {
        String found = switch (node) {
            case Map<?, ?> map -> "an object";
            case List<?> list -> "an array";
            case String s -> "the text \"" + s + "\"";
            default -> String.valueOf(node);
        };
        return C0deException.of(
                ErrorCode.TYPE_MISMATCH, "The document contains " + found + " where a " + type + " is expected");
    }
}
