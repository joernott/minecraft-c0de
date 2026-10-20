package de.joernott.c0de.core.program;

import de.joernott.c0de.core.value.Type;
import java.util.Locale;

/**
 * The text form of a type as written with type blocks, e.g. {@code INTEGER}, {@code STRING 20},
 * {@code ARRAY HASH FLOAT} or {@code STRUCT point}. Variable and constant items store their type in this form.
 */
public final class TypeText {
    private TypeText() {}

    /**
     * Parses a type.
     *
     * @throws IllegalArgumentException if the text is not a complete type
     */
    public static TypeSpec parse(String text) {
        String[] words = text.trim().split("\\s+");
        int[] position = {0};
        TypeSpec type = parse(words, position, 1);
        if (position[0] != words.length) {
            throw new IllegalArgumentException("Unexpected " + words[position[0]] + " after the type");
        }
        return type;
    }

    private static TypeSpec parse(String[] words, int[] position, int depth) {
        if (position[0] >= words.length || words[position[0]].isEmpty()) {
            throw new IllegalArgumentException("A type is missing");
        }
        if (depth > ProgramParser.MAX_DEPTH) {
            throw new IllegalArgumentException("Types can be nested at most " + ProgramParser.MAX_DEPTH + " times");
        }
        String word = words[position[0]++].toUpperCase(Locale.ROOT);
        return switch (word) {
            case "BOOLEAN" -> new TypeSpec.Simple(Type.Simple.BOOLEAN);
            case "INTEGER" -> new TypeSpec.Simple(Type.Simple.INTEGER);
            case "FLOAT" -> new TypeSpec.Simple(Type.Simple.FLOAT);
            case "CHAR" -> new TypeSpec.Simple(Type.Simple.CHAR);
            case "TIME" -> new TypeSpec.Simple(Type.Simple.TIME);
            case "ERROR" -> new TypeSpec.Error();
            case "STRING" -> {
                if (position[0] < words.length && words[position[0]].matches("\\d+")) {
                    yield new TypeSpec.Str(Long.parseLong(words[position[0]++]));
                }
                yield new TypeSpec.Str(null);
            }
            case "ARRAY" -> new TypeSpec.Array(parse(words, position, depth + 1));
            case "HASH" -> new TypeSpec.Hash(parse(words, position, depth + 1));
            case "STRUCT" -> {
                if (position[0] >= words.length || !Names.isValidQualified(words[position[0]])) {
                    throw new IllegalArgumentException("A STRUCT needs the name of the struct type");
                }
                yield new TypeSpec.Struct(words[position[0]++]);
            }
            default -> throw new IllegalArgumentException("Not a type: " + word);
        };
    }

    /** The text form of a type. */
    public static String format(TypeSpec type) {
        return switch (type) {
            case TypeSpec.Simple simple -> simple.type().name();
            case TypeSpec.Str str -> str.maxLength() == null ? "STRING" : "STRING " + str.maxLength();
            case TypeSpec.Array array -> "ARRAY " + format(array.element());
            case TypeSpec.Hash hash -> "HASH " + format(hash.element());
            case TypeSpec.Struct struct -> "STRUCT " + struct.name();
            case TypeSpec.Error error -> "ERROR";
        };
    }
}
