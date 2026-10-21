package de.joernott.c0de.core.yaml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.joernott.c0de.core.cursor.CursorLibraryTest;
import de.joernott.c0de.core.library.standard.StandardLibraries;
import de.joernott.c0de.core.line.Notation;
import de.joernott.c0de.core.line.Sheet;
import de.joernott.c0de.core.validation.Validation;
import de.joernott.c0de.core.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/** Export and import of programs as YAML (tasks 10.4, 10.5 and 10.7). */
class ProgramYamlTest {
    static Stream<String> examples() throws IOException, URISyntaxException {
        try (Stream<Path> files = Files.list(
                Path.of(ProgramYamlTest.class.getResource("/examples").toURI()))) {
            return files.map(file -> file.getFileName().toString()).sorted().toList().stream();
        }
    }

    private static Sheet example(String name) throws IOException {
        try (InputStream in = ProgramYamlTest.class.getResourceAsStream("/examples/" + name)) {
            String text = new String(in.readAllBytes(), StandardCharsets.UTF_8).replace("\r\n", "\n");
            return Notation.parse(text.substring(text.indexOf("\n---\n") + 5));
        }
    }

    /**
     * Every example survives export and import: the imported program is valid and its export is the same as the
     * export of the original.
     */
    @ParameterizedTest
    @MethodSource("examples")
    void examplesSurviveExportAndImport(String name) throws IOException {
        String yaml = ProgramYaml.export(example(name));
        Sheet imported = ProgramYaml.layout(yaml);
        Validation validation =
                Validator.validate(imported, StandardLibraries.registry().add(CursorLibraryTest.TEST));
        assertTrue(validation.isValid(), () -> name + ": " + validation.problems() + "\n" + Notation.format(imported));
        assertEquals(yaml, ProgramYaml.export(imported), name);
    }

    @Test
    void exportOfTheConceptExample() {
        Sheet sheet = Notation.parse("""
                PROGRAM doorbell
                NOOP
                    INCLUDE c0de/time
                CONSTANT RINGS INTEGER 3 // number of beeps
                NOOP
                FUNCTION ring
                    PARAMETER visitor STRING ""
                    RESULT count INTEGER
                BEGIN
                    FOR i INTEGER 1 ; RINGS ;
                    BEGIN
                        CALL c0de/time/SLEEP
                            PARAMETER ticks 10 ;
                        ASSIGN count count + 1 ;
                    END FOR
                    IF visitor ≠ "" ;
                    BEGIN
                        ASSIGN count ( count + 1 ) * 2 ;
                    END
                    ELSE
                    BEGIN
                        NOOP
                    END IF
                END FUNCTION
                """);
        String yaml = ProgramYaml.export(sheet);
        assertTrue(yaml.startsWith("PROGRAM: doorbell\nINCLUDE:\n  - c0de/time\ncode:\n"), yaml);
        assertTrue(yaml.contains("comment: number of beeps"), yaml);
        assertTrue(
                yaml.contains("expression: (count + 1) * 2".replace("(", "( ").replace(")", " )")), yaml);
        assertTrue(yaml.contains("expression: visitor ≠ \"\""), yaml);
        assertEquals("doorbell", ProgramYaml.programName(yaml));
    }

    @Test
    void longExpressionsAreContinued() {
        StringBuilder expression = new StringBuilder("1");
        for (int i = 0; i < 20; i++) {
            expression.append(" + 1");
        }
        String yaml = """
                PROGRAM long
                code:
                  - keyword: FUNCTION
                    name: main
                    results:
                      - keyword: RESULT
                        name: total
                        type: INTEGER
                    code:
                      - keyword: ASSIGN
                        name: total
                        expression: "%s"
                """.formatted(expression);
        Sheet sheet = ProgramYaml.layout(yaml.replace("PROGRAM long", "PROGRAM: long"));
        assertTrue(sheet.line(6).cells().size() > 0 && sheet.line(7).cells().size() > 0, Notation.format(sheet));
        assertTrue(Validator.validate(sheet, StandardLibraries.registry()).isValid(), Notation.format(sheet));
    }

    @Test
    void invalidPrograms() {
        assertThrows(IllegalArgumentException.class, () -> ProgramYaml.export(Notation.parse("""
                PROGRAM broken
                NOOP
                FUNCTION main
                BEGIN
                """)));
        assertThrows(IllegalArgumentException.class, () -> ProgramYaml.layout("just text"));
        assertThrows(
                IllegalArgumentException.class, () -> ProgramYaml.layout("PROGRAM: x\ncode:\n  - keyword: JUMP\n"));
        // 24 nested loops don't fit between x=5 and x=28
        StringBuilder nested = new StringBuilder("PROGRAM: deep\ncode:\n");
        String indent = "  ";
        for (int i = 0; i < 24; i++) {
            nested.append(indent).append("- keyword: WHILE\n");
            nested.append(indent).append("  expression: \"TRUE\"\n");
            nested.append(indent).append("  code:\n");
            indent += "    ";
        }
        nested.append(indent).append("- keyword: BREAK\n");
        IllegalArgumentException e =
                assertThrows(IllegalArgumentException.class, () -> ProgramYaml.layout(nested.toString()));
        assertTrue(e.getMessage().contains("doesn't fit"), e.getMessage());
    }
}
