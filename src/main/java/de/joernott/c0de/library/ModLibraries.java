package de.joernott.c0de.library;

import de.joernott.c0de.C0de;
import de.joernott.c0de.core.library.Library;
import de.joernott.c0de.core.library.LibraryRegistry;
import de.joernott.c0de.core.library.standard.StandardLibraries;
import de.joernott.c0de.core.program.Names;

/**
 * The libraries that programs in the game can include: the standard libraries, c0de/minecraft and the libraries
 * of other mods, which register them with {@link #register} while they are initialized (docs/library-development.md).
 */
public final class ModLibraries {
    private static final LibraryRegistry REGISTRY = StandardLibraries.registry().add(new MinecraftLibrary());

    private ModLibraries() {}

    public static LibraryRegistry registry() {
        return REGISTRY;
    }

    /**
     * Registers the library of another mod. Its name is the ID of the mod, a slash and the name of the library,
     * e.g. {@code mymod/robots}; names of c0de ({@code c0de/…}) and names that are already registered are refused.
     * Call it while the mod is initialized, before worlds are loaded.
     *
     * @throws IllegalArgumentException if the name is not valid or already registered
     */
    public static synchronized void register(Library library) {
        String name = library.name();
        if (!Names.isValidLibrary(name)) {
            throw new IllegalArgumentException("Not a library name (mod/library): " + name);
        }
        if (name.startsWith(C0de.MOD_ID + "/")) {
            throw new IllegalArgumentException("The libraries " + C0de.MOD_ID + "/… belong to c0de: " + name);
        }
        if (REGISTRY.find(name).isPresent()) {
            throw new IllegalArgumentException("The library " + name + " is already registered");
        }
        REGISTRY.add(library);
        C0de.LOGGER.info("Registered the c0de library {}", name);
    }
}
