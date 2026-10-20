package de.joernott.c0de.core.program;

import de.joernott.c0de.core.line.IndexEntry;
import java.util.List;

/** A reference to a variable or constant by its name, optionally with an index, e.g. {@code matrix[i][2]}. */
public record LabelRef(String name, List<IndexEntry> index) {
    public LabelRef {
        index = List.copyOf(index);
    }

    public LabelRef(String name) {
        this(name, List.of());
    }
}
