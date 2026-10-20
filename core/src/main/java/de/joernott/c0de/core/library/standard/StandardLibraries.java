package de.joernott.c0de.core.library.standard;

import de.joernott.c0de.core.library.LibraryRegistry;

/** The libraries of the c0de language that don't need Minecraft. */
public final class StandardLibraries {
    private StandardLibraries() {}

    /**
     * A registry with the built-in functions and the libraries c0de/math, c0de/strings and c0de/time. The mod
     * adds c0de/minecraft.
     */
    public static LibraryRegistry registry() {
        return new LibraryRegistry()
                .add(new Builtins())
                .add(new MathLibrary())
                .add(new StringsLibrary())
                .add(new TimeLibrary());
    }

    /**
     * Converts a small document from JSON to YAML. Fails when the embedded YAML library is missing, so the mod
     * calls it at start-up and the error shows up in the server log instead of at the first FROM_JSON.
     */
    public static String selfTest() {
        return DataFormats.writeYaml(DataFormats.parse("{\"c0de\": [1, 2]}", DataFormats.JSON))
                .strip();
    }
}
