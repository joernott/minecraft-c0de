package de.joernott.c0de.core.cursor;

import static de.joernott.c0de.core.cursor.CursorTestSupport.crashed;
import static de.joernott.c0de.core.cursor.CursorTestSupport.finished;
import static de.joernott.c0de.core.cursor.CursorTestSupport.integer;
import static de.joernott.c0de.core.cursor.CursorTestSupport.run;
import static de.joernott.c0de.core.cursor.CursorTestSupport.string;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import de.joernott.c0de.core.error.ErrorCode;
import de.joernott.c0de.core.library.Library;
import de.joernott.c0de.core.library.LibraryRegistry;
import de.joernott.c0de.core.library.NativeFunction;
import de.joernott.c0de.core.library.NativeResult;
import de.joernott.c0de.core.value.BooleanValue;
import de.joernott.c0de.core.value.FloatValue;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.StringValue;
import de.joernott.c0de.core.value.StructDefinition;
import de.joernott.c0de.core.value.Type;
import de.joernott.c0de.core.value.Value;
import de.joernott.c0de.core.value.Values;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;

/** Native functions, libraries and the interface to the mod (tasks 4.4 and 4.7). */
class CursorLibraryTest {
    static final StructDefinition POINT = new StructDefinition(
            "POINT",
            List.of(
                    new StructDefinition.Element("x", Type.Simple.INTEGER, Map.of()),
                    new StructDefinition.Element("y", Type.Simple.INTEGER, Map.of())));

    static final Library TEST = new Library() {
        @Override
        public String name() {
            return "test/lib";
        }

        @Override
        public Map<String, StructDefinition> types() {
            return Map.of("POINT", POINT);
        }

        @Override
        public Map<String, Value> constants() {
            return Map.of("ANSWER", new IntegerValue(42));
        }

        @Override
        public Map<String, NativeFunction> functions() {
            return Map.of(
                    "DOUBLE",
                    new NativeFunction(
                            "DOUBLE",
                            List.of(NativeFunction.Parameter.required("value", "INTEGER|FLOAT", Values::isNumber)),
                            List.of("result"),
                            call -> NativeResult.of(
                                    call.argument("value") instanceof IntegerValue i
                                            ? new IntegerValue(i.value() * 2)
                                            : new FloatValue(Values.toDouble(call.argument("value")) * 2))),
                    "TAGS",
                    new NativeFunction(
                            "TAGS",
                            List.of(NativeFunction.Parameter.required("value", "any", value -> true)),
                            List.of("result"),
                            call -> NativeResult.of(new StringValue(new TreeMap<>(call.tags("value")).toString()))),
                    "FILL",
                    new NativeFunction(
                            "FILL",
                            List.of(NativeFunction.Parameter.reference("destination", "INTEGER")),
                            List.of(),
                            call -> {
                                call.reference("destination").orElseThrow().set(new IntegerValue(42));
                                return NativeResult.none();
                            }),
                    "FAIL",
                    new NativeFunction(
                            "FAIL",
                            List.of(NativeFunction.Parameter.withDefault(
                                    "code", "INTEGER", v -> v instanceof IntegerValue, new IntegerValue(1001))),
                            List.of(),
                            call -> NativeResult.error(((IntegerValue) call.argument("code")).value(), "failed")),
                    "WAIT",
                    new NativeFunction(
                            "WAIT",
                            List.of(NativeFunction.Parameter.required(
                                    "steps", "INTEGER", v -> v instanceof IntegerValue)),
                            List.of(),
                            call -> NativeResult.waitSteps((int) ((IntegerValue) call.argument("steps")).value())),
                    "PROVIDED",
                    new NativeFunction(
                            "PROVIDED",
                            List.of(NativeFunction.Parameter.optional("value", "any", v -> true)),
                            List.of("result"),
                            call -> NativeResult.of(BooleanValue.of(call.provided("value")))),
                    "RANDOM",
                    new NativeFunction(
                            "RANDOM",
                            List.of(),
                            List.of("result"),
                            call -> NativeResult.of(
                                    new IntegerValue(call.process().random().nextInt(1000)))));
        }
    };

    static LibraryRegistry libraries() {
        return new LibraryRegistry().add(TEST);
    }

    private static Map<String, Value> runMain(String body, String results) {
        return finished(run(
                "    INCLUDE test/lib\nNOOP\nFUNCTION main\n" + results + "BEGIN\n" + body + "END FUNCTION\n",
                "main",
                Map.of(),
                libraries()));
    }

    @Test
    void typesConstantsAndFunctionsOfLibraries() {
        Map<String, Value> results = runMain("""
                    VARIABLE p STRUCT test/lib/POINT
                    ASSIGN p.x test/lib/ANSWER ;
                    CALL test/lib/DOUBLE
                        PARAMETER value p.x ;
                        RESULT result doubled
                    CALL test/lib/DOUBLE
                        PARAMETER value 1.5 ;
                        RESULT result half
                """, "    RESULT doubled INTEGER\n    RESULT half FLOAT\n");
        assertEquals(integer(84), results.get("doubled"));
        assertEquals(new FloatValue(3.0), results.get("half"));
    }

    @Test
    void tagsAndReferencesArePassed() {
        Map<String, Value> results =
                runMain("""
                    VARIABLE port INTEGER 1 TAG(json: "server.port")
                    CALL test/lib/TAGS
                        PARAMETER value port ;
                        RESULT result tags
                    CALL test/lib/TAGS
                        PARAMETER value port + 1 ;
                        RESULT result none
                    CALL test/lib/FILL
                        PARAMETER destination port ;
                    ASSIGN filled port ;
                """, "    RESULT tags STRING\n    RESULT none STRING\n    RESULT filled INTEGER\n");
        assertEquals(string("{json=server.port}"), results.get("tags"));
        assertEquals(string("{}"), results.get("none"), "Only a single label passes its tags");
        assertEquals(integer(42), results.get("filled"));
    }

    @Test
    void optionalParametersAndReturnedErrors() {
        Map<String, Value> results = runMain(
                """
                    VARIABLE problem ERROR
                    CALL test/lib/PROVIDED
                        RESULT result without
                    CALL test/lib/PROVIDED
                        PARAMETER value 1 ;
                        RESULT result with
                    CALL test/lib/FAIL problem
                    ASSIGN code problem.code ;
                    ASSIGN context problem.context ;
                """,
                "    RESULT without BOOLEAN\n    RESULT with BOOLEAN\n    RESULT code INTEGER\n    RESULT context STRING\n");
        assertEquals(BooleanValue.FALSE, results.get("without"));
        assertEquals(BooleanValue.TRUE, results.get("with"));
        assertEquals(integer(1001), results.get("code"));
        assertEquals(string("test/lib/FAIL"), results.get("context"));
    }

    @Test
    void uncaughtErrorsOfNativeFunctionsCrashTheEntryFunction() {
        Cursor cursor = run(
                "    INCLUDE test/lib\nNOOP\nFUNCTION main\nBEGIN\n    CALL test/lib/FAIL\nEND FUNCTION\n",
                "main",
                Map.of(),
                libraries());
        assertEquals(CursorState.CRASHED, cursor.state());
        assertEquals(integer(1001), cursor.error().get("code"));
        assertEquals(integer(6), cursor.error().get("line"));
        assertEquals(Cursor.CRASH_EXIT_CODE, cursor.exitCode());
    }

    @Test
    void waitingTakesSteps() {
        Cursor cursor = CursorTestSupport.cursor(
                "    INCLUDE test/lib\nNOOP\nFUNCTION main\nBEGIN\n    CALL test/lib/WAIT\n        PARAMETER steps 3 ;\nEND FUNCTION\n",
                "main",
                Map.of(),
                libraries());
        // INCLUDE, FUNCTION, CALL, 3 waiting steps, END FUNCTION
        assertEquals(6, cursor.run(6));
        assertEquals(CursorState.RUNNING, cursor.state());
        cursor.step();
        finished(cursor);
    }

    @Test
    void randomNumbersDependOnTheSeed() {
        Map<String, Value> first = runMain("""
                    CALL test/lib/RANDOM
                        RESULT result a
                    CALL test/lib/RANDOM
                        RESULT result b
                """, "    RESULT a INTEGER\n    RESULT b INTEGER\n");
        Map<String, Value> second = runMain("""
                    CALL test/lib/RANDOM
                        RESULT result a
                    CALL test/lib/RANDOM
                        RESULT result b
                """, "    RESULT a INTEGER\n    RESULT b INTEGER\n");
        assertEquals(first, second, "The same seed gives the same numbers");
        assertNotEquals(first.get("a"), first.get("b"));
    }

    @Test
    void libraryErrors() {
        String function = "NOOP\nFUNCTION main\nBEGIN\n    NOOP\nEND FUNCTION\n";
        crashed(
                run("    INCLUDE test/unknown\n" + function, "main", Map.of(), libraries()),
                ErrorCode.UNKNOWN_LIBRARY,
                2);
        Library broken = new Library() {
            @Override
            public String name() {
                return "test/broken";
            }

            @Override
            public Map<String, NativeFunction> functions() {
                throw new IllegalStateException("not available");
            }
        };
        crashed(
                run("    INCLUDE test/broken\n" + function, "main", Map.of(), new LibraryRegistry().add(broken)),
                ErrorCode.BROKEN_LIBRARY,
                2);
        String call = "    INCLUDE test/lib\nNOOP\nFUNCTION main\nBEGIN\n%s\nEND FUNCTION\n";
        crashed(
                run(call.formatted("    CALL test/lib/DOUBLE"), "main", Map.of(), libraries()),
                ErrorCode.FUNCTION_MISSING_INPUT,
                6);
        crashed(
                run(
                        call.formatted("    CALL test/lib/DOUBLE\n        PARAMETER other 1 ;"),
                        "main",
                        Map.of(),
                        libraries()),
                ErrorCode.SYNTAX_ERROR_CALL,
                6);
        crashed(
                run(
                        call.formatted("    CALL test/lib/DOUBLE\n        PARAMETER value \"x\" ;"),
                        "main",
                        Map.of(),
                        libraries()),
                ErrorCode.TYPE_MISMATCH,
                6);
        crashed(
                run(
                        call.formatted("    CALL test/lib/FILL\n        PARAMETER destination 1 ;"),
                        "main",
                        Map.of(),
                        libraries()),
                ErrorCode.SYNTAX_ERROR_CALL,
                6);
        crashed(
                run(
                        call.formatted("    CALL test/lib/DOUBLE\n        PARAMETER value 1 ;\n        RESULT other x"),
                        "main",
                        Map.of(),
                        libraries()),
                ErrorCode.SYNTAX_ERROR_CALL,
                6);
    }
}
