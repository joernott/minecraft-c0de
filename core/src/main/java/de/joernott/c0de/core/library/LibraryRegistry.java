package de.joernott.c0de.core.library;

import de.joernott.c0de.core.value.Value;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The libraries available to INCLUDE statements, the built-in functions (the library with the empty
 * name, which is always available), and built-in constants that every program can read without INCLUDE, such as
 * C0DE_VERSION and the constants of other mods (task 14.9).
 */
public final class LibraryRegistry {
    private final Map<String, Library> libraries = new LinkedHashMap<>();
    private final Map<String, Value> constants = new LinkedHashMap<>();

    /** Adds a library; a library with the same name is replaced. */
    public LibraryRegistry add(Library library) {
        libraries.put(library.name(), library);
        return this;
    }

    /** Adds a built-in constant that every program can read without INCLUDE; one with the same name is replaced. */
    public LibraryRegistry addConstant(String name, Value value) {
        constants.put(name, value);
        return this;
    }

    /** The built-in constants added with {@link #addConstant}, in the order they were added. */
    public Map<String, Value> constants() {
        return Collections.unmodifiableMap(constants);
    }

    /** The library with this name. */
    public Optional<Library> find(String name) {
        return Optional.ofNullable(libraries.get(name));
    }

    /** All libraries, including the built-in functions, in the order they were added. */
    public Collection<Library> all() {
        return libraries.values();
    }

    /** The built-in functions, or an empty library if none are registered. */
    public Library builtins() {
        return libraries.getOrDefault("", new Library() {
            @Override
            public String name() {
                return "";
            }

            @Override
            public Map<String, NativeFunction> functions() {
                return Map.of();
            }
        });
    }
}
