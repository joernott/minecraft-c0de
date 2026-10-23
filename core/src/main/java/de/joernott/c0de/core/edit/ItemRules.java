package de.joernott.c0de.core.edit;

import de.joernott.c0de.core.error.C0deException;
import de.joernott.c0de.core.eval.TypeRegistry;
import de.joernott.c0de.core.library.standard.JsonValues;
import de.joernott.c0de.core.program.Names;
import de.joernott.c0de.core.program.TypeSpec;
import de.joernott.c0de.core.program.TypeText;
import de.joernott.c0de.core.value.Literals;
import de.joernott.c0de.core.value.Type;
import de.joernott.c0de.core.value.Value;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * The rules for the properties of variable and constant items (docs/concept/14-items-and-tools.md): name, type
 * and value. Values of simple types and STRING are entered like the values of LITERAL blocks, values of ARRAY,
 * HASH, STRUCT and ERROR as JSON. Items store all values as JSON; an empty value means that the item has no
 * value yet.
 */
public final class ItemRules {
    private ItemRules() {}

    /** The types without subtype that an item can have, and that are the subtypes of arrays and hashes. */
    private static final List<String> ELEMENT_TYPES =
            List.of("BOOLEAN", "INTEGER", "FLOAT", "CHAR", "STRING", "TIME", "ERROR");

    /**
     * The types that can be chosen in the dialog of an item: the simple types, STRING, ERROR and the struct types
     * of the libraries, and arrays and hashes of them. The struct types of a program are only known in the
     * program, so they can only be typed.
     *
     * @param base the kind of type that the item got when it was crafted (e.g. {@code ARRAY}), or empty for an
     *     uninitialized item; only the types of this kind are returned
     * @param structTypes the qualified names of the struct types of the libraries
     */
    public static List<String> typeChoices(String base, Collection<String> structTypes) {
        List<String> elements = new ArrayList<>(ELEMENT_TYPES);
        structTypes.stream().sorted().forEach(name -> elements.add("STRUCT " + name));
        List<String> all = new ArrayList<>(elements);
        elements.forEach(element -> all.add("ARRAY " + element));
        elements.forEach(element -> all.add("HASH " + element));
        return base.isEmpty()
                ? all
                : all.stream().filter(type -> type.split(" ")[0].equals(base)).toList();
    }

    /** The result of checking the properties: the value as JSON to store, or a problem. */
    public record Result(String json, Optional<String> problem) {
        static Result problem(String problem) {
            return new Result("", Optional.of(problem));
        }
    }

    /**
     * Checks the properties entered in the dialog of an item.
     *
     * @param name the name, empty for an empty item; name, type and value all empty make an uninitialized item
     * @param type the type in its text form, e.g. {@code ARRAY INTEGER}
     * @param value the value as entered, empty for none
     */
    public static Result check(String name, String type, String value, ZoneId zone) {
        if (name.length() > PropertyRules.MAX_TEXT_LENGTH || value.length() > 10 * PropertyRules.MAX_TEXT_LENGTH) {
            return Result.problem("The name or value is too long");
        }
        if (name.isBlank() && type.isBlank() && value.isEmpty()) {
            // An uninitialized item: a result target without type, e.g. for a result that has no parameter
            return new Result("", Optional.empty());
        }
        if (name.isEmpty() && !value.isEmpty()) {
            return Result.problem("An item with a value needs a name");
        }
        if (!name.isEmpty() && !Names.isValid(name)) {
            return Result.problem("A name consists of letters, digits and underscores and starts with a letter");
        }
        TypeSpec spec;
        try {
            spec = TypeText.parse(type);
        } catch (IllegalArgumentException e) {
            return Result.problem(e.getMessage());
        }
        if (value.isEmpty()) {
            return new Result("", Optional.empty());
        }
        try {
            return new Result(json(spec, value, zone), Optional.empty());
        } catch (IllegalArgumentException | C0deException e) {
            return Result.problem(e.getMessage());
        }
    }

    /** The value as JSON. Values of struct types can only be checked when they are passed to a program. */
    private static String json(TypeSpec spec, String value, ZoneId zone) {
        return switch (spec) {
            case TypeSpec.Simple simple -> JsonValues.write(Literals.parse(simple.type(), value, zone));
            case TypeSpec.Str str -> JsonValues.write(Literals.parse(new Type.Str(str.maxLength()), value, zone));
            default -> {
                if (containsStruct(spec)) {
                    // Struct types are only known in a program, so only the JSON syntax and shape are checked
                    Object tree = JsonValues.parse(value);
                    boolean shape = spec instanceof TypeSpec.Array
                            ? tree instanceof java.util.List
                            : tree instanceof java.util.Map;
                    if (!shape) {
                        throw new IllegalArgumentException(
                                spec instanceof TypeSpec.Array
                                        ? "The value must be a JSON array"
                                        : "The value must be a JSON object");
                    }
                    yield value;
                }
                yield JsonValues.write(JsonValues.read(value, new TypeRegistry().resolve(spec)));
            }
        };
    }

    private static boolean containsStruct(TypeSpec spec) {
        return switch (spec) {
            case TypeSpec.Array array -> containsStruct(array.element());
            case TypeSpec.Hash hash -> containsStruct(hash.element());
            case TypeSpec.Struct struct -> true;
            case TypeSpec.Error error -> true;
            default -> false;
        };
    }

    /** The value as it is shown in the dialog: like a literal for simple types and STRING, JSON otherwise. */
    public static String display(String type, String json) {
        if (json.isEmpty()) {
            return "";
        }
        try {
            return switch (TypeText.parse(type)) {
                case TypeSpec.Simple simple ->
                    JsonValues.read(json, simple.type()).text();
                case TypeSpec.Str str ->
                    JsonValues.read(json, Type.Str.UNLIMITED).text();
                default -> json;
            };
        } catch (IllegalArgumentException | C0deException e) {
            return json;
        }
    }

    /**
     * The value of an item as a value of the type of a parameter.
     *
     * @throws C0deException TYPE_MISMATCH if the value doesn't fit the type
     */
    public static Value value(String json, Type type) {
        return json.isEmpty() ? type.defaultValue() : JsonValues.read(json, type);
    }
}
