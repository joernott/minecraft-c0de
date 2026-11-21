package de.joernott.c0de.core.library;

import de.joernott.c0de.core.value.LibraryType;
import de.joernott.c0de.core.value.StructDefinition;
import de.joernott.c0de.core.value.Value;
import java.util.Map;

/**
 * A library of types, constants and functions, included with {@code INCLUDE mod/library}. Functions of
 * libraries are implemented in Java ({@link NativeFunction}). The names returned here are not qualified;
 * the cursor adds them with the library name, e.g. {@code c0de/strings/MID}.
 */
public interface Library {
    /** The name of the library, e.g. {@code c0de/strings}. The built-in functions have the empty name. */
    String name();

    /** The struct types of the library by name. */
    default Map<String, StructDefinition> types() {
        return Map.of();
    }

    /**
     * The library types by name, e.g. {@code CANVAS}; their {@link LibraryType#name()} is the full name, e.g.
     * {@code c0de_paint/cpaint/CANVAS} (since c0de 0.7.0).
     */
    default Map<String, LibraryType> libraryTypes() {
        return Map.of();
    }

    /** The constants of the library by name. */
    default Map<String, Value> constants() {
        return Map.of();
    }

    /** The functions of the library by name. */
    Map<String, NativeFunction> functions();
}
