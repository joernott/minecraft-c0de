package de.joernott.c0de.core.edit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.joernott.c0de.core.line.Notation;
import java.util.OptionalInt;
import org.junit.jupiter.api.Test;

/** Moving lines to the left and the right with the pliers (task 13.13). */
class IndentationTest {
    private static final SheetView SHEET = SheetView.of(Notation.parse("""
            PROGRAM demo
            NOOP
                ASSIGN a 1 ;

            BEGIN
            """));

    @Test
    void linesAtTheLeftEdgeDontMoveLeft() {
        assertEquals(OptionalInt.of(0), Indentation.blocked(SHEET, 0, 1, -1));
        assertEquals(OptionalInt.of(4), Indentation.blocked(SHEET, 2, 4, -1));
        assertTrue(Indentation.blocked(SHEET, 2, 3, -1).isEmpty());
    }

    @Test
    void linesMoveRightUntilTheRightEdge() {
        assertTrue(Indentation.blocked(SHEET, 0, 4, 1).isEmpty());
        SheetView full = (x, z) -> x == 28
                ? new de.joernott.c0de.core.line.Block.KeywordBlock(de.joernott.c0de.core.line.Keyword.NOOP)
                : null;
        assertEquals(OptionalInt.of(0), Indentation.blocked(full, 0, 0, 1));
    }

    @Test
    void emptyLinesHaveNothingToMove() {
        assertTrue(Indentation.isEmpty(SHEET, 3, 3));
        assertTrue(Indentation.isEmpty(SHEET, 5, 9));
        assertFalse(Indentation.isEmpty(SHEET, 2, 3));
    }
}
