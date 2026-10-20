package de.joernott.c0de.library;

import de.joernott.c0de.core.library.LibraryRegistry;
import de.joernott.c0de.core.library.standard.StandardLibraries;

/** The libraries that programs in the game can include. Sprint 10 adds c0de/minecraft. */
public final class ModLibraries {
    private static final LibraryRegistry REGISTRY = StandardLibraries.registry();

    private ModLibraries() {}

    public static LibraryRegistry registry() {
        return REGISTRY;
    }
}
