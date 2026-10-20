package de.joernott.c0de.core.eval;

import de.joernott.c0de.core.error.C0deException;
import de.joernott.c0de.core.error.ErrorCode;
import de.joernott.c0de.core.program.TypeSpec;
import de.joernott.c0de.core.value.StructDefinition;
import de.joernott.c0de.core.value.Type;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** The struct types known to a cursor: declared in the program or provided by included libraries. */
public final class TypeRegistry {
    private final Map<String, StructDefinition> structs = new LinkedHashMap<>();

    public Optional<StructDefinition> find(String name) {
        return Optional.ofNullable(structs.get(name));
    }

    /** Adds a struct type. Library types are added with their qualified names, e.g. c0de/minecraft/POSITION. */
    public void add(String name, StructDefinition definition) {
        structs.put(name, definition);
    }

    /**
     * The type of a type specification.
     *
     * @throws C0deException UNKNOWN_STRUCT_ENTITY (crash) if a struct is not known
     */
    public Type resolve(TypeSpec spec) {
        return switch (spec) {
            case TypeSpec.Simple simple -> simple.type();
            case TypeSpec.Str str -> new Type.Str(str.maxLength());
            case TypeSpec.Array array -> new Type.Array(resolve(array.element()));
            case TypeSpec.Hash hash -> new Type.Hash(resolve(hash.element()));
            case TypeSpec.Error error -> Type.Struct.ERROR;
            case TypeSpec.Struct struct ->
                new Type.Struct(find(struct.name())
                        .orElseThrow(() -> C0deException.crash(
                                ErrorCode.UNKNOWN_STRUCT_ENTITY,
                                "The struct " + struct.name() + " has not been declared")));
        };
    }
}
