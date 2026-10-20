package de.joernott.c0de.core.cursor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.joernott.c0de.core.error.ErrorCode;
import de.joernott.c0de.core.line.Block;
import de.joernott.c0de.core.line.Notation;
import de.joernott.c0de.core.program.Program;
import de.joernott.c0de.core.program.ProgramParser;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.Value;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Runs the example programs in {@code src/test/resources/examples}, one per chapter of the concept. Every
 * file starts with a header (entry function, inputs, expected exit code, results or crash), followed by
 * {@code ---} and the program in the notation of {@link Notation}:
 *
 * <pre>
 * entry: main
 * inputs: n=10, name="x"
 * expect exit: 0
 * expect total: 55
 * expect crash: DIVISION_BY_ZERO line 9
 * ---
 * PROGRAM ...
 * </pre>
 */
class ExamplesTest {
    static Stream<String> examples() throws IOException, URISyntaxException {
        Path directory = Path.of(ExamplesTest.class.getResource("/examples").toURI());
        try (Stream<Path> files = Files.list(directory)) {
            return files
                    .map(f -> f.getFileName().toString())
                    .filter(n -> n.endsWith(".c0de"))
                    .sorted()
                    .toList()
                    .stream();
        }
    }

    @ParameterizedTest
    @MethodSource("examples")
    void example(String name) throws IOException {
        String text;
        try (InputStream in = ExamplesTest.class.getResourceAsStream("/examples/" + name)) {
            text = new String(in.readAllBytes(), StandardCharsets.UTF_8).replace("\r\n", "\n");
        }
        int separator = text.indexOf("\n---\n");
        assertTrue(separator > 0, "missing --- in " + name);
        String entry = "main";
        Map<String, Value> inputs = new LinkedHashMap<>();
        Map<String, Value> expected = new LinkedHashMap<>();
        Integer exitCode = null;
        String crash = null;
        for (String line : text.substring(0, separator).split("\n")) {
            if (line.isBlank() || line.startsWith("//")) {
                continue;
            }
            int colon = line.indexOf(':');
            String key = line.substring(0, colon).trim();
            String value = line.substring(colon + 1).trim();
            switch (key) {
                case "entry" -> entry = value;
                case "inputs" -> {
                    for (String input : value.split(",")) {
                        String[] parts = input.trim().split("=", 2);
                        inputs.put(parts[0], literal(parts[1]));
                    }
                }
                case "expect exit" -> exitCode = Integer.parseInt(value);
                case "expect crash" -> crash = value;
                default -> {
                    if (!key.startsWith("expect ")) {
                        throw new IllegalArgumentException("Unknown header " + key + " in " + name);
                    }
                    expected.put(key.substring("expect ".length()), literal(value));
                }
            }
        }
        Program program = ProgramParser.parse(Notation.parse(text.substring(separator + 5)));
        Cursor cursor = new Cursor(program, entry, inputs, CursorTestSupport.settings(CursorLibraryTest.libraries()));
        cursor.run(1_000_000);
        assertTrue(cursor.ended(), name + " didn't end");
        if (crash != null) {
            String[] parts = crash.split(" line ");
            assertEquals(CursorState.CRASHED, cursor.state(), name + ": results " + cursor.results());
            assertEquals(
                    new IntegerValue(ErrorCode.valueOf(parts[0]).code()),
                    cursor.error().get("code"),
                    name + ": " + cursor.error());
            assertEquals(
                    new IntegerValue(Long.parseLong(parts[1])),
                    cursor.error().get("line"),
                    name + ": " + cursor.error());
            return;
        }
        assertEquals(CursorState.FINISHED, cursor.state(), () -> name + " crashed: " + cursor.error());
        if (exitCode != null) {
            assertEquals(exitCode, cursor.exitCode(), name);
        }
        for (Map.Entry<String, Value> result : expected.entrySet()) {
            assertEquals(
                    result.getValue(), cursor.results().get(result.getKey()), name + ": result " + result.getKey());
        }
    }

    private static Value literal(String text) {
        List<Block> blocks = Notation.parseLine(0, text, ZoneOffset.UTC).cells().stream()
                .map(cell -> cell.block())
                .toList();
        return ((Block.LiteralBlock) blocks.get(0)).value();
    }
}
