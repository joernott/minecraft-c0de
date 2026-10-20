package de.joernott.c0de.core.program;

import de.joernott.c0de.core.line.Keyword;
import java.util.regex.Pattern;

/** The rules for names (see docs/concept/02-language-basics.md, "Naming with labels"). */
public final class Names {
    private static final Pattern NAME = Pattern.compile("[A-Za-z][A-Za-z0-9_]*");

    private Names() {}

    /** A valid name: letters, digits and underscores, starting with a letter, not a keyword. */
    public static boolean isValid(String name) {
        return name != null && NAME.matcher(name).matches() && !Keyword.isReserved(name);
    }

    /**
     * A valid name or a qualified name of a library element, e.g. {@code c0de/strings/MID}: valid names
     * separated by slashes.
     */
    public static boolean isValidQualified(String name) {
        if (name == null || name.isEmpty() || name.startsWith("/") || name.endsWith("/")) {
            return false;
        }
        for (String part : name.split("/")) {
            if (!isValid(part)) {
                return false;
            }
        }
        return true;
    }

    /** A valid library name: at least the mod and the library, e.g. {@code c0de/strings}. */
    public static boolean isValidLibrary(String name) {
        return isValidQualified(name) && name.contains("/");
    }
}
