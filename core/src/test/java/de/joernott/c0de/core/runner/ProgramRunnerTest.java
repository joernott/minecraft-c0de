package de.joernott.c0de.core.runner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.joernott.c0de.core.library.LibraryRegistry;
import de.joernott.c0de.core.library.standard.JsonValues;
import de.joernott.c0de.core.library.standard.StandardLibraries;
import de.joernott.c0de.core.value.StringValue;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** The program runner for the tests of the documentation and the tutorials (task 14.1). */
class ProgramRunnerTest {
    private static final String ADDER = """
            PROGRAM adder
            NOOP
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
            """;

    private static ProgramRunner.Options options(String yaml) {
        return ProgramRunner.Options.of(JsonValues.parseYaml(yaml));
    }

    private static Map<String, Object> run(String program, String yaml) {
        return ProgramRunner.run(program, options(yaml), StandardLibraries.registry());
    }

    @Test
    void runsAFunctionWithItsParameters() {
        Map<String, Object> report = run(ADDER, "function: sum\nparameters:\n  n: 10\n");
        assertEquals(true, report.get("valid"));
        assertEquals(true, report.get("run"));
        assertEquals("FINISHED", report.get("state"));
        assertEquals(0, report.get("exit_code"));
        assertEquals(Map.of("result", 55L), report.get("results"));
        assertTrue(ProgramRunner.succeeded(report));
    }

    @Test
    void reportsTheProblemsOfInvalidPrograms() {
        Map<String, Object> report = run("PROGRAM broken\nNOOP\nNOOP\nFUNCTION main\nBEGIN\nEND IF\n", "");
        assertEquals(false, report.get("valid"));
        assertEquals(false, report.get("run"));
        assertFalse(((List<?>) report.get("problems")).isEmpty());
        assertFalse(ProgramRunner.succeeded(report));
    }

    @Test
    void reportsCrashesWithTheError() {
        Map<String, Object> report = run("""
                PROGRAM errors
                NOOP
                NOOP
                FUNCTION main
                    RESULT result INTEGER
                BEGIN
                    ASSIGN result 1 / 0 ;
                END FUNCTION
                """, "");
        assertEquals("CRASHED", report.get("state"));
        assertEquals(15, report.get("exit_code"));
        @SuppressWarnings("unchecked")
        Map<String, Object> error = (Map<String, Object>) report.get("error");
        assertEquals("DIVISION_BY_ZERO", error.get("name"));
        assertEquals(6L, error.get("line"));
        assertFalse(ProgramRunner.succeeded(report));
    }

    @Test
    void printsOnTheScreenAndReadsInput() {
        Map<String, Object> report = run("""
                PROGRAM talk
                NOOP
                NOOP
                FUNCTION main
                BEGIN
                    VARIABLE name STRING
                    VARIABLE count INTEGER
                    CALL INPUT
                        PARAMETER variable name ;
                    CALL INPUT
                        PARAMETER variable count ;
                    CALL PRINT
                        PARAMETER value "Hello, " ⊕ name ;
                    CALL PRINT
                        PARAMETER value count * 2 ;
                    CALL WRITE
                        PARAMETER value count ;
                END FUNCTION
                """, "input: [Steve, 21]\n");
        assertEquals("FINISHED", report.get("state"));
        assertEquals(List.of("Hello, Steve", "42"), report.get("screen"));
        assertEquals(List.of(Map.of("count", 21L)), report.get("written"));
    }

    @Test
    void stopsProgramsThatWaitForInput() {
        Map<String, Object> report = run("""
                PROGRAM waiting
                NOOP
                NOOP
                FUNCTION main
                BEGIN
                    VARIABLE count INTEGER
                    CALL INPUT
                        PARAMETER variable count ;
                END FUNCTION
                """, "max_steps: 100\n");
        assertEquals("STOPPED", report.get("state"));
        assertTrue(String.valueOf(report.get("stopped")).contains("INPUT"));
        assertFalse(ProgramRunner.succeeded(report));
    }

    @Test
    void theClockAndTheRandomNumbersAreFixed() {
        String program = """
                PROGRAM fixed
                NOOP
                INCLUDE c0de/math
                INCLUDE c0de/time
                NOOP
                FUNCTION main
                    RESULT hour INTEGER
                    RESULT random FLOAT
                BEGIN
                    CALL c0de/time/HOUR
                        RESULT result hour
                    CALL c0de/math/RANDOM
                        RESULT result random
                END FUNCTION
                """;
        Map<String, Object> first = run(program, "time: 2026-11-11T08:30:00Z\nseed: 7\n");
        Map<String, Object> second = run(program, "time: 2026-11-11T08:30:00Z\nseed: 7\n");
        assertEquals(8L, ((Map<?, ?>) first.get("results")).get("hour"));
        assertEquals(first.get("results"), second.get("results"));
    }

    @Test
    void programsWithTheMinecraftLibraryAreOnlyValidated() {
        Map<String, Object> report = run("""
                PROGRAM chime
                NOOP
                INCLUDE c0de/minecraft
                NOOP
                FUNCTION ring
                BEGIN
                    CALL c0de/minecraft/SOUND
                        PARAMETER note 12 ;
                        PARAMETER instrument "bell" ;
                END FUNCTION
                """, "function: ring\n");
        assertEquals(true, report.get("valid"));
        assertEquals(false, report.get("run"));
        assertTrue(ProgramRunner.succeeded(report));
    }

    @Test
    void otherLibrariesAndConstantsCanBeAdded() {
        LibraryRegistry libraries =
                StandardLibraries.registry().addConstant("C0DE_TEST_VERSION", new StringValue("1.2.3"));
        Map<String, Object> report = ProgramRunner.run("""
                PROGRAM other
                NOOP
                NOOP
                FUNCTION main
                    RESULT version STRING
                BEGIN
                    ASSIGN version C0DE_TEST_VERSION ;
                END FUNCTION
                """, ProgramRunner.Options.DEFAULT, libraries);
        assertEquals(Map.of("version", "1.2.3"), report.get("results"));
    }

    @Test
    void parametersMustFitTheFunction() {
        Map<String, Object> wrongType = run(ADDER, "function: sum\nparameters:\n  n: ten\n");
        assertEquals(false, wrongType.get("valid"));
        Map<String, Object> missing = run(ADDER, "function: sum\n");
        assertEquals(false, missing.get("valid"));
        Map<String, Object> unknown = run(ADDER, "function: total\n");
        assertEquals(false, unknown.get("valid"));
        assertThrows(IllegalArgumentException.class, () -> options("colour: red\n"));
    }

    @Test
    void comparesReportsWithExpectedValues() {
        Map<String, Object> report = run(ADDER, "function: sum\nparameters:\n  n: 10\n");
        assertEquals(List.of(), ProgramRunner.compare(JsonValues.parseYaml("results:\n  result: 55\n"), report));
        assertEquals(
                1,
                ProgramRunner.compare(JsonValues.parseYaml("results:\n  result: 56\n"), report)
                        .size());
        assertEquals(
                1,
                ProgramRunner.compare(JsonValues.parseYaml("screen: [x]\n"), report)
                        .size());
    }

    @Test
    void theCommandLinePrintsTheReportAndChecksTheExpectedValues(@TempDir Path folder) throws IOException {
        Path program = Files.writeString(folder.resolve("adder.c0de"), ADDER, StandardCharsets.UTF_8);
        Path options = Files.writeString(folder.resolve("adder.options.yaml"), "function: sum\nparameters: {n: 4}\n");
        Path right = Files.writeString(folder.resolve("right.yaml"), "results: {result: 10}\n");
        Path wrong = Files.writeString(folder.resolve("wrong.yaml"), "results: {result: 11}\n");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        PrintStream stdout = new PrintStream(out, true, StandardCharsets.UTF_8);
        PrintStream stderr = new PrintStream(err, true, StandardCharsets.UTF_8);
        String[] base = {"--options", options.toString(), "--expected"};
        assertEquals(0, Main.run(concat(base, right.toString(), program.toString()), stdout, stderr));
        assertTrue(out.toString(StandardCharsets.UTF_8).contains("result: 10"));
        assertEquals(1, Main.run(concat(base, wrong.toString(), program.toString()), stdout, stderr));
        assertTrue(err.toString(StandardCharsets.UTF_8).contains("results.result"));
        assertEquals(2, Main.run(new String[0], stdout, stderr));
        assertEquals(2, Main.run(new String[] {folder.resolve("missing.c0de").toString()}, stdout, stderr));
    }

    @Test
    void theLayoutHasTheBlocksOfTheMod() {
        Map<String, Object> layout = ProgramRunner.layout("""
                PROGRAM chime
                NOOP
                INCLUDE c0de/minecraft
                NOOP
                STRUCT point
                BEGIN
                    x INTEGER
                END STRUCT
                NOOP
                FUNCTION ring
                BEGIN
                    CALL c0de/minecraft/BEEP
                    VARIABLE n INTEGER 1 + 2
                END FUNCTION
                """, StandardLibraries.registry(), java.time.ZoneOffset.UTC);
        assertEquals("chime", layout.get("program"));
        List<?> lines = (List<?>) layout.get("lines");
        assertEquals(14, lines.size());
        Map<?, ?> noop = (Map<?, ?>) lines.get(1);
        assertEquals("neutral", noop.get("state"));
        Map<?, ?> struct = (Map<?, ?>) lines.get(4);
        assertEquals("c0de:type_struct_block", ((Map<?, ?>) ((List<?>) struct.get("blocks")).get(0)).get("block"));
        Map<?, ?> call = (Map<?, ?>) lines.get(11);
        assertEquals("valid", call.get("state"));
        List<?> variable = (List<?>) ((Map<?, ?>) lines.get(12)).get("blocks");
        assertEquals(Map.of("x", 6, "block", "c0de:variable_block", "text", "VARIABLE"), variable.get(0));
        assertEquals(
                Map.of("x", 10, "block", "c0de:operator_block", "state", "operator=add", "text", "+"), variable.get(4));
        assertEquals("c0de:type_integer_block", ((Map<?, ?>) variable.get(2)).get("block"));
    }

    private static String[] concat(String[] base, String... more) {
        String[] all = new String[base.length + more.length];
        System.arraycopy(base, 0, all, 0, base.length);
        System.arraycopy(more, 0, all, base.length, more.length);
        return all;
    }
}
