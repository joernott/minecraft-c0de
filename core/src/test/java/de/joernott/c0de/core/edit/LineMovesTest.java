package de.joernott.c0de.core.edit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;

/** Moving and copying the marked lines with the wrench (task 9.4) and deleting lines with the hammer (task 9.5). */
class LineMovesTest {
    private static TreeSet<Integer> marked(Integer... lines) {
        return new TreeSet<>(java.util.List.of(lines));
    }

    @Test
    void moveUp() {
        // Lines 5 and 6 in front of line 2: 5→2, 6→3, 2→4, 3→5, 4→6
        assertEquals(
                Map.of(5, 2, 6, 3, 2, 4, 3, 5, 4, 6),
                LineMoves.move(marked(5, 6), 2).orElseThrow());
    }

    @Test
    void moveDown() {
        // Lines 2 and 3 in front of line 6: 2→4, 3→5, 4→2, 5→3
        assertEquals(
                Map.of(2, 4, 3, 5, 4, 2, 5, 3), LineMoves.move(marked(2, 3), 6).orElseThrow());
    }

    @Test
    void separateMarkedLinesAreMovedTogether() {
        // Lines 1 and 4 in front of line 6: 2→1, 3→2, 5→3, 1→4, 4→5
        assertEquals(
                Map.of(2, 1, 3, 2, 5, 3, 1, 4, 4, 5),
                LineMoves.move(marked(1, 4), 6).orElseThrow());
    }

    @Test
    void markedTargetsAndMovesInPlace() {
        assertTrue(LineMoves.move(marked(2, 3), 3).isEmpty());
        assertTrue(LineMoves.move(marked(), 3).isEmpty());
        // In front of the line directly after them: nothing changes
        assertEquals(Map.of(), LineMoves.move(marked(2, 3), 4).orElseThrow());
    }

    @Test
    void insert() {
        assertEquals(Map.of(4, 6, 5, 7, 6, 8), LineMoves.insert(4, 2, 7));
    }

    @Test
    void delete() {
        assertEquals(Map.of(3, LineMoves.DELETED, 4, LineMoves.DELETED, 5, 3, 6, 4), LineMoves.delete(3, 4, 7));
    }
}
