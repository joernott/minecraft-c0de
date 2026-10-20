package de.joernott.c0de.core.cursor;

import static de.joernott.c0de.core.cursor.CursorTestSupport.crashed;
import static de.joernott.c0de.core.cursor.CursorTestSupport.finished;
import static de.joernott.c0de.core.cursor.CursorTestSupport.integer;
import static de.joernott.c0de.core.cursor.CursorTestSupport.run;
import static de.joernott.c0de.core.cursor.CursorTestSupport.string;
import static org.junit.jupiter.api.Assertions.assertEquals;

import de.joernott.c0de.core.error.ErrorCode;
import de.joernott.c0de.core.library.LibraryRegistry;
import de.joernott.c0de.core.value.FloatValue;
import de.joernott.c0de.core.value.StructValue;
import de.joernott.c0de.core.value.Value;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Error handling: crashes, raised errors and their propagation (docs/concept/15-cursor.md). */
class CursorErrorTest {
    private static final String DIVIDE = """
            NOOP
            FUNCTION divide
                PARAMETER a INTEGER
                PARAMETER b INTEGER
                RESULT result FLOAT
            BEGIN
                ASSIGN result 1.5 ;
                ASSIGN result a / b ;
            END FUNCTION
            NOOP
            FUNCTION middle
                RESULT result FLOAT
            BEGIN
                CALL divide
                    PARAMETER a 1 ;
                    PARAMETER b 0 ;
                    RESULT result result
                ASSIGN result 99 ;
            END FUNCTION
            """;

    @Test
    void raisedErrorsAreCaughtByTheErrorLabel() {
        Map<String, Value> results = finished(run(DIVIDE + """
                NOOP
                FUNCTION main
                    RESULT code INTEGER
                    RESULT context STRING
                    RESULT line INTEGER
                    RESULT value FLOAT
                BEGIN
                    VARIABLE problem ERROR
                    CALL divide problem
                        PARAMETER a 1 ;
                        PARAMETER b 0 ;
                        RESULT result value
                    ASSIGN code problem.code ;
                    ASSIGN context problem.context ;
                    ASSIGN line problem.line ;
                END FUNCTION
                """, "main"));
        assertEquals(integer(ErrorCode.DIVISION_BY_ZERO.code()), results.get("code"));
        assertEquals(string("divide"), results.get("context"));
        assertEquals(integer(9), results.get("line"), "the line of the division");
        assertEquals(new FloatValue(1.5), results.get("value"), "results are returned as with RETURN");
    }

    @Test
    void uncaughtErrorsPropagateAndCrashInTheEntryFunction() {
        Cursor cursor = run(DIVIDE + """
                NOOP
                FUNCTION main
                    RESULT value FLOAT
                BEGIN
                    CALL middle
                        RESULT result value
                    ASSIGN value 7 ;
                END FUNCTION
                """, "main");
        crashed(cursor, ErrorCode.DIVISION_BY_ZERO, 9);
        assertEquals(string("divide"), cursor.error().get("context"), "the context of the original error is kept");
        assertEquals(Map.of(), cursor.results());
    }

    @Test
    void userDefinedErrorsAreReturned() {
        Map<String, Value> results = finished(run("""
                NOOP
                FUNCTION check
                    PARAMETER value INTEGER
                BEGIN
                    VARIABLE failure ERROR
                    IF value < 0 ;
                    BEGIN
                        ASSIGN failure.code 1001 ;
                        ASSIGN failure.message "negative" ;
                        RETURN failure
                    END IF
                END FUNCTION
                NOOP
                FUNCTION main
                    RESULT ok INTEGER
                    RESULT message STRING
                BEGIN
                    VARIABLE problem ERROR
                    CALL check problem
                        PARAMETER value 1 ;
                    ASSIGN ok problem.code ;
                    CALL check problem
                        PARAMETER value -1 ;
                    ASSIGN message problem.message ;
                END FUNCTION
                """, "main"));
        assertEquals(integer(0), results.get("ok"));
        assertEquals(string("negative"), results.get("message"));
    }

    @Test
    void syntaxErrorsCrashOnlyWhenTheCursorReachesThem() {
        String program = """
                NOOP
                FUNCTION broken
                BEGIN
                    ASSIGN 1 ;
                END FUNCTION
                NOOP
                FUNCTION main
                    RESULT value INTEGER
                BEGIN
                    ASSIGN value 1 ;
                %s
                END FUNCTION
                """;
        finished(run(program.formatted("    NOOP"), "main"));
        crashed(run(program.formatted("    CALL broken"), "main"), ErrorCode.SYNTAX_ERROR_ASSIGN, 5);
    }

    @Test
    void crashesInDeclarationsAndStatements() {
        crashed(run("VARIABLE a INTEGER\nVARIABLE a FLOAT\n", "main"), ErrorCode.SYNTAX_ERROR_VARIABLE, 3);
        crashed(run("VARIABLE DIVISION_BY_ZERO INTEGER\n", "main"), ErrorCode.SYNTAX_ERROR_VARIABLE, 2);
        String function = "NOOP\nFUNCTION main\nBEGIN\n%s\nEND FUNCTION\n";
        crashed(
                run(function.formatted("    CONSTANT c INTEGER 1\n    ASSIGN c 2 ;"), "main"),
                ErrorCode.SYNTAX_ERROR_ASSIGN,
                6);
        crashed(
                run(
                        function.formatted("    FOR i INTEGER 1 ; 2 ;\n    BEGIN\n        ASSIGN i 5 ;\n    END FOR"),
                        "main"),
                ErrorCode.SYNTAX_ERROR_ASSIGN,
                7);
        crashed(run(function.formatted("    EXIT 16 ;"), "main"), ErrorCode.INVALID_EXIT_CODE, 5);
        crashed(run(function.formatted("    CALL unknown"), "main"), ErrorCode.UNKNOWN_FUNCTION, 5);
        crashed(
                run(function.formatted("    VARIABLE e INTEGER\n    CALL main e"), "main"),
                ErrorCode.SYNTAX_ERROR_CALL,
                6);
        // Raised in the entry function: the process crashes with the raised error
        crashed(
                run(function.formatted("    VARIABLE x INTEGER\n    ASSIGN x 1 / 0 ;"), "main"),
                ErrorCode.DIVISION_BY_ZERO,
                6);
        crashed(
                run(function.formatted("    VARIABLE x INTEGER\n    ASSIGN x 1 / 2 ;"), "main"),
                ErrorCode.TYPE_MISMATCH,
                6);
        crashed(
                run(function.formatted("    FOR i INTEGER 1 ; 2 ; 0 ;\n    BEGIN\n        NOOP\n    END FOR"), "main"),
                ErrorCode.INVALID_LOOP_STEP,
                5);
    }

    @Test
    void entryFunctionProblems() {
        crashed(run("NOOP\nFUNCTION other\nBEGIN\n    NOOP\nEND FUNCTION\n", "main"), ErrorCode.UNKNOWN_FUNCTION, 7);
        String program = "NOOP\nFUNCTION main\n    PARAMETER count INTEGER\nBEGIN\n    NOOP\nEND FUNCTION\n";
        crashed(run(program, "main"), ErrorCode.FUNCTION_MISSING_INPUT, 3);
        crashed(run(program, "main", Map.of("count", string("x"))), ErrorCode.TYPE_MISMATCH, 3);
        finished(run(program, "main", Map.of("count", integer(1))));
    }

    @Test
    void functionsMustBeDeclaredBeforeTheCall() {
        Cursor cursor = run("""
                NOOP
                FUNCTION first
                BEGIN
                    CALL second
                END FUNCTION
                NOOP
                FUNCTION second
                BEGIN
                    NOOP
                END FUNCTION
                NOOP
                FUNCTION main
                BEGIN
                    CALL first
                END FUNCTION
                """, "main");
        crashed(cursor, ErrorCode.UNKNOWN_FUNCTION, 5);
    }

    @Test
    void endlessRecursionOverflowsTheStack() {
        Cursor cursor = new Cursor(
                CursorTestSupport.program("""
                        NOOP
                        FUNCTION endless
                        BEGIN
                            CALL endless
                        END FUNCTION
                        """),
                "endless",
                Map.of(),
                CursorTestSupport.settings(new LibraryRegistry()).withStackSize(100));
        cursor.run(10_000);
        crashed(cursor, ErrorCode.STACK_OVERFLOW, 5);
    }

    @Test
    void resultsOfTheWrongTypeAreRaisedInTheCaller() {
        Cursor cursor = run("""
                NOOP
                FUNCTION text
                    RESULT result STRING
                BEGIN
                    ASSIGN result "x" ;
                END FUNCTION
                NOOP
                FUNCTION main
                    RESULT code INTEGER
                BEGIN
                    VARIABLE number INTEGER
                    CALL text
                        RESULT result number
                END FUNCTION
                """, "main");
        crashed(cursor, ErrorCode.TYPE_MISMATCH, 13);
        StructValue error = cursor.error();
        assertEquals(string("main"), error.get("context"));
    }
}
