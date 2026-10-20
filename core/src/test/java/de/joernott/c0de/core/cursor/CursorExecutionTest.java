package de.joernott.c0de.core.cursor;

import static de.joernott.c0de.core.cursor.CursorTestSupport.finished;
import static de.joernott.c0de.core.cursor.CursorTestSupport.integer;
import static de.joernott.c0de.core.cursor.CursorTestSupport.run;
import static de.joernott.c0de.core.cursor.CursorTestSupport.string;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.joernott.c0de.core.library.LibraryRegistry;
import de.joernott.c0de.core.value.FloatValue;
import de.joernott.c0de.core.value.Value;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Example programs for the statements of the concept (functions, declarations, control flow). */
class CursorExecutionTest {
    @Test
    void forLoopSumsUp() {
        Map<String, Value> results = finished(run("""
                NOOP
                FUNCTION sum
                    PARAMETER count INTEGER 4
                    RESULT total INTEGER
                BEGIN
                    FOR i INTEGER 1 ; count ;
                    BEGIN
                        ASSIGN total total + i ;
                    END FOR
                END FUNCTION
                """, "sum"));
        assertEquals(Map.of("total", integer(10)), results);
    }

    @Test
    void inputsOverrideDefaults() {
        Map<String, Value> results = finished(run("""
                NOOP
                FUNCTION twice
                    PARAMETER value FLOAT 1
                    RESULT result FLOAT
                BEGIN
                    ASSIGN result value * 2 ;
                END FUNCTION
                """, "twice", Map.of("value", integer(4))));
        assertEquals(new FloatValue(8.0), results.get("result"), "The INTEGER input was converted to FLOAT");
    }

    @Test
    void forLoopsCountDownAndUseFloats() {
        Map<String, Value> results = finished(run("""
                NOOP
                FUNCTION main
                    RESULT down STRING
                    RESULT halves FLOAT
                BEGIN
                    FOR i INTEGER 3 ; 1 ; -1 ;
                    BEGIN
                        ASSIGN down down ⊕ i ;
                    END FOR
                    FOR x FLOAT 0 ; 1 ; 0.5 ;
                    BEGIN
                        ASSIGN halves halves + x ;
                    END FOR
                END FUNCTION
                """, "main"));
        assertEquals(string("321"), results.get("down"));
        assertEquals(new FloatValue(1.5), results.get("halves"));
    }

    @Test
    void conditionsAndConditionalLoops() {
        Map<String, Value> results = finished(run("""
                NOOP
                FUNCTION main
                    RESULT log STRING
                BEGIN
                    VARIABLE i INTEGER
                    WHILE i < 3 ;
                    BEGIN
                        IF i = 1 ;
                        BEGIN
                            ASSIGN log log ⊕ "one" ;
                        END
                        ELSE
                        BEGIN
                            ASSIGN log log ⊕ i ;
                        END IF
                        ASSIGN i i + 1 ;
                    END WHILE
                    REPEAT
                    BEGIN
                        ASSIGN log log ⊕ "r" ;
                    END REPEAT FALSE ;
                    WHILE FALSE ;
                    BEGIN
                        ASSIGN log "never" ;
                    END WHILE
                END FUNCTION
                """, "main"));
        assertEquals(string("0one2r"), results.get("log"));
    }

    @Test
    void eachIteratesOverArraysAndSortedHashKeys() {
        Map<String, Value> results = finished(run("""
                NOOP
                FUNCTION main
                    RESULT log STRING
                BEGIN
                    VARIABLE values ARRAY INTEGER
                    BEGIN
                        3 1 2
                    END VARIABLE
                    VARIABLE stock HASH INTEGER
                    BEGIN
                        gold 1
                        diamond 2
                    END VARIABLE
                    EACH v values
                    BEGIN
                        ASSIGN log log ⊕ v ;
                        ASSIGN values[0] 9 ;
                    END EACH
                    EACH key stock
                    BEGIN
                        ASSIGN log log ⊕ key ⊕ stock[key] ;
                    END EACH
                END FUNCTION
                """, "main"));
        // Changing the array inside the loop doesn't change the iteration
        assertEquals(string("312diamond2gold1"), results.get("log"));
    }

    @Test
    void breakLeavesTheInnermostLoop() {
        Map<String, Value> results = finished(run("""
                NOOP
                FUNCTION main
                    RESULT log STRING
                BEGIN
                    FOR i INTEGER 1 ; 3 ;
                    BEGIN
                        FOR j INTEGER 1 ; 3 ;
                        BEGIN
                            BREAK j > i ;
                            IF j = 3 ;
                            BEGIN
                                BREAK
                            END IF
                            ASSIGN log log ⊕ i ⊕ j ⊕ " " ;
                        END FOR
                    END FOR
                END FUNCTION
                """, "main"));
        assertEquals(string("11 21 22 31 32 "), results.get("log"));
    }

    @Test
    void functionsWithNamedParametersAndResultsAndRecursion() {
        Map<String, Value> results = finished(run("""
                NOOP
                FUNCTION factorial
                    PARAMETER n INTEGER
                    RESULT result INTEGER
                BEGIN
                    ASSIGN result 1 ;
                    IF n > 1 ;
                    BEGIN
                        VARIABLE smaller INTEGER
                        CALL factorial
                            PARAMETER n n - 1 ;
                            RESULT result smaller
                        ASSIGN result n * smaller ;
                    END IF
                END FUNCTION
                NOOP
                FUNCTION divide
                    PARAMETER dividend INTEGER
                    PARAMETER divisor INTEGER 2
                    RESULT even INTEGER
                    RESULT rest INTEGER
                BEGIN
                    ASSIGN rest dividend \\ divisor ;
                    ASSIGN even dividend - rest ;
                END FUNCTION
                NOOP
                FUNCTION main
                    RESULT f INTEGER
                    RESULT q INTEGER
                    RESULT r INTEGER
                BEGIN
                    CALL factorial
                        PARAMETER n 10 ;
                        RESULT result f
                    CALL divide
                        PARAMETER dividend 7 ;
                        RESULT rest r
                        RESULT even q
                END FUNCTION
                """, "main"));
        assertEquals(integer(3628800), results.get("f"));
        assertEquals(integer(6), results.get("q"));
        assertEquals(integer(1), results.get("r"));
    }

    @Test
    void parametersArePassedByValue() {
        Map<String, Value> results = finished(run("""
                NOOP
                FUNCTION change
                    PARAMETER values ARRAY INTEGER
                BEGIN
                    ASSIGN values[0] 99 ;
                END FUNCTION
                NOOP
                FUNCTION main
                    RESULT first INTEGER
                BEGIN
                    VARIABLE values ARRAY INTEGER
                    BEGIN
                        1
                    END VARIABLE
                    CALL change
                        PARAMETER values values ;
                    ASSIGN first values[0] ;
                END FUNCTION
                """, "main"));
        assertEquals(integer(1), results.get("first"));
    }

    @Test
    void globalsAreVisibleWhenDeclaredBeforeTheFunction() {
        Map<String, Value> results = finished(run("""
                CONSTANT FACTOR INTEGER 3
                VARIABLE counter INTEGER
                NOOP
                FUNCTION count
                BEGIN
                    ASSIGN counter counter + FACTOR ;
                END FUNCTION
                NOOP
                FUNCTION main
                    RESULT value INTEGER
                BEGIN
                    CALL count
                    CALL count
                    ASSIGN value counter ;
                END FUNCTION
                """, "main"));
        assertEquals(integer(6), results.get("value"));
    }

    @Test
    void returnEndsTheFunctionWithItsResults() {
        Map<String, Value> results = finished(run("""
                NOOP
                FUNCTION main
                    RESULT value INTEGER
                BEGIN
                    FOR i INTEGER 1 ; 10 ;
                    BEGIN
                        ASSIGN value i ;
                        IF i = 4 ;
                        BEGIN
                            RETURN
                        END IF
                    END FOR
                END FUNCTION
                """, "main"));
        assertEquals(integer(4), results.get("value"));
    }

    @Test
    void exitEndsTheProcessFromNestedFunctions() {
        Cursor cursor = run("""
                NOOP
                FUNCTION stop
                BEGIN
                    EXIT 7 ;
                END FUNCTION
                NOOP
                FUNCTION main
                    RESULT value INTEGER
                BEGIN
                    ASSIGN value 5 ;
                    CALL stop
                    ASSIGN value 6 ;
                END FUNCTION
                """, "main");
        assertEquals(CursorState.FINISHED, cursor.state());
        assertEquals(7, cursor.exitCode());
        assertEquals(Map.of("value", integer(5)), cursor.results());
    }

    @Test
    void localVariablesEndWithTheirCodeBlock() {
        Map<String, Value> results = finished(run("""
                NOOP
                FUNCTION main
                    RESULT value INTEGER
                BEGIN
                    IF TRUE ;
                    BEGIN
                        VARIABLE x INTEGER 1
                    END IF
                    VARIABLE x INTEGER 2
                    ASSIGN value x ;
                END FUNCTION
                """, "main"));
        assertEquals(integer(2), results.get("value"));
    }

    @Test
    void globalsAfterTheEntryFunctionAreNeverInitialized() {
        Cursor cursor = run("""
                NOOP
                FUNCTION main
                BEGIN
                    NOOP
                END FUNCTION
                VARIABLE broken STRUCT unknown
                """, "main");
        finished(cursor);
    }

    @Test
    void oneLinePerStep() {
        Cursor cursor = CursorTestSupport.cursor("""
                VARIABLE a INTEGER
                // a comment line
                NOOP
                FUNCTION main
                BEGIN
                    ASSIGN a 1
                        + 2 ;
                    NOOP
                    ASSIGN a 3 ;
                END FUNCTION
                """, "main", Map.of(), new LibraryRegistry());
        // VARIABLE, FUNCTION (entering the function), ASSIGN, ASSIGN, END FUNCTION
        assertEquals(2, cursor.line() + 1);
        cursor.step();
        assertEquals(5, cursor.line(), "at the FUNCTION line, the comment and NOOP lines are skipped");
        cursor.step();
        assertEquals(7, cursor.line(), "at the first statement of the function");
        cursor.step();
        assertEquals(10, cursor.line(), "the continuation line and the NOOP line are skipped");
        cursor.step();
        assertEquals(11, cursor.line(), "the END FUNCTION line follows");
        cursor.step();
        assertTrue(cursor.ended());
    }

    @Test
    void debugCursorsOnlyMoveWhenStepped() {
        Cursor cursor = CursorTestSupport.cursor("""
                NOOP
                FUNCTION main
                BEGIN
                    NOOP
                END FUNCTION
                """, "main", Map.of(), new LibraryRegistry());
        cursor.pause();
        cursor.step();
        assertEquals(CursorState.PAUSED, cursor.state());
        assertEquals(1, cursor.line());
        cursor.stepOnce();
        assertEquals(6, cursor.line(), "the END line, the NOOP line after BEGIN is skipped");
        cursor.resume();
        cursor.run(10);
        assertFalse(cursor.state() == CursorState.PAUSED);
        finished(cursor);
    }
}
