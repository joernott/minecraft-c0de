package de.joernott.c0de.core.library;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The libraries available to INCLUDE statements, and the built-in functions (the library with the empty
 * name, which is always available).
 */
public final class LibraryRegistry {
    private final Map<String, Library> libraries = new LinkedHashMap<>();

    /** Adds a library; a library with the same name is replaced. */
    public LibraryRegistry add(Library library) {
        libraries.put(library.name(), library);
        return this;
    }

    /** The library with this name. */
    public Optional<Library> find(String name) {
        return Optional.ofNullable(libraries.get(name));
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
