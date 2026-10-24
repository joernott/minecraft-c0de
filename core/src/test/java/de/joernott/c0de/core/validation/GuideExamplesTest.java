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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * The complete programs of the player guide and the handbooks (docs/guide, docs/reference: the code blocks that
 * start with PROGRAM) are valid.
 * Programs that include c0de/minecraft are only checked apart from that library, which is part of the mod.
 */
class GuideExamplesTest {
    private static final LibraryRegistry LIBRARIES =
            StandardLibraries.registry().add(CursorLibraryTest.TEST);
    private static final Pattern CODE = Pattern.compile("```c0de\\n(.*?)```", Pattern.DOTALL);

    static Stream<String> programs() throws IOException {
        List<String> programs = new ArrayList<>();
        for (Path folder : List.of(Path.of("..", "docs", "guide"), Path.of("..", "docs", "reference"))) {
            List<Path> files;
            try (Stream<Path> listed = Files.list(folder)) {
                files = listed.filter(file -> file.toString().endsWith(".md"))
                        .sorted()
                        .toList();
            }
            for (Path file : files) {
                Matcher matcher = CODE.matcher(
                        Files.readString(file, StandardCharsets.UTF_8).replace("\r\n", "\n"));
                while (matcher.find()) {
                    if (matcher.group(1).startsWith("PROGRAM")) {
                        programs.add(file.getFileName() + ": " + matcher.group(1));
                    }
                }
            }
        }
        assertFalse(programs.isEmpty(), "The guide has no complete programs");
        return programs.stream();
    }

    @ParameterizedTest
    @MethodSource("programs")
    void programsAreValid(String example) {
        String program = example.substring(example.indexOf(": ") + 2);
        Validation validation = Validator.validate(Notation.parse(program), LIBRARIES);
        if (program.contains("INCLUDE c0de/minecraft")) {
            // The library is part of the mod: only its inclusion and its functions are unknown here
            List<ErrorCode> unknown = List.of(ErrorCode.UNKNOWN_LIBRARY, ErrorCode.UNKNOWN_FUNCTION);
            validation
                    .problems()
                    .values()
                    .forEach(problem -> assertTrue(unknown.contains(problem.code()), example + "\n" + problem));
            return;
        }
        assertEquals(Map.of(), validation.problems(), example);
    }
}
