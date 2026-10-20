package de.joernott.c0de.core.line;

import de.joernott.c0de.core.value.Value;

/** An entry of the index of a LABEL, e.g. {@code [2]}, {@code ["key"]}, {@code [i]} or {@code .x}. */
public sealed interface IndexEntry {
    /** A literal INTEGER position or STRING key, e.g. {@code [2]} or {@code ["key"]}. */
    record Literal(Value value) implements IndexEntry {}

    /** The name of an INTEGER or STRING variable or constant whose value is used, e.g. {@code [i]}. */
    record Variable(String name) implements IndexEntry {}

    /** An element name of a struct, e.g. {@code .x}. For hashes it is used as key. */
    record Element(String name) implements IndexEntry {}
}
