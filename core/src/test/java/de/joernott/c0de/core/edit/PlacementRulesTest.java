package de.joernott.c0de.core.edit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.joernott.c0de.core.line.Block;
import de.joernott.c0de.core.line.Keyword;
import de.joernott.c0de.core.line.Notation;
import org.junit.jupiter.api.Test;

/** The rules that are checked when a block is placed (task 7.4). */
class PlacementRulesTest {
    private static final SheetView SHEET = SheetView.of(Notation.parse("""
            PROGRAM demo
            NOOP

            NOOP
            FUNCTION main
                PARAMETER a INTEGER

            BEGIN

            END FUNCTION
            """));

    private static boolean allowed(Keyword keyword, int x, int z) {
        return PlacementRules.check(SHEET, new Block.KeywordBlock(keyword), x, z)
                .isEmpty();
    }

    private static boolean allowed(Block block, int x, int z) {
        return PlacementRules.check(SHEET, block, x, z).isEmpty();
    }

    @Test
    void noopLinesStayEmpty() {
        assertFalse(allowed(new Block.LabelBlock(null), 6, 1));
        assertFalse(allowed(Keyword.NOOP, 7, 0));
        assertTrue(allowed(Keyword.NOOP, 5, 2));
        assertTrue(allowed(Keyword.NOOP, 6, 8));
    }

    @Test
    void functionsStartAtX5AfterANoopLine() {
        assertTrue(allowed(Keyword.FUNCTION, 5, 2));
        assertFalse(allowed(Keyword.FUNCTION, 6, 2));
        assertFalse(allowed(Keyword.FUNCTION, 5, 6));
        // END FUNCTION
        assertTrue(allowed(Keyword.FUNCTION, 6, 9));
    }

    @Test
    void codeBlocksAreNotEmpty() {
        assertFalse(allowed(Keyword.END, 5, 8));
        assertTrue(allowed(Keyword.END, 6, 8));
    }

    @Test
    void parametersAndResultsFollowTheirFunction() {
        // Below the PARAMETER line: another PARAMETER or a RESULT
        assertTrue(allowed(Keyword.PARAMETER, 6, 6));
        assertTrue(allowed(Keyword.RESULT, 6, 6));
        assertFalse(allowed(Keyword.PARAMETER, 5, 6));
        // Not after other lines and not in the middle of a line
        assertFalse(allowed(Keyword.RESULT, 6, 2));
        assertFalse(allowed(Keyword.PARAMETER, 7, 5));
    }
}
