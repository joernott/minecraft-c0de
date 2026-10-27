package de.joernott.c0de.core.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.joernott.c0de.core.cursor.CursorLibraryTest;
import de.joernott.c0de.core.error.ErrorCode;
import de.joernott.c0de.core.library.LibraryRegistry;
import de.joernott.c0de.core.library.standard.StandardLibraries;
import de.joernott.c0de.core.line.Notation;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/** Static validation of programs (task 7.2). */
class ValidatorTest {
    private static final LibraryRegistry LIBRARIES =
            StandardLibraries.registry().add(CursorLibraryTest.TEST);

    private static Validation validate(String program) {
        return Validator.validate(Notation.parse(program), LIBRARIES);
    }

    /** The error codes of the invalid lines. */
    private static Map<Integer, ErrorCode> codes(Validation validation) {
        Map<Integer, ErrorCode> codes = new java.util.TreeMap<>();
        validation.problems().forEach((z, problem) -> codes.put(z, problem.code()));
        return codes;
    }

    static Stream<String> examples() throws IOException, URISyntaxException {
        try (Stream<Path> files =
                Files.list(Path.of(ValidatorTest.class.getResource("/examples").toURI()))) {
            return files.map(file -> file.getFileName().toString()).sorted().toList().stream();
        }
    }

    private static String states(String program) {
        Validation validation = validate(program);
        StringBuilder states = new StringBuilder();
        for (int z = 0; z < validation.endLine(); z++) {
            states.append(
                    switch (validation.state(z)) {
                        case VALID -> 'V';
                        case INCOMPLETE -> 'I';
                        case INVALID -> 'X';
                    });
        }
        return states.toString();
    }

    /** Line numbers (task 11.3): V valid, I incomplete statement, X wrong line. */
    @Test
    void lineStates() {
        String function = """
                PROGRAM states
                NOOP
                FUNCTION run
                    RESULT result INTEGER
                BEGIN
                    ASSIGN result 1 ;
                END FUNCTION
                """;
        assertEquals("VVVVVVV", states(function));
        // A BEGIN without END: the statement is not finished
        assertEquals("VVIIII", states(function.replace("END FUNCTION\n", "")));
        // A wrong line inside a BEGIN without END stays wrong (task 11.2.21)
        assertEquals("VVIIIX", states(function.replace("END FUNCTION\n", "").replace("1 ;", "1")));
        // A FUNCTION without code block at the end of the program
        assertEquals("VVII", states(function.substring(0, function.indexOf("BEGIN"))));
        // A wrong line in a function: the line is wrong, the other lines of the function are incomplete
        assertEquals("VVIIIIX", states(function.replace("END FUNCTION", "END WHILE")));
        // A wrong line on its own
        assertEquals("VVVVVXV", states(function.replace("ASSIGN result 1 ;", "ASSIGN unknown 1 ;")));
    }

    /** INPUT needs a variable that can be changed (task 11.5.1). */
    @Test
    void inputNeedsAVariable() {
        Validation validation = validate("""
                PROGRAM reading
                NOOP
                CONSTANT LIMIT INTEGER 3
                NOOP
                FUNCTION main
                BEGIN
                    VARIABLE count INTEGER
                    CALL INPUT
                        PARAMETER variable count ;
                    CALL INPUT
                        PARAMETER variable LIMIT ;
                    CALL PRINT
                        PARAMETER value count ;
                END FUNCTION
                """);
        assertEquals(Map.of(10, ErrorCode.SYNTAX_ERROR_CALL), codes(validation));
    }

    /** The examples crash only at runtime, if at all, so they are statically valid. */
    @ParameterizedTest
    @MethodSource("examples")
    void examplesAreValid(String name) throws IOException {
        String text;
        try (InputStream in = ValidatorTest.class.getResourceAsStream("/examples/" + name)) {
            text = new String(in.readAllBytes(), StandardCharsets.UTF_8).replace("\r\n", "\n");
        }
        Validation validation = validate(text.substring(text.indexOf("\n---\n") + 5));
        assertEquals(Map.of(), validation.problems(), name);
    }

    @Test
    void validProgram() {
        Validation validation = validate("""
                PROGRAM demo
                NOOP
                INCLUDE c0de/math
                VARIABLE total INTEGER
                NOOP
                FUNCTION twice
                    PARAMETER value INTEGER
                    RESULT result INTEGER
                BEGIN
                    ASSIGN result value * 2 ;
                END FUNCTION
                NOOP
                FUNCTION main
                BEGIN
                    VARIABLE problem ERROR
                    CALL twice problem
                        PARAMETER value 21 ;
                        RESULT result total
                    CALL c0de/math/ABS
                        PARAMETER value c0de/math/PI ;
                        RESULT result total
                END FUNCTION
                """);
        assertTrue(validation.isValid(), () -> validation.problems().toString());
        assertEquals("demo", validation.programName());
        assertEquals(22, validation.endLine());
        assertEquals(
                List.of(
                        new Validation.Function("twice", 5, List.of("value"), List.of("value"), List.of("result")),
                        new Validation.Function("main", 12, List.of(), List.of(), List.of())),
                validation.functions());
    }

    @Test
    void syntaxErrorsMarkAllLinesOfTheStatement() {
        Validation validation = validate("""
                PROGRAM demo
                NOOP
                FUNCTION main
                BEGIN
                    IF TRUE
                    BEGIN
                        ASSIGN
                END FUNCTION
                """);
        // The BEGIN of the IF has no END, so the parser skips the rest of the function
        assertFalse(validation.isValid(4));
        assertFalse(validation.isValid(6));
        assertTrue(validation.isValid(0));
        assertTrue(validation.isValid(1));
    }

    @Test
    void namesAreCheckedLikeTheCursorDoes() {
        Validation validation = validate("""
                PROGRAM demo
                NOOP
                INCLUDE c0de/nothing
                CONSTANT limit INTEGER 10
                VARIABLE limit INTEGER
                NOOP
                FUNCTION main
                BEGIN
                    ASSIGN limit 1 ;
                    ASSIGN unknown 1 ;
                    CALL later
                    CALL c0de/strings/LEFT
                        PARAMETER text "abc" ;
                    CALL LEN
                        PARAMETER size 1 ;
                        RESULT length limit
                    FOR i INTEGER 1 ; 2 ;
                    BEGIN
                        ASSIGN i 3 ;
                    END FOR
                END FUNCTION
                NOOP
                FUNCTION later
                BEGIN
                    CALL later
                END FUNCTION
                """);
        assertEquals(
                Map.ofEntries(
                        Map.entry(2, ErrorCode.UNKNOWN_LIBRARY),
                        Map.entry(4, ErrorCode.SYNTAX_ERROR_VARIABLE),
                        Map.entry(8, ErrorCode.SYNTAX_ERROR_ASSIGN),
                        Map.entry(9, ErrorCode.UNKNOWN_NAME),
                        Map.entry(10, ErrorCode.UNKNOWN_FUNCTION),
                        Map.entry(11, ErrorCode.UNKNOWN_FUNCTION),
                        Map.entry(13, ErrorCode.FUNCTION_MISSING_INPUT),
                        Map.entry(14, ErrorCode.SYNTAX_ERROR_CALL),
                        Map.entry(15, ErrorCode.SYNTAX_ERROR_CALL),
                        Map.entry(18, ErrorCode.SYNTAX_ERROR_ASSIGN)),
                codes(validation));
        // The recursive call of the function itself is allowed
        assertTrue(validation.isValid(24));
    }

    @Test
    void visibleNames() {
        String program = """
                PROGRAM demo
                NOOP
                INCLUDE c0de/math
                STRUCT point
                BEGIN
                    x INTEGER
                END STRUCT
                VARIABLE origin STRUCT point
                NOOP
                FUNCTION main
                    PARAMETER count INTEGER
                BEGIN
                    VARIABLE local INTEGER
                    FOR i INTEGER 1 ; count ;
                    BEGIN
                        ASSIGN local i ;
                    END FOR
                    ASSIGN local 0 ;
                END FUNCTION
                """;
        Validator.Visible inLoop = Validator.visibleAt(Notation.parse(program), LIBRARIES, 15);
        List<String> names = inLoop.symbols().stream().map(Symbol::name).toList();
        assertTrue(names.containsAll(List.of("origin", "count", "local", "i", "c0de/math/PI", "DIVISION_BY_ZERO")));
        assertTrue(inLoop.function("main").isPresent(), "recursion");
        assertTrue(inLoop.function("c0de/math/SIN").isPresent());
        assertTrue(inLoop.function("LEN").isPresent());
        assertEquals(List.of("SHEET_INFO", "PID_INFO", "point"), inLoop.structTypes());

        List<String> afterLoop = Validator.visibleAt(Notation.parse(program), LIBRARIES, 17).symbols().stream()
                .map(Symbol::name)
                .toList();
        assertFalse(afterLoop.contains("i"));
        assertTrue(afterLoop.contains("local"));

        List<String> global = Validator.visibleAt(Notation.parse(program), LIBRARIES, 7).symbols().stream()
                .map(Symbol::name)
                .toList();
        assertFalse(global.contains("origin"), "a declaration doesn't see itself");
    }

    /** While a function is written, its END is missing, but its names are listed anyway. */
    @Test
    void namesOfAFunctionWithoutEnd() {
        String program = """
                PROGRAM demo
                NOOP
                FUNCTION main
                    PARAMETER count INTEGER
                    RESULT total INTEGER
                BEGIN
                    VARIABLE local INTEGER
                    FOR i INTEGER 1 ; count ;
                    BEGIN
                        VARIABLE inner INTEGER
                    END FOR
                    ASSIGN
                """;
        Validator.Visible visible = Validator.visibleAt(Notation.parse(program), LIBRARIES, 11);
        List<String> names = visible.symbols().stream().map(Symbol::name).toList();
        assertTrue(names.containsAll(List.of("count", "total", "local")), names::toString);
        assertFalse(names.contains("i") || names.contains("inner"), names::toString);
        assertTrue(visible.function("main").isPresent(), "recursion");
        Symbol total = visible.symbols().stream()
                .filter(symbol -> symbol.name().equals("total"))
                .findFirst()
                .orElseThrow();
        assertTrue(total.isWritable());
    }
}
