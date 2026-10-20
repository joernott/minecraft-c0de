package de.joernott.c0de.dialog;

import de.joernott.c0de.core.edit.LabelMode;
import de.joernott.c0de.core.library.Library;
import de.joernott.c0de.core.library.LibraryRegistry;
import de.joernott.c0de.core.library.standard.StandardLibraries;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * The entries of the lists of the LABEL dialog. For now, they are static: all libraries and all of their
 * functions. Sprint 7 (task 7.5) adds the functions, struct types and variables of the program.
 */
public final class DialogOptions {
    /** The library of the mod, which is added in sprint 10. */
    private static final String MINECRAFT_LIBRARY = "c0de/minecraft";

    private static final LibraryRegistry LIBRARIES = StandardLibraries.registry();

    private DialogOptions() {}

    /** The names of the built-in functions. */
    public static Set<String> builtinFunctions() {
        return LIBRARIES.builtins().functions().keySet();
    }

    /** The entries of the list of the LABEL dialog in a position. */
    public static List<String> forLabel(LabelMode mode) {
        return switch (mode) {
            case LIBRARY -> libraries();
            case FUNCTION -> functions();
            default -> List.of();
        };
    }

    private static List<String> libraries() {
        TreeSet<String> names = new TreeSet<>();
        for (Library library : LIBRARIES.all()) {
            if (!library.name().isEmpty()) {
                names.add(library.name());
            }
        }
        names.add(MINECRAFT_LIBRARY);
        return List.copyOf(names);
    }

    private static List<String> functions() {
        List<String> names = new ArrayList<>(new TreeSet<>(builtinFunctions()));
        for (Library library : LIBRARIES.all()) {
            if (!library.name().isEmpty()) {
                new TreeSet<>(library.functions().keySet()).forEach(name -> names.add(library.name() + "/" + name));
            }
        }
        return names;
    }
}
