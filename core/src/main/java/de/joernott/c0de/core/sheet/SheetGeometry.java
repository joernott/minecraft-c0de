package de.joernott.c0de.core.sheet;

/**
 * Coordinates of the sheets of greenbar paper in the c0de dimension.
 *
 * <p>A sheet is two chunks (32 blocks) wide in x direction and extends from z=0 in positive z
 * direction. Sheet n covers the absolute x coordinates 32·n to 32·n+31. All chunks with a negative x
 * or z coordinate belong to no sheet. Relative x coordinates are relative to the western border of a
 * sheet; y and z coordinates are always absolute.
 */
public final class SheetGeometry {
    /** Width of a sheet in blocks. */
    public static final int SHEET_WIDTH = 32;

    /** y coordinate of the floor. */
    public static final int FLOOR_Y = -4;

    /** y coordinate of the code layer, directly on top of the floor. All blocks of a program are placed here. */
    public static final int CODE_Y = -3;

    /** Lowest y coordinate of the dimension. */
    public static final int MIN_Y = -16;

    /** Relative x coordinate of the cursor rail. */
    public static final int CURSOR_X = 4;

    /** Lowest relative x coordinate of the code area. */
    public static final int CODE_MIN_X = 5;

    /** Highest relative x coordinate of the code area. */
    public static final int CODE_MAX_X = 28;

    /** Number of blocks of a line. */
    public static final int LINE_LENGTH = CODE_MAX_X - CODE_MIN_X + 1;

    /** Value of {@link #sheetIndex} for coordinates outside of all sheets. */
    public static final int NO_SHEET = -1;

    private SheetGeometry() {}

    /** True if the position belongs to a sheet, i.e. x and z are not negative. */
    public static boolean isOnSheet(int x, int z) {
        return x >= 0 && z >= 0;
    }

    /** Index of the sheet that contains the position, or {@link #NO_SHEET}. */
    public static int sheetIndex(int x, int z) {
        return isOnSheet(x, z) ? x / SHEET_WIDTH : NO_SHEET;
    }

    /** x coordinate relative to the western border of the sheet (absolute x modulo 32). */
    public static int relativeX(int x) {
        return Math.floorMod(x, SHEET_WIDTH);
    }

    /** Absolute x coordinate of a relative x coordinate on a sheet. */
    public static int absoluteX(int sheet, int relativeX) {
        if (sheet < 0) {
            throw new IllegalArgumentException("Sheet index must not be negative: " + sheet);
        }
        if (relativeX < 0 || relativeX >= SHEET_WIDTH) {
            throw new IllegalArgumentException("Relative x must be between 0 and 31: " + relativeX);
        }
        return sheet * SHEET_WIDTH + relativeX;
    }

    /** True if the position is in the code area of a sheet (relative x from 5 to 28, z ≥ 0). */
    public static boolean isInCodeArea(int x, int z) {
        int relativeX = relativeX(x);
        return isOnSheet(x, z) && relativeX >= CODE_MIN_X && relativeX <= CODE_MAX_X;
    }

    /** The block of the floor at an absolute position. */
    public static FloorBlock floorAt(int x, int z) {
        if (!isOnSheet(x, z)) {
            return FloorBlock.BLACK;
        }
        int relativeX = relativeX(x);
        if (relativeX == CURSOR_X) {
            return FloorBlock.CURSOR;
        }
        if (relativeX == 0 || relativeX == SHEET_WIDTH - 1) {
            return FloorBlock.BLACK;
        }
        boolean whiteLine = z % 2 == 0;
        if (!whiteLine) {
            return FloorBlock.GREEN;
        }
        // Holes of the tractor feed in the margins of the white lines
        if (relativeX == 2 || relativeX == SHEET_WIDTH - 3) {
            return FloorBlock.BLACK;
        }
        return FloorBlock.WHITE;
    }
}
