package de.joernott.c0de.core.cursor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import de.joernott.c0de.core.library.LibraryRegistry;
import de.joernott.c0de.core.program.Program;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.Value;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Cursors are stored and restored after every step and continue exactly as without interruption. */
class CursorSnapshotTest {
    private static final String PROGRAM = """
                INCLUDE test/lib
            STRUCT pair
            BEGIN
                left INTEGER
                right STRING 5
            END STRUCT
            VARIABLE calls INTEGER
            NOOP
            FUNCTION fib
                PARAMETER n INTEGER
                RESULT result INTEGER
            BEGIN
                ASSIGN calls calls + 1 ;
                IF n < 2 ;
                BEGIN
                    ASSIGN result n ;
                END
                ELSE
                BEGIN
                    VARIABLE a INTEGER
                    VARIABLE b INTEGER
                    CALL fib
                        PARAMETER n n - 1 ;
                        RESULT result a
                    CALL fib
                        PARAMETER n n - 2 ;
                        RESULT result b
                    ASSIGN result a + b ;
                END IF
            END FUNCTION
            NOOP
            FUNCTION main
                PARAMETER start INTEGER
                RESULT log STRING
                RESULT total INTEGER
            BEGIN
                VARIABLE p STRUCT test/lib/POINT
                VARIABLE items HASH STRUCT pair
                BEGIN
                    first
                    BEGIN
                        left 1
                        right "a"
                    END
                END VARIABLE
                FOR i INTEGER start ; 6 ; 2 ;
                BEGIN
                    VARIABLE f INTEGER
                    CALL fib
                        PARAMETER n i ;
                        RESULT result f
                    ASSIGN log log ⊕ f ⊕ "," ;
                END FOR
                EACH key items
                BEGIN
                    ASSIGN total total + items[key].left ;
                END EACH
                VARIABLE n INTEGER
                WHILE n < 2 ;
                BEGIN
                    ASSIGN n n + 1 ;
                    CALL test/lib/WAIT
                        PARAMETER steps 2 ;
                END WHILE
                REPEAT
                BEGIN
                    VARIABLE r INTEGER
                    CALL test/lib/RANDOM
                        RESULT result r
                    ASSIGN p.x p.x + r ;
                    ASSIGN n n - 1 ;
                END REPEAT n > 0 ;
                ASSIGN total total + p.x + calls ;
            END FUNCTION
            """;

    private static final Map<String, Value> INPUTS = Map.of("start", new IntegerValue(2));

    private static LibraryRegistry libraries() {
        return CursorLibraryTest.libraries();
    }

    private static Cursor fresh(Program program) {
        return new Cursor(program, "main", INPUTS, CursorTestSupport.settings(libraries()));
    }

    @Test
    void restoredCursorsContinueExactlyAsWithoutInterruption() {
        Program program = CursorTestSupport.program(PROGRAM);
        Cursor reference = fresh(program);
        int steps = reference.run(100_000);
        assertEquals(CursorState.FINISHED, reference.state(), () -> "error: " + reference.error());
        assertTrue(steps > 50, "the program should take many steps: " + steps);

        for (int k = 0; k <= steps; k++) {
            Cursor cursor = fresh(program);
            cursor.run(k);
            Map<String, Object> data = deepCopy(CursorSnapshot.save(cursor));
            Cursor restored = CursorSnapshot.restore(
                    data, CursorTestSupport.program(PROGRAM), CursorTestSupport.settings(libraries()));
            assertEquals(cursor.line(), restored.line(), "line after " + k + " steps");
            int remaining = restored.run(100_000);
            assertEquals(steps - k, remaining, "remaining steps after " + k + " steps");
            assertEquals(reference.state(), restored.state(), "state after restoring at step " + k);
            assertEquals(reference.exitCode(), restored.exitCode());
            assertEquals(reference.results(), restored.results(), "results after restoring at step " + k);
        }
    }

    @Test
    void crashedCursorsKeepTheirError() {
        Program program = CursorTestSupport.program("NOOP\nFUNCTION main\nBEGIN\n    EXIT 99 ;\nEND FUNCTION\n");
        Cursor cursor = new Cursor(program, "main", Map.of(), CursorTestSupport.settings(new LibraryRegistry()));
        cursor.run(10);
        Cursor restored = CursorSnapshot.restore(
                deepCopy(CursorSnapshot.save(cursor)), program, CursorTestSupport.settings(new LibraryRegistry()));
        assertEquals(CursorState.CRASHED, restored.state());
        assertEquals(cursor.error(), restored.error());
        assertEquals(15, restored.exitCode());
    }

    @Test
    void unknownVersionsAreRejected() {
        Program program = CursorTestSupport.program("NOOP\nFUNCTION main\nBEGIN\n    NOOP\nEND FUNCTION\n");
        Map<String, Object> data = new LinkedHashMap<>(CursorSnapshot.save(fresh(program)));
        data.put("version", 99L);
        assertThrows(
                IllegalArgumentException.class,
                () -> CursorSnapshot.restore(data, program, CursorTestSupport.settings(libraries())));
    }

    /** Copies the tree and checks that it consists only of plain data without nulls. */
    @SuppressWarnings("unchecked")
    private static <T> T deepCopy(T data) {
        assertNotNull(data, "snapshots must not contain null values");
        return switch (data) {
            case Map<?, ?> map -> {
                Map<String, Object> copy = new LinkedHashMap<>();
                map.forEach((key, value) -> copy.put((String) key, deepCopy(value)));
                yield (T) copy;
            }
            case List<?> list -> {
                List<Object> copy = new ArrayList<>();
                list.forEach(value -> copy.add(deepCopy(value)));
                yield (T) copy;
            }
            case String s -> data;
            case Long l -> data;
            case Double d -> data;
            case Boolean b -> data;
            default -> fail("Not plain data: " + data.getClass());
        };
    }
}
