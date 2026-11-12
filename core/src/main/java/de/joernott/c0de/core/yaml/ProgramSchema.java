package de.joernott.c0de.core.yaml;

import de.joernott.c0de.core.program.Names;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

/**
 * The JSON Schema of the program files (task 15.5): {@code c0de-program.schema.json} next to this class, published
 * with every release, so editors can check hand-written files. Files are checked against it before they are laid out
 * (task 15.6). The check knows the keywords the schema uses ({@link #KEYWORDS}) and reports the first problem with
 * its path in the file, e.g. {@code code[2].name}.
 */
public final class ProgramSchema {
    /** The name of the schema file, in the package of this class and attached to the releases. */
    public static final String FILE = "c0de-program.schema.json";

    /** The keywords of JSON Schema the check knows; the schema must not use others. */
    static final Set<String> KEYWORDS = Set.of(
            "$schema",
            "$id",
            "$defs",
            "$ref",
            "title",
            "description",
            "type",
            "enum",
            "const",
            "pattern",
            "minLength",
            "not",
            "properties",
            "required",
            "additionalProperties",
            "items",
            "allOf",
            "if",
            "then");

    private static final String TEXT = read();
    private static final Map<?, ?> SCHEMA =
            (Map<?, ?>) new Load(LoadSettings.builder().build()).loadFromString(TEXT);

    private ProgramSchema() {}

    /** The schema as published. */
    public static String text() {
        return TEXT;
    }

    private static String read() {
        try (InputStream in =
                Objects.requireNonNull(ProgramSchema.class.getResourceAsStream(FILE), FILE + " is missing")) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Checks a loaded file against the schema.
     *
     * @return the first problem, with its path in the file, or empty if the file matches
     */
    public static Optional<String> check(Object file) {
        return check(file, SCHEMA, "");
    }

    private static Optional<String> check(Object value, Map<?, ?> schema, String path) {
        for (Object keyword : schema.keySet()) {
            if (!KEYWORDS.contains(String.valueOf(keyword))) {
                throw new IllegalStateException(FILE + " uses the unknown keyword " + keyword);
            }
        }
        if (schema.get("$ref") instanceof String ref) {
            Optional<String> problem = check(value, definition(ref), path);
            if (problem.isPresent()) {
                return problem;
            }
        }
        if (schema.containsKey("type") && !hasType(value, schema.get("type"))) {
            return problem(path, "must be " + typeText(schema.get("type")));
        }
        if (schema.containsKey("const") && !same(schema.get("const"), value)) {
            return problem(path, "must be " + schema.get("const"));
        }
        if (schema.get("enum") instanceof List<?> values && values.stream().noneMatch(v -> same(v, value))) {
            return problem(
                    path,
                    shown(value) + " is not allowed, it must be one of "
                            + values.stream().map(String::valueOf).collect(Collectors.joining(", ")));
        }
        if (value instanceof String text) {
            if (schema.get("minLength") instanceof Number min
                    && text.codePointCount(0, text.length()) < min.intValue()) {
                return problem(path, "must not be empty");
            }
            if (schema.get("pattern") instanceof String pattern
                    && !Pattern.compile(pattern).matcher(text).find()) {
                return problem(path, shown(value) + " is not allowed, it must match " + pattern);
            }
        }
        if (schema.get("not") instanceof Map<?, ?> not
                && check(value, not, path).isEmpty()) {
            return problem(path, shown(value) + " is reserved");
        }
        if (value instanceof Map<?, ?> map) {
            Optional<String> problem = object(map, schema, path);
            if (problem.isPresent()) {
                return problem;
            }
        }
        if (value instanceof List<?> list && schema.get("items") instanceof Map<?, ?> items) {
            for (int i = 0; i < list.size(); i++) {
                Optional<String> problem = check(list.get(i), items, path + "[" + i + "]");
                if (problem.isPresent()) {
                    return problem;
                }
            }
        }
        if (schema.get("allOf") instanceof List<?> all) {
            for (Object part : all) {
                Optional<String> problem = check(value, (Map<?, ?>) part, path);
                if (problem.isPresent()) {
                    return problem;
                }
            }
        }
        if (schema.get("if") instanceof Map<?, ?> condition
                && schema.get("then") instanceof Map<?, ?> then
                && check(value, condition, path).isEmpty()) {
            return check(value, then, path);
        }
        return Optional.empty();
    }

    /** The keys of an object: the required ones, then every key in the order of the file. */
    private static Optional<String> object(Map<?, ?> map, Map<?, ?> schema, String path) {
        if (schema.get("required") instanceof List<?> required) {
            for (Object key : required) {
                if (!map.containsKey(key)) {
                    return problem(path, "has no " + key);
                }
            }
        }
        Map<?, ?> properties = schema.get("properties") instanceof Map<?, ?> p ? p : Map.of();
        Object additional = schema.get("additionalProperties");
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = String.valueOf(entry.getKey());
            String inner = path.isEmpty() ? key : path + "." + key;
            Optional<String> problem;
            if (properties.get(key) instanceof Map<?, ?> property) {
                problem = check(entry.getValue(), property, inner);
            } else if (Boolean.FALSE.equals(additional)) {
                problem = problem(path, "has the unknown key " + shown(key));
            } else if (additional instanceof Map<?, ?> other) {
                problem = check(entry.getValue(), other, inner);
            } else {
                problem = Optional.empty();
            }
            if (problem.isPresent()) {
                return problem;
            }
        }
        return Optional.empty();
    }

    private static Map<?, ?> definition(String ref) {
        String prefix = "#/$defs/";
        if (!ref.startsWith(prefix)
                || !(((Map<?, ?>) SCHEMA.get("$defs")).get(ref.substring(prefix.length()))
                        instanceof Map<?, ?> definition)) {
            throw new IllegalStateException(FILE + " has no definition " + ref);
        }
        return definition;
    }

    private static boolean hasType(Object value, Object type) {
        if (type instanceof List<?> types) {
            return types.stream().anyMatch(t -> hasType(value, t));
        }
        return switch (String.valueOf(type)) {
            case "object" -> value instanceof Map<?, ?>;
            case "array" -> value instanceof List<?>;
            case "string" -> value instanceof String;
            case "boolean" -> value instanceof Boolean;
            case "integer" -> value instanceof Integer || value instanceof Long || value instanceof BigInteger;
            case "number" -> value instanceof Number;
            default -> throw new IllegalStateException(FILE + " uses the unknown type " + type);
        };
    }

    private static String typeText(Object type) {
        if (type instanceof List<?> types) {
            return types.stream().map(ProgramSchema::typeText).collect(Collectors.joining(" or "));
        }
        return switch (String.valueOf(type)) {
            case "object" -> "a map";
            case "array" -> "a list";
            case "string" -> "a text";
            case "boolean" -> "true or false";
            case "integer" -> "a whole number";
            case "number" -> "a number";
            default -> String.valueOf(type);
        };
    }

    /** Equal values; numbers by their value, whatever their Java type. */
    private static boolean same(Object expected, Object value) {
        if (expected instanceof Number a && value instanceof Number b) {
            return new BigDecimal(a.toString()).compareTo(new BigDecimal(b.toString())) == 0;
        }
        return Objects.equals(expected, value);
    }

    /** A value for a message, with the characters that could hide what it is made visible (task 15.7). */
    static String shown(Object value) {
        return Names.shown(String.valueOf(value));
    }

    private static Optional<String> problem(String path, String message) {
        return Optional.of((path.isEmpty() ? "the file" : path) + " " + message);
    }
}
