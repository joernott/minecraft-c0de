package de.joernott.c0de.core.cursor;

import static de.joernott.c0de.core.cursor.CursorTestSupport.finished;
import static de.joernott.c0de.core.cursor.CursorTestSupport.integer;
import static de.joernott.c0de.core.cursor.CursorTestSupport.run;
import static de.joernott.c0de.core.cursor.CursorTestSupport.string;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.joernott.c0de.core.error.C0deException;
import de.joernott.c0de.core.error.ErrorCode;
import de.joernott.c0de.core.library.Library;
import de.joernott.c0de.core.library.LibraryRegistry;
import de.joernott.c0de.core.library.NativeFunction;
import de.joernott.c0de.core.library.NativeResult;
import de.joernott.c0de.core.library.standard.StringsLibrary;
import de.joernott.c0de.core.program.Program;
import de.joernott.c0de.core.value.BooleanValue;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.LibraryType;
import de.joernott.c0de.core.value.LibraryValue;
import de.joernott.c0de.core.value.StructValue;
import de.joernott.c0de.core.value.Type;
import de.joernott.c0de.core.value.Value;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Types defined by libraries (task 19.2, docs/concept/04-data.md "Library type"). */
public class CursorLibraryTypeTest {
    /** A counter: an immutable value, so its copies are trivially cheap. */
    record Counter(long count) implements LibraryValue {
        @Override
        public LibraryType libraryType() {
            return COUNTER;
        }

        @Override
        public Value copy() {
            return this;
        }

        @Override
        public String text() {
            return "COUNTER " + count;
        }
    }

    static final LibraryType COUNTER = new LibraryType() {
        @Override
        public String name() {
            return "test/memo/COUNTER";
        }

        @Override
        public LibraryValue defaultValue() {
            return new Counter(0);
        }

        @Override
        public String save(LibraryValue value) {
            return Long.toString(((Counter) value).count());
        }

        @Override
        public LibraryValue load(String data) {
            return new Counter(Long.parseLong(data));
        }
    };

    public static final Library MEMO = new Library() {
        @Override
        public String name() {
            return "test/memo";
        }

        @Override
        public Map<String, LibraryType> libraryTypes() {
            return Map.of("COUNTER", COUNTER);
        }

        @Override
        public Map<String, NativeFunction> functions() {
            return Map.of(
                    "ADD",
                    new NativeFunction(
                            "ADD",
                            List.of(
                                    NativeFunction.Parameter.reference("counter", "TYPE test/memo/COUNTER"),
                                    NativeFunction.Parameter.withDefault(
                                            "amount", "INTEGER", v -> v instanceof IntegerValue, integer(1))),
                            List.of(),
                            call -> {
                                var counter = call.reference("counter").orElseThrow();
                                long amount = ((IntegerValue) call.argument("amount")).value();
                                counter.set(new Counter(((Counter) counter.value()).count() + amount));
                                return NativeResult.none();
                            }),
                    "COUNT",
                    new NativeFunction(
                            "COUNT",
                            List.of(NativeFunction.Parameter.required(
                                    "counter", "TYPE test/memo/COUNTER", v -> v instanceof Counter)),
                            List.of("result"),
                            call -> NativeResult.of(integer(((Counter) call.argument("counter")).count()))),
                    "WAIT",
                    new NativeFunction("WAIT", List.of(), List.of(), call -> NativeResult.waitSteps(3)),
                    "PARTS",
                    new NativeFunction("PARTS", List.of(), List.of("result"), call -> {
                        // Three parts, one per step; the result is the number of the last part
                        int part = call.state() == null ? 1 : (Integer) call.state() + 1;
                        if (part < 3) {
                            call.setState(part);
                            return NativeResult.waitForValue();
                        }
                        return NativeResult.of(integer(part));
                    }));
        }
    };

    private static LibraryRegistry libraries() {
        return new LibraryRegistry().add(MEMO).add(new StringsLibrary());
    }

    private static String program(String body, String results) {
        return "INCLUDE test/memo\nINCLUDE c0de/strings\nNOOP\nFUNCTION main\n" + results + "BEGIN\n" + body
                + "END FUNCTION\n";
    }

    private static Cursor runMain(String body, String results) {
        return run(program(body, results), "main", Map.of(), libraries());
    }

    private static void crashedWith(Cursor cursor, ErrorCode code) {
        assertEquals(CursorState.CRASHED, cursor.state(), () -> "results: " + cursor.results());
        StructValue error = cursor.error();
        assertEquals(integer(code.code()), error.get("code"), () -> "error: " + error);
    }

    @Test
    void valuesAreDeclaredChangedThroughReferencesAndCopied() {
        Map<String, Value> results = finished(runMain(
                """
                    VARIABLE first TYPE test/memo/COUNTER
                    VARIABLE second TYPE test/memo/COUNTER
                    CALL test/memo/COUNT
                        PARAMETER counter first ;
                        RESULT result initial
                    CALL test/memo/ADD
                        PARAMETER counter first ;
                        PARAMETER amount 3 ;
                    ASSIGN second first ;
                    CALL test/memo/ADD
                        PARAMETER counter first ;
                    CALL test/memo/COUNT
                        PARAMETER counter first ;
                        RESULT result a
                    CALL test/memo/COUNT
                        PARAMETER counter second ;
                        RESULT result b
                    ASSIGN text "is " ⊕ second ;
                """,
                "    RESULT initial INTEGER\n    RESULT a INTEGER\n    RESULT b INTEGER\n    RESULT text STRING\n"));
        assertEquals(integer(0), results.get("initial"), "the default value of the library");
        assertEquals(integer(4), results.get("a"));
        assertEquals(integer(3), results.get("b"), "the copy doesn't change with the original");
        assertEquals(string("is COUNTER 3"), results.get("text"));
    }

    @Test
    void valuesAreComparedWithEqualsButNotOrdered() {
        Map<String, Value> results = finished(runMain("""
                    VARIABLE first TYPE test/memo/COUNTER
                    VARIABLE second TYPE test/memo/COUNTER
                    ASSIGN same first = second ;
                    CALL test/memo/ADD
                        PARAMETER counter first ;
                    ASSIGN different first <> second ;
                """, "    RESULT same BOOLEAN\n    RESULT different BOOLEAN\n"));
        assertEquals(BooleanValue.TRUE, results.get("same"));
        assertEquals(BooleanValue.TRUE, results.get("different"));

        crashedWith(
                runMain(
                        "    VARIABLE c TYPE test/memo/COUNTER\n    ASSIGN wrong c < c ;\n",
                        "    RESULT wrong BOOLEAN\n"),
                ErrorCode.TYPE_MISMATCH);
        crashedWith(
                runMain("    VARIABLE c TYPE test/memo/COUNTER\n    ASSIGN wrong c ;\n", "    RESULT wrong INTEGER\n"),
                ErrorCode.TYPE_MISMATCH);
    }

    @Test
    void parametersResultsArraysAndHashesOfLibraryTypes() {
        String text = """
                INCLUDE test/memo
                NOOP
                FUNCTION increment
                    PARAMETER counter TYPE test/memo/COUNTER
                    RESULT result TYPE test/memo/COUNTER
                BEGIN
                    ASSIGN result counter ;
                    CALL test/memo/ADD
                        PARAMETER counter result ;
                END FUNCTION
                NOOP
                FUNCTION main
                    RESULT count INTEGER
                BEGIN
                    VARIABLE counters ARRAY TYPE test/memo/COUNTER
                    VARIABLE named HASH TYPE test/memo/COUNTER
                    VARIABLE c TYPE test/memo/COUNTER
                    CALL increment
                        PARAMETER counter c ;
                        RESULT result c
                    CALL increment
                        PARAMETER counter c ;
                        RESULT result c
                    ASSIGN counters[0] c ;
                    ASSIGN named["two"] counters[0] ;
                    CALL test/memo/COUNT
                        PARAMETER counter named["two"] ;
                        RESULT result count
                END FUNCTION
                """;
        assertEquals(
                integer(2), finished(run(text, "main", Map.of(), libraries())).get("count"));
    }

    @Test
    void declarationsAreChecked() {
        crashedWith(runMain("    VARIABLE c TYPE test/memo/UNKNOWN\n", ""), ErrorCode.UNKNOWN_STRUCT_ENTITY);
        crashedWith(
                run("NOOP\nFUNCTION main\nBEGIN\n    VARIABLE c TYPE test/memo/COUNTER\nEND FUNCTION\n", "main"),
                ErrorCode.UNKNOWN_STRUCT_ENTITY);
        crashedWith(runMain("    VARIABLE c TYPE test/memo/COUNTER 1\n", ""), ErrorCode.SYNTAX_ERROR_VARIABLE);
        crashedWith(runMain("    CONSTANT c TYPE test/memo/COUNTER\n", ""), ErrorCode.SYNTAX_ERROR_CONSTANT);
    }

    @Test
    void valuesCantBeConvertedToJsonOrYaml() {
        crashedWith(runMain("""
                            VARIABLE c TYPE test/memo/COUNTER
                            CALL c0de/strings/TO_JSON
                                PARAMETER value c ;
                                RESULT result json
                        """, "    RESULT json STRING\n"), ErrorCode.TYPE_MISMATCH);
        crashedWith(runMain("""
                            VARIABLE c TYPE test/memo/COUNTER
                            CALL c0de/strings/FROM_YAML
                                PARAMETER source "1" ;
                                PARAMETER destination c ;
                        """, ""), ErrorCode.TYPE_MISMATCH);
    }

    @Test
    void valuesAreSavedWithTheCursor() {
        String text = program("""
                    VARIABLE c TYPE test/memo/COUNTER
                    CALL test/memo/ADD
                        PARAMETER counter c ;
                        PARAMETER amount 7 ;
                    CALL test/memo/WAIT
                    CALL test/memo/COUNT
                        PARAMETER counter c ;
                        RESULT result count
                """, "    RESULT count INTEGER\n");
        Program program = CursorTestSupport.program(text);
        Cursor cursor = new Cursor(program, "main", Map.of(), CursorTestSupport.settings(libraries()));
        cursor.run(8);
        Map<String, Object> data = CursorSnapshot.save(cursor);

        Cursor restored = CursorSnapshot.restore(data, program, CursorTestSupport.settings(libraries()));
        restored.run(100);
        assertEquals(integer(7), finished(restored).get("count"));

        C0deException missing = assertThrows(
                C0deException.class,
                () -> CursorSnapshot.restore(
                        data, program, CursorTestSupport.settings(new LibraryRegistry().add(new StringsLibrary()))));
        assertSame(ErrorCode.UNKNOWN_LIBRARY, missing.code());
    }

    @Test
    void functionsKeepTheirProgressWhileTheCursorStaysOnTheCall() {
        Map<String, Value> results = finished(runMain("""
                    FOR i INTEGER 1 ; 2 ; 1 ;
                    BEGIN
                        VARIABLE parts INTEGER
                        CALL test/memo/PARTS
                            RESULT result parts
                        ASSIGN log log ⊕ parts ;
                    END FOR
                """, "    RESULT log STRING\n"));
        assertEquals(string("33"), results.get("log"), "the second call starts again with the first part");
    }

    @Test
    void typesAreWrittenWithTheTypeBlock() {
        assertEquals("TYPE test/memo/COUNTER", new Type.Library(COUNTER).toString());
    }
}
