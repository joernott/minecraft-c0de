package de.joernott.c0de.core.eval;

import de.joernott.c0de.core.error.C0deException;
import de.joernott.c0de.core.error.ErrorCode;
import de.joernott.c0de.core.program.TypeSpec;
import de.joernott.c0de.core.value.LibraryType;
import de.joernott.c0de.core.value.StructDefinition;
import de.joernott.c0de.core.value.Type;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The struct types known to a cursor (declared in the program or provided by included libraries) and the library
 * types of the included libraries.
 */
public final class TypeRegistry {
    private final Map<String, StructDefinition> structs = new LinkedHashMap<>();
    private final Map<String, LibraryType> libraryTypes = new LinkedHashMap<>();

    public Optional<StructDefinition> find(String name) {
        return Optional.ofNullable(structs.get(name));
    }

    /** All struct types by the name they were added with, in the order they were added. */
    public Map<String, StructDefinition> all() {
        return java.util.Collections.unmodifiableMap(structs);
    }

    /** The name a struct type was added with, e.g. {@code c0de/minecraft/POSITION}. */
    public Optional<String> nameOf(StructDefinition definition) {
        return structs.entrySet().stream()
                .filter(entry -> entry.getValue().equals(definition))
                .map(Map.Entry::getKey)
                .findFirst();
    }

    /** Adds a struct type. Library types are added with their qualified names, e.g. c0de/minecraft/POSITION. */
    public void add(String name, StructDefinition definition) {
        structs.put(name, definition);
    }

    /** Adds a library type with its full name, e.g. {@code c0de_paint/cpaint/CANVAS}. */
    public void addLibraryType(LibraryType type) {
        libraryTypes.put(type.name(), type);
    }

    /** The library type with this full name. */
    public Optional<LibraryType> findLibraryType(String name) {
        return Optional.ofNullable(libraryTypes.get(name));
    }

    /** All library types by their full names, in the order they were added. */
    public Map<String, LibraryType> libraryTypes() {
        return java.util.Collections.unmodifiableMap(libraryTypes);
    }

    /**
     * The type of a type specification.
     *
     * @throws C0deException UNKNOWN_STRUCT_ENTITY (crash) if a struct or library type is not known
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
            case TypeSpec.Library library ->
                new Type.Library(findLibraryType(library.name())
                        .orElseThrow(() -> C0deException.crash(
                                ErrorCode.UNKNOWN_STRUCT_ENTITY,
                                "No included library defines the type " + library.name())));
        };
    }
}
