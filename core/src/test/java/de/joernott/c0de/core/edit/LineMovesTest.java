package de.joernott.c0de.core.edit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

/** Moving lines with the wrench (task 9.4) and deleting them with the hammer (task 9.5). */
class LineMovesTest {
    @Test
    void moveUp() {
        // Lines 5 and 6 in front of line 2: 5→2, 6→3, 2→4, 3→5, 4→6
        assertEquals(
                Map.of(5, 2, 6, 3, 2, 4, 3, 5, 4, 6), LineMoves.move(5, 6, 2).orElseThrow());
    }

    @Test
    void moveDown() {
        // Lines 2 and 3 in front of line 6: 2→4, 3→5, 4→2, 5→3
        assertEquals(Map.of(2, 4, 3, 5, 4, 2, 5, 3), LineMoves.move(2, 3, 6).orElseThrow());
    }

    @Test
    void targetsInsideOrDirectlyAfterTheMovedLinesChangeNothing() {
        assertTrue(LineMoves.move(2, 3, 2).isEmpty());
        assertTrue(LineMoves.move(2, 3, 3).isEmpty());
        assertTrue(LineMoves.move(2, 3, 4).isEmpty());
    }

    @Test
    void delete() {
        assertEquals(Map.of(3, LineMoves.DELETED, 4, LineMoves.DELETED, 5, 3, 6, 4), LineMoves.delete(3, 4, 7));
    }
}
