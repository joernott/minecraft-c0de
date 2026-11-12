package de.joernott.c0de.core.yaml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.joernott.c0de.core.line.Notation;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

/** The schema of the program files (task 15.5) and the check of loaded files (tasks 15.6 and 15.7). */
class ProgramSchemaTest {
    static Stream<String> examples() throws IOException, URISyntaxException {
        return ProgramYamlTest.examples();
    }

    private static Object load(String yaml) {
        return new Load(LoadSettings.builder().build()).loadFromString(yaml);
    }

    private static Optional<String> check(String yaml) {
        return ProgramSchema.check(load(yaml));
    }

    @ParameterizedTest
    @MethodSource("examples")
    void theExportOfEveryExampleMatchesTheSchema(String name) throws IOException {
        String text;
        try (InputStream in = ProgramSchemaTest.class.getResourceAsStream("/examples/" + name)) {
            text = new String(in.readAllBytes(), StandardCharsets.UTF_8).replace("\r\n", "\n");
        }
        String yaml = ProgramYaml.export(Notation.parse(text.substring(text.indexOf("\n---\n") + 5)));
        assertEquals(Optional.empty(), check(yaml), () -> name + ":\n" + yaml);
    }

    @Test
    void theSchemaUsesOnlyWhatTheCheckKnows() {
        Map<?, ?> schema = (Map<?, ?>) load(ProgramSchema.text());
        assertEquals("https://json-schema.org/draft/2020-12/schema", schema.get("$schema"));
        Map<?, ?> definitions = (Map<?, ?>) schema.get("$defs");
        walk(schema, definitions);
    }

    /** Every keyword is known and every reference has its definition. */
    private static void walk(Object node, Map<?, ?> definitions) {
        if (node instanceof Map<?, ?> map) {
            map.forEach((key, value) -> {
                if (key.equals("$ref")) {
                    String name = String.valueOf(value).substring("#/$defs/".length());
                    assertTrue(definitions.containsKey(name), "no definition " + value);
                }
                if (key.equals("properties") || key.equals("$defs")) {
                    ((Map<?, ?>) value).values().forEach(child -> walk(child, definitions));
                } else {
                    assertTrue(ProgramSchema.KEYWORDS.contains(String.valueOf(key)), "unknown keyword " + key);
                    walk(value, definitions);
                }
            });
        } else if (node instanceof List<?> list) {
            list.forEach(child -> walk(child, definitions));
        }
    }

    @Test
    void namesTheFirstProblemAndItsPath() {
        assertEquals(Optional.of("the file has no code"), check("PROGRAM: test\n"));
        assertEquals(
                Optional.of("the file has the unknown key \"VERSION\""),
                check("PROGRAM: test\nVERSION: 2\ncode: []\n"));
        assertEquals(Optional.of("code must be a list"), check("PROGRAM: test\ncode: NOOP\n"));
        assertEquals(
                Optional.of("code[1].keyword \"GOTO\" is not allowed, it must be one of NOOP, COMMENT, STRUCT, "
                        + "VARIABLE, CONSTANT, FUNCTION, ASSIGN, CALL, RETURN, EXIT, BREAK, IF, FOR, EACH, WHILE, "
                        + "REPEAT"),
                check("PROGRAM: test\ncode:\n- keyword: NOOP\n- keyword: GOTO\n"));
        assertEquals(Optional.of("code[0].code[0] has no expression"), check("""
                        PROGRAM: test
                        code:
                        - keyword: FUNCTION
                          name: main
                          code:
                          - keyword: ASSIGN
                            name: x
                        """));
        assertEquals(Optional.of("code[0].parameters[0] has the unknown key \"target\""), check("""
                        PROGRAM: test
                        code:
                        - keyword: FUNCTION
                          name: main
                          parameters:
                          - keyword: PARAMETER
                            name: count
                            type: INTEGER
                            target: x
                          code: []
                        """));
        assertEquals(
                Optional.of(
                        "INCLUDE[0] \"strings\" is not allowed, it must match ^[A-Za-z][A-Za-z0-9_]*(/[A-Za-z][A-Za-z0-9_]*)+$"),
                check("PROGRAM: test\nINCLUDE: [strings]\ncode: []\n"));
    }

    @Test
    void programNamesCantHideOrLeaveTheFolder() {
        for (String name : List.of(
                "../../config/evil",
                "evil/../../x",
                "C:\\\\evil",
                ".hidden",
                "safe\\u200Bname",
                "safe\\u202Eeman",
                "p\\u0430ypal",
                "tab\\tname",
                "1st",
                "_x",
                "with-dash",
                "",
                "END")) {
            Optional<String> problem = check("PROGRAM: \"" + name + "\"\ncode: []\n");
            assertTrue(problem.isPresent() && problem.get().startsWith("PROGRAM "), () -> name + ": " + problem);
        }
        assertEquals(
                Optional.of("PROGRAM \"safe\\u200Bname\" is not allowed, it must match ^[A-Za-z][A-Za-z0-9_]*$"),
                check("PROGRAM: \"safe\\u200Bname\"\ncode: []\n"),
                "the invisible character is shown");
        assertEquals(Optional.of("PROGRAM \"END\" is reserved"), check("PROGRAM: END\ncode: []\n"));
        assertEquals(Optional.empty(), check("PROGRAM: Door_bell2\ncode: []\n"));
    }

    @Test
    void filesThatDontMatchAreNotLaidOut() {
        IllegalArgumentException e = assertThrows(
                IllegalArgumentException.class, () -> ProgramYaml.layout("PROGRAM: \"../evil\"\ncode: []\n"));
        assertTrue(
                e.getMessage().startsWith("The file doesn't match the format of c0de programs: PROGRAM "),
                e.getMessage());
        assertThrows(
                IllegalArgumentException.class, () -> ProgramYaml.programName("PROGRAM: \"a\\u2066b\"\ncode: []\n"));
    }
}
