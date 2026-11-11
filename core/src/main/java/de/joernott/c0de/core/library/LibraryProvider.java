package de.joernott.c0de.core.library;

/**
 * Adds the libraries and built-in constants of another mod to the program runner (task 14.1). The runner finds the
 * providers on its class path with {@link java.util.ServiceLoader}: a jar names its provider in
 * {@code META-INF/services/de.joernott.c0de.core.library.LibraryProvider}. In the game, mods register their
 * libraries with the mod instead (docs/library-development.md).
 */
public interface LibraryProvider {
    /** Adds the libraries and built-in constants to the registry of the runner. */
    void register(LibraryRegistry registry);
}
