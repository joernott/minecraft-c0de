package de.joernott.c0de.core.sheet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SheetGeometryTest {
    /**
     * The two lines of the pattern in docs/images/floor_c0de.svg, relative x 0 to 31.
     * B = black, W = white, G = light green, C = cursor rail.
     */
    private static final String WHITE_LINE = "BWBWCWWWWWWWWWWWWWWWWWWWWWWWWBWB";

    private static final String GREEN_LINE = "BGGGCGGGGGGGGGGGGGGGGGGGGGGGGGGB";

    @Test
    void floorMatchesTheGreenbarPattern() {
        for (int sheet : new int[] {0, 1, 7}) {
            for (int z = 0; z < 6; z++) {
                String expected = z % 2 == 0 ? WHITE_LINE : GREEN_LINE;
                for (int relativeX = 0; relativeX < SheetGeometry.SHEET_WIDTH; relativeX++) {
                    int x = SheetGeometry.absoluteX(sheet, relativeX);
                    assertEquals(
                            expected.charAt(relativeX),
                            symbol(SheetGeometry.floorAt(x, z)),
                            "sheet " + sheet + ", relative x " + relativeX + ", z " + z);
                }
            }
        }
    }

    @Test
    void floorOutsideOfTheSheetsIsBlack() {
        assertEquals(FloorBlock.BLACK, SheetGeometry.floorAt(-1, 0));
        assertEquals(FloorBlock.BLACK, SheetGeometry.floorAt(10, -1));
        assertEquals(FloorBlock.BLACK, SheetGeometry.floorAt(-28, -100));
        // relative x 4 would be the cursor rail on a sheet
        assertEquals(FloorBlock.BLACK, SheetGeometry.floorAt(-28, 5));
    }

    @Test
    void sheetIndexAndRelativeX() {
        assertEquals(0, SheetGeometry.sheetIndex(0, 0));
        assertEquals(0, SheetGeometry.sheetIndex(31, 1000));
        assertEquals(1, SheetGeometry.sheetIndex(32, 0));
        assertEquals(SheetGeometry.NO_SHEET, SheetGeometry.sheetIndex(-1, 0));
        assertEquals(SheetGeometry.NO_SHEET, SheetGeometry.sheetIndex(40, -1));
        assertEquals(5, SheetGeometry.relativeX(37));
        assertEquals(31, SheetGeometry.relativeX(-1));
        assertEquals(69, SheetGeometry.absoluteX(2, 5));
    }

    @Test
    void absoluteXRejectsInvalidArguments() {
        assertThrows(IllegalArgumentException.class, () -> SheetGeometry.absoluteX(-1, 5));
        assertThrows(IllegalArgumentException.class, () -> SheetGeometry.absoluteX(0, 32));
    }

    @Test
    void codeArea() {
        assertFalse(SheetGeometry.isInCodeArea(4, 0), "cursor rail");
        assertTrue(SheetGeometry.isInCodeArea(5, 0));
        assertTrue(SheetGeometry.isInCodeArea(28, 0));
        assertFalse(SheetGeometry.isInCodeArea(29, 0));
        assertTrue(SheetGeometry.isInCodeArea(32 + 5, 12));
        assertFalse(SheetGeometry.isInCodeArea(5, -1));
        assertEquals(24, SheetGeometry.LINE_LENGTH);
    }

    private static char symbol(FloorBlock block) {
        return switch (block) {
            case WHITE -> 'W';
            case GREEN -> 'G';
            case BLACK -> 'B';
            case CURSOR -> 'C';
        };
    }
}
