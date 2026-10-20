package de.joernott.c0de.core.cursor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.joernott.c0de.core.error.ErrorCode;
import de.joernott.c0de.core.library.LibraryRegistry;
import de.joernott.c0de.core.line.Notation;
import de.joernott.c0de.core.program.Program;
import de.joernott.c0de.core.program.ProgramParser;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.StringValue;
import de.joernott.c0de.core.value.StructValue;
import de.joernott.c0de.core.value.Value;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Map;

/** Helpers for running programs in tests. */
final class CursorTestSupport {
    static final String HEADER = "PROGRAM demo\nNOOP\n";

    /** A fixed clock: 2026-10-19T12:05:09.250Z. */
    static final Clock CLOCK = Clock.fixed(Instant.ofEpochMilli(1792411509250L), ZoneOffset.UTC);

    private CursorTestSupport() {}

    static Program program(String text) {
        return ProgramParser.parse(Notation.parse(HEADER + text));
    }

    static CursorSettings settings(LibraryRegistry libraries) {
        return new CursorSettings(CursorSettings.DEFAULT_STACK_SIZE, libraries, CLOCK, ZoneId.of("UTC"), null, 42);
    }

    static Cursor cursor(String text, String entry, Map<String, Value> inputs, LibraryRegistry libraries) {
        return new Cursor(program(text), entry, inputs, settings(libraries));
    }

    /** Runs a program until it ends. */
    static Cursor run(String text, String entry, Map<String, Value> inputs, LibraryRegistry libraries) {
        Cursor cursor = cursor(text, entry, inputs, libraries);
        cursor.run(100_000);
        assertTrue(cursor.ended(), "The program didn't end within 100000 steps");
        return cursor;
    }

    static Cursor run(String text, String entry, Map<String, Value> inputs) {
        return run(text, entry, inputs, new LibraryRegistry());
    }

    static Cursor run(String text, String entry) {
        return run(text, entry, Map.of());
    }

    /** Asserts that the process finished with exit code 0 and returns its results. */
    static Map<String, Value> finished(Cursor cursor) {
        assertEquals(CursorState.FINISHED, cursor.state(), () -> "crashed: " + cursor.error());
        return cursor.results();
    }

    /** Asserts that the process crashed with the error code in the line. */
    static void crashed(Cursor cursor, ErrorCode code, int line) {
        assertEquals(CursorState.CRASHED, cursor.state(), "results: " + cursor.results());
        StructValue error = cursor.error();
        assertEquals(new IntegerValue(code.code()), error.get("code"), () -> "error: " + error);
        assertEquals(new IntegerValue(line), error.get("line"), () -> "error: " + error);
        assertEquals(Cursor.CRASH_EXIT_CODE, cursor.exitCode());
    }

    static IntegerValue integer(long value) {
        return new IntegerValue(value);
    }

    static StringValue string(String value) {
        return new StringValue(value);
    }
}
