package de.joernott.c0de.core.eval;

import de.joernott.c0de.core.error.C0deException;
import de.joernott.c0de.core.error.ErrorCode;
import de.joernott.c0de.core.program.Initializer;
import de.joernott.c0de.core.program.Statement;
import de.joernott.c0de.core.value.ArrayValue;
import de.joernott.c0de.core.value.HashValue;
import de.joernott.c0de.core.value.StructDefinition;
import de.joernott.c0de.core.value.StructValue;
import de.joernott.c0de.core.value.Type;
import de.joernott.c0de.core.value.Value;
import de.joernott.c0de.core.value.Values;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Executes declarations: creates struct types and the variables and constants of VARIABLE and CONSTANT
 * statements with their initial values. All errors in declarations make the cursor crash.
 */
public final class Declarations {
    private Declarations() {}

    /**
     * Creates the struct type of a STRUCT declaration. Element types can only use structs that are known
     * already.
     *
     * @throws C0deException UNKNOWN_STRUCT_ENTITY if an element uses an unknown struct
     */
    public static StructDefinition defineStruct(Statement.StructDeclaration declaration, TypeRegistry types) {
        List<StructDefinition.Element> elements = new ArrayList<>();
        for (Statement.StructDeclaration.Element element : declaration.elements()) {
            Type type = located(element.line(), () -> types.resolve(element.type()));
            elements.add(new StructDefinition.Element(element.name(), type, element.tags()));
        }
        return new StructDefinition(declaration.name(), elements);
    }

    /**
     * Creates the variable or constant of a declaration with its initial value: the literal, the
     * initializer or the default value of the type.
     *
     * @throws C0deException (always a crash) UNKNOWN_STRUCT_ENTITY, SYNTAX_ERROR_TYPE_MISMATCH,
     *     STRING_TOO_LONG or SYNTAX_ERROR_VARIABLE / SYNTAX_ERROR_CONSTANT for initializers that don't fit
     *     the type
     */
    public static Variable declare(Statement.Declaration declaration, TypeRegistry types) {
        ErrorCode format = declaration.constant() ? ErrorCode.SYNTAX_ERROR_CONSTANT : ErrorCode.SYNTAX_ERROR_VARIABLE;
        Type type = located(declaration.line(), () -> types.resolve(declaration.type()));
        Value value;
        if (declaration.literal() != null) {
            value = located(declaration.line(), () -> literal(declaration.literal(), type));
        } else if (declaration.initializer() != null) {
            value = initialize(type, declaration.initializer(), format);
        } else {
            value = type.defaultValue();
        }
        return new Variable(declaration.name(), type, value, declaration.constant(), declaration.tags());
    }

    /** A literal converted to a type; a literal of another type is a SYNTAX_ERROR_TYPE_MISMATCH. */
    private static Value literal(Value literal, Type type) {
        try {
            return Values.convert(literal, type);
        } catch (C0deException e) {
            if (e.code() == ErrorCode.TYPE_MISMATCH) {
                throw C0deException.crash(ErrorCode.SYNTAX_ERROR_TYPE_MISMATCH, e.getMessage());
            }
            throw e.asCrash();
        }
    }

    private static Value initialize(Type type, Initializer initializer, ErrorCode format) {
        return switch (type) {
            case Type.Array array -> {
                ArrayValue value = new ArrayValue(array.element());
                for (Initializer.Entry entry : initializer.entries()) {
                    switch (entry) {
                        case Initializer.Values values -> {
                            for (Value literal : values.values()) {
                                value.elements().add(located(entry.line(), () -> literal(literal, array.element())));
                            }
                        }
                        case Initializer.Nested nested ->
                            value.elements().add(nested(array.element(), nested.content(), entry, format));
                        default ->
                            throw C0deException.at(
                                            entry.line(),
                                            format,
                                            "An array is initialized with literals, not with keys")
                                    .asCrash();
                    }
                }
                yield value;
            }
            case Type.Hash hash -> {
                HashValue value = new HashValue(hash.element());
                for (Initializer.Entry entry : initializer.entries()) {
                    String key = key(entry, format);
                    if (value.elements().containsKey(key)) {
                        throw C0deException.at(entry.line(), format, "The key " + key + " is used twice")
                                .asCrash();
                    }
                    value.elements().put(key, element(hash.element(), entry, format));
                }
                yield value;
            }
            case Type.Struct struct -> {
                StructValue value = new StructValue(struct.definition());
                Set<String> assigned = new HashSet<>();
                for (Initializer.Entry entry : initializer.entries()) {
                    String name = key(entry, format);
                    StructDefinition.Element element = struct.definition()
                            .element(name)
                            .orElseThrow(() -> C0deException.at(
                                            entry.line(),
                                            ErrorCode.UNKNOWN_STRUCT_ENTITY,
                                            "The struct " + struct.definition().name() + " has no element " + name)
                                    .asCrash());
                    if (!assigned.add(name)) {
                        throw C0deException.at(entry.line(), format, "The element " + name + " is used twice")
                                .asCrash();
                    }
                    value.set(name, element(element.type(), entry, format));
                }
                yield value;
            }
            default ->
                throw C0deException.at(
                                initializer.beginLine(),
                                format,
                                "A " + type + " can't be initialized with a code block")
                        .asCrash();
        };
    }

    private static String key(Initializer.Entry entry, ErrorCode format) {
        return switch (entry) {
            case Initializer.Keyed keyed -> keyed.key();
            case Initializer.KeyedNested keyed -> keyed.key();
            default ->
                throw C0deException.at(entry.line(), format, "Every line must start with a key")
                        .asCrash();
        };
    }

    private static Value element(Type type, Initializer.Entry entry, ErrorCode format) {
        return switch (entry) {
            case Initializer.Keyed keyed -> located(entry.line(), () -> literal(keyed.value(), type));
            case Initializer.KeyedNested keyed -> nested(type, keyed.content(), entry, format);
            default -> throw new IllegalStateException();
        };
    }

    private static Value nested(Type type, Initializer content, Initializer.Entry entry, ErrorCode format) {
        if (!(type instanceof Type.Array || type instanceof Type.Hash || type instanceof Type.Struct)) {
            throw C0deException.at(
                            entry.line(), format, "A " + type + " is initialized with a literal, not a code block")
                    .asCrash();
        }
        return initialize(type, content, format);
    }

    /** Runs an operation and assigns the line to its errors; errors in declarations always crash. */
    private static <T> T located(int line, java.util.function.Supplier<T> operation) {
        try {
            return operation.get();
        } catch (C0deException e) {
            throw e.inLine(line).asCrash();
        }
    }
}
