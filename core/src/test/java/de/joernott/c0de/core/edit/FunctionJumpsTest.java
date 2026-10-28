package de.joernott.c0de.core.edit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.joernott.c0de.core.line.Notation;
import java.util.OptionalInt;
import org.junit.jupiter.api.Test;

/** Where boots take a player from a FUNCTION block (task 13.12). */
class FunctionJumpsTest {
    private static final SheetView SHEET = SheetView.of(Notation.parse("""
            PROGRAM demo
            NOOP
            FUNCTION first
            BEGIN
                ASSIGN a 1 ;
            END FUNCTION first
            NOOP
            FUNCTION second
                PARAMETER a INTEGER
            BEGIN
                RETURN
            END FUNCTION
            NOOP
            FUNCTION broken
            BEGIN

            END FUNCTION
            """));

    @Test
    void startsJumpToTheirEnds() {
        assertEquals(OptionalInt.of(5), FunctionJumps.counterpart(SHEET, 5, 2));
        assertEquals(OptionalInt.of(11), FunctionJumps.counterpart(SHEET, 5, 7));
    }

    @Test
    void endsJumpToTheirStarts() {
        assertEquals(OptionalInt.of(2), FunctionJumps.counterpart(SHEET, 6, 5));
        assertEquals(OptionalInt.of(7), FunctionJumps.counterpart(SHEET, 6, 11));
    }

    @Test
    void otherBlocksAndIncompleteFunctionsDontJump() {
        // The END block, the label behind END FUNCTION and a line without FUNCTION
        assertTrue(FunctionJumps.counterpart(SHEET, 5, 5).isEmpty());
        assertTrue(FunctionJumps.counterpart(SHEET, 7, 5).isEmpty());
        assertTrue(FunctionJumps.counterpart(SHEET, 5, 3).isEmpty());
        // The empty line ends the search in both directions
        assertTrue(FunctionJumps.counterpart(SHEET, 5, 13).isEmpty());
        assertTrue(FunctionJumps.counterpart(SHEET, 6, 16).isEmpty());
    }

    @Test
    void aFunctionWithoutEndDoesntJumpToTheNextOne() {
        SheetView sheet = SheetView.of(Notation.parse("""
                PROGRAM demo
                NOOP
                FUNCTION first
                BEGIN
                NOOP
                FUNCTION second
                BEGIN
                END FUNCTION
                """));
        assertTrue(FunctionJumps.counterpart(sheet, 5, 2).isEmpty());
        assertEquals(OptionalInt.of(5), FunctionJumps.counterpart(sheet, 6, 7));
    }
}
