package de.joernott.c0de.core.edit;

import de.joernott.c0de.core.sheet.SheetGeometry;
import java.util.OptionalInt;

/**
 * Whether the pliers can move lines to the left or the right (docs/concept/14-items-and-tools.md): all blocks of
 * the lines move by one block, and none of them may leave the code area.
 */
public final class Indentation {
    private Indentation() {}

    /**
     * The first line that can't move because one of its blocks would leave the code area.
     *
     * @param from the first line
     * @param to the last line (inclusive)
     * @param dx -1 to move the lines to the left (west), 1 to the right (east)
     * @return the line, or empty if all lines can move
     */
    public static OptionalInt blocked(SheetView view, int from, int to, int dx) {
        int edge = dx < 0 ? SheetGeometry.CODE_MIN_X : SheetGeometry.CODE_MAX_X;
        for (int z = from; z <= to; z++) {
            if (view.at(edge, z) != null) {
                return OptionalInt.of(z);
            }
        }
        return OptionalInt.empty();
    }

    /** True if all lines from {@code from} to {@code to} (inclusive) are empty, so there is nothing to move. */
    public static boolean isEmpty(SheetView view, int from, int to) {
        for (int z = from; z <= to; z++) {
            if (view.lineStart(z) >= 0) {
                return false;
            }
        }
        return true;
    }
}
