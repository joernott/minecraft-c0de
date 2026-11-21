package de.joernott.c0de.core.value;

/**
 * A value of a {@link LibraryType}. Like every value of c0de, it is copied on every assignment, as a parameter and
 * as a result; implementations make {@link #copy()} cheap with copy on write. {@code equals} is the comparison of
 * {@code =} and {@code <>}; {@link #text()} a short description for SAY, displays and the debugger.
 */
public non-sealed interface LibraryValue extends Value {
    /** The type of the value. */
    LibraryType libraryType();

    @Override
    default Type type() {
        return new Type.Library(libraryType());
    }

    /** A copy that doesn't change when the original changes; it should share the content until then. */
    @Override
    Value copy();
}
