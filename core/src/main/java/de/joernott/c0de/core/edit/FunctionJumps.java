package de.joernott.c0de.core.edit;

import de.joernott.c0de.core.line.Block;
import de.joernott.c0de.core.line.Keyword;
import de.joernott.c0de.core.sheet.SheetGeometry;
import java.util.OptionalInt;

/**
 * Where boots take a player who left-clicks a FUNCTION block (docs/concept/14-items-and-tools.md): from the
 * FUNCTION line of a function to its END FUNCTION line and back. A function contains no empty line, so the search
 * ends at the first one.
 */
public final class FunctionJumps {
    /** The maximum number of lines that are searched. */
    public static final int MAX_LINES = 10_000;

    private FunctionJumps() {}

    /** True for the FUNCTION block that starts a function, at x=5. */
    public static boolean isStart(SheetView view, int x, int z) {
        return x == SheetGeometry.CODE_MIN_X && is(view, x, z, Keyword.FUNCTION);
    }

    /** True for the FUNCTION block behind the END block that ends a function, at x=6. */
    public static boolean isEnd(SheetView view, int x, int z) {
        return x == SheetGeometry.CODE_MIN_X + 1
                && is(view, x, z, Keyword.FUNCTION)
                && is(view, SheetGeometry.CODE_MIN_X, z, Keyword.END);
    }

    /**
     * The line of the counterpart of a FUNCTION block: the END FUNCTION line of a FUNCTION line, or the FUNCTION
     * line of an END FUNCTION line.
     *
     * @return the line, or empty if the block is no such FUNCTION block or its function is not complete
     */
    public static OptionalInt counterpart(SheetView view, int x, int z) {
        if (isStart(view, x, z)) {
            for (int line = z + 1; line < z + MAX_LINES && view.lineStart(line) >= 0; line++) {
                if (isEnd(view, SheetGeometry.CODE_MIN_X + 1, line)) {
                    return OptionalInt.of(line);
                }
                if (isStart(view, SheetGeometry.CODE_MIN_X, line)) {
                    return OptionalInt.empty();
                }
            }
        } else if (isEnd(view, x, z)) {
            for (int line = z - 1; line >= 0 && line > z - MAX_LINES && view.lineStart(line) >= 0; line--) {
                if (isStart(view, SheetGeometry.CODE_MIN_X, line)) {
                    return OptionalInt.of(line);
                }
                if (isEnd(view, SheetGeometry.CODE_MIN_X + 1, line)) {
                    return OptionalInt.empty();
                }
            }
        }
        return OptionalInt.empty();
    }

    private static boolean is(SheetView view, int x, int z, Keyword keyword) {
        return view.at(x, z) instanceof Block.KeywordBlock(var found) && found == keyword;
    }
}
