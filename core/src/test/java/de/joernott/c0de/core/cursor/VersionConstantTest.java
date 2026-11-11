package de.joernott.c0de.core.cursor;

import static de.joernott.c0de.core.cursor.CursorTestSupport.finished;
import static de.joernott.c0de.core.cursor.CursorTestSupport.run;
import static de.joernott.c0de.core.cursor.CursorTestSupport.string;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import de.joernott.c0de.core.error.ErrorCode;
import de.joernott.c0de.core.library.LibraryRegistry;
import de.joernott.c0de.core.library.standard.StandardLibraries;
import de.joernott.c0de.core.line.Notation;
import de.joernott.c0de.core.validation.Validator;
import de.joernott.c0de.core.value.StringValue;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;

/** The built-in constant C0DE_VERSION and the built-in constants of other mods (task 14.9). */
class VersionConstantTest {
    private static final String READ = """
            FUNCTION main
                RESULT version STRING
            BEGIN
                ASSIGN version C0DE_VERSION ;
            END FUNCTION
            """;

    private static Map<Integer, ErrorCode> problems(String text, LibraryRegistry libraries) {
        Map<Integer, ErrorCode> codes = new TreeMap<>();
        Validator.validate(Notation.parse(CursorTestSupport.HEADER + text), libraries)
                .problems()
                .forEach((z, problem) -> codes.put(z, problem.code()));
        return codes;
    }

    @Test
    void theStandardLibrariesHaveTheVersionOfC0de() {
        Map<String, ?> results = finished(run(READ, "main", Map.of(), StandardLibraries.registry()));
        assertEquals(string(System.getProperty("c0de.version")), results.get("version"));
        assertNotEquals("dev", StandardLibraries.version());
    }

    @Test
    void theVersionIsAValidConstant() {
        assertEquals(Map.of(), problems(READ, StandardLibraries.registry()));
    }

    @Test
    void theVersionCantBeChanged() {
        Map<Integer, ErrorCode> codes = problems("""
                FUNCTION main
                BEGIN
                    ASSIGN C0DE_VERSION "1.0" ;
                END FUNCTION
                """, StandardLibraries.registry());
        assertEquals(Map.of(4, ErrorCode.SYNTAX_ERROR_ASSIGN), codes);
    }

    @Test
    void otherModsAddConstants() {
        LibraryRegistry libraries =
                StandardLibraries.registry().addConstant("C0DE_NET_VERSION", new StringValue("0.2.0"));
        String text = """
                FUNCTION main
                    RESULT version STRING
                BEGIN
                    ASSIGN version C0DE_NET_VERSION ;
                END FUNCTION
                """;
        assertEquals(Map.of(), problems(text, libraries));
        assertEquals(
                string("0.2.0"),
                finished(run(text, "main", Map.of(), libraries)).get("version"));
    }

    @Test
    void withoutTheRegistryTheConstantIsUnknown() {
        assertFalse(problems(READ, new LibraryRegistry()).isEmpty());
    }
}
