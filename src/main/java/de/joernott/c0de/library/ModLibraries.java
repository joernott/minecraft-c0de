package de.joernott.c0de.library;

import de.joernott.c0de.core.library.LibraryRegistry;
import de.joernott.c0de.core.library.standard.StandardLibraries;

/** The libraries that programs in the game can include: the standard libraries and c0de/minecraft. */
public final class ModLibraries {
    private static final LibraryRegistry REGISTRY = StandardLibraries.registry().add(new MinecraftLibrary());

    private ModLibraries() {}

    public static LibraryRegistry registry() {
        return REGISTRY;
    }
}
