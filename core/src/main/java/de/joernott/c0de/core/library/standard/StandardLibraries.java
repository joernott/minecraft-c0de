package de.joernott.c0de.core.library.standard;

import de.joernott.c0de.core.library.LibraryRegistry;
import de.joernott.c0de.core.value.StringValue;

/** The libraries of the c0de language that don't need Minecraft. */
public final class StandardLibraries {
    private StandardLibraries() {}

    /** The name of the built-in constant with the version of c0de (task 14.9). */
    public static final String VERSION_CONSTANT = "C0DE_VERSION";

    /**
     * A registry with the built-in functions, the libraries c0de/math, c0de/strings and c0de/time and the constant
     * C0DE_VERSION. The mod adds c0de/minecraft and sets C0DE_VERSION to the version of the mod.
     */
    public static LibraryRegistry registry() {
        return new LibraryRegistry()
                .addConstant(VERSION_CONSTANT, new StringValue(version()))
                .add(new Builtins())
                .add(new MathLibrary())
                .add(new StringsLibrary())
                .add(new TimeLibrary());
    }

    /**
     * The version of c0de without the mod: from the manifest of the jar (the program runner), else from the system
     * property {@code c0de.version} (the tests), else "dev".
     */
    public static String version() {
        String version = StandardLibraries.class.getPackage().getImplementationVersion();
        if (version == null || version.isBlank()) {
            version = System.getProperty("c0de.version", "dev");
        }
        return version;
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
