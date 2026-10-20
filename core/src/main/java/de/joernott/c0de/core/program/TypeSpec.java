package de.joernott.c0de.core.program;

import de.joernott.c0de.core.value.Type;

/**
 * A type as written in a program. Struct types are referenced by name; they are resolved to {@link Type}s
 * when the declaration is executed, because structs can only be used after they have been declared.
 */
public sealed interface TypeSpec {
    record Simple(Type.Simple type) implements TypeSpec {}

    /** STRING with an optional length limit (null for no limit). */
    record Str(Long maxLength) implements TypeSpec {}

    record Array(TypeSpec element) implements TypeSpec {}

    record Hash(TypeSpec element) implements TypeSpec {}

    /** A struct type by name; qualified names refer to types of libraries, e.g. {@code c0de/minecraft/POSITION}. */
    record Struct(String name) implements TypeSpec {}

    /** The predefined type ERROR. */
    record Error() implements TypeSpec {}

    /** True for ARRAY, HASH, STRUCT and ERROR, whose values are initialized with a code block. */
    default boolean isComplex() {
        return this instanceof Array || this instanceof Hash || this instanceof Struct || this instanceof Error;
    }
}
