package de.joernott.c0de.core.edit;

import de.joernott.c0de.core.line.Block;
import de.joernott.c0de.core.line.Keyword;
import de.joernott.c0de.core.sheet.SheetGeometry;
import java.util.Optional;

/**
 * Rules that are checked when a player places a block on a sheet (docs/concept/03-programs.md and
 * 06-functions.md). They prevent mistakes that the validation can only report afterwards. All other rules
 * are left to the validation, so that programs can be written in any order.
 */
public final class PlacementRules {
    private PlacementRules() {}

    /**
     * Checks whether a language block may be placed at a position of a sheet: only in the code area (x=5 to
     * x=28) of the code layer directly above the floor.
     *
     * @param x the x coordinate relative to the sheet
     * @return the reason if no language block may be placed there
     */
    public static Optional<String> checkArea(int x, int y) {
        return y == SheetGeometry.CODE_Y && x >= SheetGeometry.CODE_MIN_X && x <= SheetGeometry.CODE_MAX_X
                ? Optional.empty()
                : Optional.of("On a sheet, c0de blocks can only be placed on the floor between x=5 and x=28");
    }

    /**
     * Checks whether a block may be placed at a position of the code layer of a sheet.
     *
     * @return the reason if the block must not be placed there
     */
    public static Optional<String> check(SheetView sheet, Block block, int x, int z) {
        Keyword keyword = block instanceof Block.KeywordBlock(Keyword k) ? k : null;
        Block west = sheet.at(x - 1, z);
        boolean startsLine = west == null;
        Block first = sheet.firstBlock(z);
        if (is(first, Keyword.NOOP) && sheet.lineStart(z) != x) {
            return Optional.of("A NOOP line must not contain other blocks");
        }
        if (keyword == Keyword.NOOP && (!startsLine || sheet.lineStart(z) >= 0)) {
            return Optional.of("A NOOP block must be the only block of its line");
        }
        if (keyword == Keyword.FUNCTION && !is(west, Keyword.END)) {
            if (x != SheetGeometry.CODE_MIN_X) {
                return Optional.of("A FUNCTION line starts at x=5");
            }
            if (z == 0 || !isNoopLine(sheet, z - 1)) {
                return Optional.of("A FUNCTION line must follow a NOOP line");
            }
        }
        if (keyword == Keyword.END && startsLine && is(sheet.at(x, z - 1), Keyword.BEGIN)) {
            return Optional.of("A code block must contain at least one statement, e.g. a NOOP line");
        }
        if (keyword == Keyword.PARAMETER || keyword == Keyword.RESULT) {
            return parameterOrResult(sheet, keyword, x, z, startsLine);
        }
        return Optional.empty();
    }

    /**
     * PARAMETER and RESULT lines directly follow the FUNCTION or CALL line, indented by one block, or another
     * PARAMETER line; RESULT lines may also follow RESULT lines.
     */
    private static Optional<String> parameterOrResult(
            SheetView sheet, Keyword keyword, int x, int z, boolean startsLine) {
        String message = keyword
                + " lines directly follow the FUNCTION or CALL line, indented by one block"
                + (keyword == Keyword.PARAMETER ? ", or another PARAMETER line" : ", or a PARAMETER or RESULT line");
        if (!startsLine || z == 0) {
            return Optional.of(message);
        }
        Block owner = sheet.at(x - 1, z - 1);
        if ((is(owner, Keyword.FUNCTION) || is(owner, Keyword.CALL)) && sheet.lineStart(z - 1) == x - 1) {
            return Optional.empty();
        }
        Block above = sheet.at(x, z - 1);
        boolean followsParameter = is(above, Keyword.PARAMETER);
        boolean followsResult = is(above, Keyword.RESULT);
        if (sheet.lineStart(z - 1) == x && (followsParameter || (keyword == Keyword.RESULT && followsResult))) {
            return Optional.empty();
        }
        return Optional.of(message);
    }

    private static boolean isNoopLine(SheetView sheet, int z) {
        int start = sheet.lineStart(z);
        return start >= 0 && is(sheet.at(start, z), Keyword.NOOP) && sheet.at(start + 1, z) == null;
    }

    private static boolean is(Block block, Keyword keyword) {
        return block instanceof Block.KeywordBlock(Keyword k) && k == keyword;
    }
}
