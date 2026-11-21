package de.joernott.c0de.core.value;

/**
 * A type defined by a library besides its struct types, e.g. the canvases of c0de paint (docs/concept/04-data.md,
 * "Library type"). Its values are opaque {@link LibraryValue}s that programs only pass to the functions of the
 * library. Programs declare them with the TYPE block: {@code VARIABLE c TYPE c0de_paint/cpaint/CANVAS}.
 */
public interface LibraryType {
    /** The full name, e.g. {@code c0de_paint/cpaint/CANVAS}. */
    String name();

    /** The value of a new variable of this type. */
    LibraryValue defaultValue();

    /**
     * A compact text of the value, saved with the cursor so that a process keeps it over a restart of the server,
     * e.g. Base64 of compressed bytes.
     */
    String save(LibraryValue value);

    /** The value of a text created by {@link #save}. */
    LibraryValue load(String data);
}
