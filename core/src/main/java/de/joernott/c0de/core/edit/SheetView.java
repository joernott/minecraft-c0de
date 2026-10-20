package de.joernott.c0de.core.edit;

import de.joernott.c0de.core.line.Block;
import de.joernott.c0de.core.line.Keyword;
import de.joernott.c0de.core.line.Line;
import de.joernott.c0de.core.line.Sheet;
import de.joernott.c0de.core.sheet.SheetGeometry;

/**
 * Read access to the blocks of a sheet. Positions are relative to the sheet: x from 0 to 31 (the code area is
 * x=5 to x=28), z is the line, starting with 0 for the PROGRAM line.
 */
@FunctionalInterface
public interface SheetView {
    /** The c0de block at a position, or null for air and other blocks. */
    Block at(int x, int z);

    /** The x coordinate of the first block of a line, or -1 if the line is empty. */
    default int lineStart(int z) {
        for (int x = SheetGeometry.CODE_MIN_X; x <= SheetGeometry.CODE_MAX_X; x++) {
            if (at(x, z) != null) {
                return x;
            }
        }
        return -1;
    }

    /** The first block of a line, or null if the line is empty. */
    default Block firstBlock(int z) {
        int x = lineStart(z);
        return x < 0 ? null : at(x, z);
    }

    /** The name of the program of the sheet (the label of the PROGRAM line), or the empty string. */
    default String programName() {
        if (at(SheetGeometry.CODE_MIN_X, 0) instanceof Block.KeywordBlock(var keyword)
                && keyword == Keyword.PROGRAM
                && at(SheetGeometry.CODE_MIN_X + 1, 0) instanceof Block.LabelBlock label
                && label.name() != null) {
            return label.name();
        }
        return "";
    }

    /** A view of a sheet of the line model, e.g. one parsed from the notation. */
    static SheetView of(Sheet sheet) {
        return (x, z) -> {
            for (Line.Cell cell : sheet.line(z).cells()) {
                if (cell.x() == x) {
                    return cell.block();
                }
            }
            return null;
        };
    }
}
