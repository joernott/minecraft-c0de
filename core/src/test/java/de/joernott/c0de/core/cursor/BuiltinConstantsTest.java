package de.joernott.c0de.core.cursor;

import static de.joernott.c0de.core.cursor.CursorTestSupport.finished;
import static de.joernott.c0de.core.cursor.CursorTestSupport.integer;
import static de.joernott.c0de.core.cursor.CursorTestSupport.program;
import static de.joernott.c0de.core.cursor.CursorTestSupport.run;
import static de.joernott.c0de.core.cursor.CursorTestSupport.settings;
import static de.joernott.c0de.core.cursor.CursorTestSupport.string;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.joernott.c0de.core.error.ErrorCode;
import de.joernott.c0de.core.library.LibraryRegistry;
import de.joernott.c0de.core.library.standard.BuiltinConstants;
import de.joernott.c0de.core.line.Notation;
import de.joernott.c0de.core.validation.Validation;
import de.joernott.c0de.core.validation.Validator;
import de.joernott.c0de.core.value.BooleanValue;
import de.joernott.c0de.core.value.Value;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** The built-in constants SHEET, PID and COLOR_BLACK to COLOR_WHITE (tasks 13.4 to 13.6). */
class BuiltinConstantsTest {
    private static final String READ = """
            FUNCTION main
                RESULT number INTEGER
                RESULT owner STRING
                RESULT pid INTEGER
                RESULT activator STRING
                RESULT debug BOOLEAN
            BEGIN
                ASSIGN number SHEET.number ;
                ASSIGN owner SHEET.owner ;
                ASSIGN pid PID.pid ;
                ASSIGN activator PID.activator ;
                ASSIGN debug PID.debug ;
            END FUNCTION
            """;

    private static Map<String, Value> process() {
        return Map.of(
                "SHEET", BuiltinConstants.sheet(7, "demo", "Tester1", 224, -4, 0),
                "PID",
                        BuiltinConstants.pid(
                                3,
                                "demo",
                                "main",
                                "Tester1",
                                "Tester2",
                                10,
                                64,
                                -5,
                                "minecraft:overworld",
                                true,
                                1200));
    }

    private static Map<Integer, ErrorCode> problems(String text) {
        Validation validation =
                Validator.validate(Notation.parse(CursorTestSupport.HEADER + text), new LibraryRegistry());
        Map<Integer, ErrorCode> codes = new java.util.TreeMap<>();
        validation.problems().forEach((z, problem) -> codes.put(z, problem.code()));
        return codes;
    }

    @Test
    void withoutProcessSheetAndPidHaveDefaultValues() {
        Map<String, Value> results = finished(run(READ, "main"));
        assertEquals(integer(0), results.get("number"));
        assertEquals(string(""), results.get("owner"));
        assertEquals(integer(0), results.get("pid"));
        assertEquals(BooleanValue.of(false), results.get("debug"));
    }

    @Test
    void theSettingsGiveSheetAndPidTheirValues() {
        CursorSettings settings = settings(new LibraryRegistry()).withConstants(process());
        Cursor cursor = new Cursor(program(READ), "main", Map.of(), settings);
        cursor.run(1_000);
        Map<String, Value> results = finished(cursor);
        assertEquals(integer(7), results.get("number"));
        assertEquals(string("Tester1"), results.get("owner"));
        assertEquals(integer(3), results.get("pid"));
        assertEquals(string("Tester2"), results.get("activator"));
        assertEquals(BooleanValue.of(true), results.get("debug"));
    }

    @Test
    void sheetCanBeCopiedIntoAVariableOfItsType() {
        String text = """
                FUNCTION main
                    RESULT x INTEGER
                BEGIN
                    VARIABLE sheet STRUCT SHEET_INFO
                    VARIABLE process STRUCT PID_INFO
                    ASSIGN sheet SHEET ;
                    ASSIGN process PID ;
                    ASSIGN sheet.x sheet.x + process.started ;
                    ASSIGN x sheet.x ;
                END FUNCTION
                """;
        assertEquals(Map.of(), problems(text));
        Cursor cursor = new Cursor(
                program(text), "main", Map.of(), settings(new LibraryRegistry()).withConstants(process()));
        cursor.run(1_000);
        assertEquals(integer(1424), finished(cursor).get("x"));
    }

    @Test
    void colorsHaveTheNumbersOfMinecraftText() {
        Map<String, Value> results = finished(run("""
                FUNCTION main
                    RESULT black INTEGER
                    RESULT red INTEGER
                    RESULT white INTEGER
                BEGIN
                    ASSIGN black COLOR_BLACK ;
                    ASSIGN red COLOR_RED ;
                    ASSIGN white COLOR_WHITE ;
                END FUNCTION
                """, "main"));
        assertEquals(integer(0), results.get("black"));
        assertEquals(integer(12), results.get("red"));
        assertEquals(integer(15), results.get("white"));
        assertEquals(16, BuiltinConstants.COLORS.size());
    }

    @Test
    void sheetAndPidAreReadOnly() {
        Map<Integer, ErrorCode> codes = problems("""
                FUNCTION main
                BEGIN
                    ASSIGN SHEET.owner "me" ;
                    ASSIGN PID PID ;
                END FUNCTION
                """);
        assertEquals(Map.of(4, ErrorCode.SYNTAX_ERROR_ASSIGN, 5, ErrorCode.SYNTAX_ERROR_ASSIGN), codes);
    }

    @Test
    void theNamesAreReserved() {
        Map<Integer, ErrorCode> codes = problems("""
                VARIABLE PID INTEGER
                NOOP
                FUNCTION main
                BEGIN
                    VARIABLE COLOR_RED INTEGER
                END FUNCTION
                """);
        assertTrue(codes.containsKey(2), () -> "problems: " + codes);
        assertTrue(codes.containsKey(6), () -> "problems: " + codes);
    }

    @Test
    void snapshotsKeepTheValuesOfTheSettings() {
        String text = """
                FUNCTION main
                    RESULT pid INTEGER
                BEGIN
                    ASSIGN pid 1 ;
                    ASSIGN pid PID.pid ;
                END FUNCTION
                """;
        CursorSettings settings = settings(new LibraryRegistry()).withConstants(process());
        Cursor cursor = new Cursor(program(text), "main", Map.of(), settings);
        cursor.step();
        Cursor restored = CursorSnapshot.restore(CursorSnapshot.save(cursor), program(text), settings);
        restored.run(1_000);
        assertEquals(integer(3), finished(restored).get("pid"));
    }
}
