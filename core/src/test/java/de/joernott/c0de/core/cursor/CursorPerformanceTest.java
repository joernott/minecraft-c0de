package de.joernott.c0de.core.cursor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.joernott.c0de.core.library.standard.StandardLibraries;
import de.joernott.c0de.core.line.Notation;
import de.joernott.c0de.core.line.Sheet;
import de.joernott.c0de.core.validation.Validation;
import de.joernott.c0de.core.validation.Validator;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Performance of the interpreter (task 11.10): long loops, deep recursion and large programs. A process takes one
 * step per tick, so a step must cost far less than a tick (50 ms) even with hundreds of processes. The limits are
 * generous, so that slow CI machines don't fail; the measured times are printed.
 */
class CursorPerformanceTest {
    private static long millisSince(long start) {
        return (System.nanoTime() - start) / 1_000_000;
    }

    /** One million steps of a loop that adds numbers. */
    @Test
    void stepsOfALoopAreCheap() {
        Cursor cursor = CursorTestSupport.cursor("""
                NOOP
                FUNCTION main
                    RESULT sum INTEGER
                BEGIN
                    FOR i INTEGER 1 ; 250000 ;
                    BEGIN
                        ASSIGN sum sum + i ;
                    END FOR
                END FUNCTION
                """, "main", Map.of(), StandardLibraries.registry());
        long start = System.nanoTime();
        int steps = cursor.run(2_000_000);
        long millis = millisSince(start);
        System.out.printf("Loop: %d steps in %d ms, %.2f µs per step%n", steps, millis, millis * 1000.0 / steps);
        assertEquals(CursorState.FINISHED, cursor.state(), () -> "crashed: " + cursor.error());
        assertEquals(
                CursorTestSupport.integer(250000L * 250001 / 2),
                cursor.results().get("sum"));
        assertTrue(millis * 1000.0 / steps < 50, "A step takes more than 50 µs");
    }

    /** A recursion 20000 calls deep, with the largest stack. */
    @Test
    void deepRecursion() {
        Cursor cursor = new Cursor(
                CursorTestSupport.program("""
                        NOOP
                        FUNCTION sum
                            PARAMETER n INTEGER
                            RESULT result INTEGER
                        BEGIN
                            IF n = 0 ;
                            BEGIN
                                RETURN
                            END IF
                            CALL sum
                                PARAMETER n n - 1 ;
                                RESULT result result
                            ASSIGN result result + n ;
                        END FUNCTION
                        """),
                "sum",
                Map.of("n", CursorTestSupport.integer(20_000)),
                CursorTestSupport.settings(StandardLibraries.registry()).withStackSize(1_048_576));
        long start = System.nanoTime();
        int steps = cursor.run(1_000_000);
        long millis = millisSince(start);
        System.out.printf("Recursion: %d steps in %d ms, %.2f µs per step%n", steps, millis, millis * 1000.0 / steps);
        assertEquals(CursorState.FINISHED, cursor.state(), () -> "crashed: " + cursor.error());
        assertEquals(
                CursorTestSupport.integer(20_000L * 20_001 / 2),
                cursor.results().get("result"));
        assertTrue(millis * 1000.0 / steps < 200, "A step of a deep recursion takes more than 200 µs");
    }

    /** A program of 4000 lines is validated after every change of a block. */
    @Test
    void largeProgramsAreValidatedQuickly() {
        StringBuilder text =
                new StringBuilder("PROGRAM large\nNOOP\nNOOP\nFUNCTION main\n    RESULT sum INTEGER\nBEGIN\n");
        for (int i = 0; i < 4000; i++) {
            text.append("    ASSIGN sum sum + ").append(i).append(" ;\n");
        }
        text.append("END FUNCTION\n");
        Sheet sheet = Notation.parse(text.toString());
        Validator.validate(sheet, StandardLibraries.registry());
        long start = System.nanoTime();
        Validation validation = null;
        for (int i = 0; i < 10; i++) {
            validation = Validator.validate(sheet, StandardLibraries.registry());
        }
        long millis = millisSince(start) / 10;
        System.out.printf("Validation of 4000 lines: %d ms%n", millis);
        assertTrue(validation.isValid(), () -> "problems: " + problems(sheet));
        assertTrue(millis < 500, "Validating 4000 lines takes more than 500 ms");
    }

    private static String problems(Sheet sheet) {
        return Validator.validate(sheet, StandardLibraries.registry())
                .problems()
                .toString();
    }
}
