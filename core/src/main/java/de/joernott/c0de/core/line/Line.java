package de.joernott.c0de.core.line;

import java.util.Comparator;
import java.util.List;

/**
 * A line of a sheet: the blocks with the same z coordinate in the code area, sorted by their x coordinate.
 * x coordinates are relative to the sheet (5 to 28).
 */
public record Line(int z, List<Cell> cells) {
    public Line {
        cells = cells.stream().sorted(Comparator.comparingInt(Cell::x)).toList();
    }

    /** True if the line contains no blocks at all. The first empty line ends a program. */
    public boolean isEmpty() {
        return cells.isEmpty();
    }

    /** A block and its relative x coordinate. */
    public record Cell(int x, Block block) {}
}
