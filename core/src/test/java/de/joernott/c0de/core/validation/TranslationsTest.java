package de.joernott.c0de.core.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * The translations of the guide and the handbooks (docs/de, tasks 14.3 to 14.5) have the structure of the English
 * files, which the Patchouli books and the website c0de.at rely on: the same headings in the same order and levels
 * (names like {@code ### MID} unchanged), the same programs (only the text of comments is translated), and links
 * whose files and anchors exist.
 */
class TranslationsTest {
    private static final Path DOCS = Path.of("..", "docs");
    private static final List<String> FOLDERS = List.of("guide", "reference");
    private static final Pattern HEADING = Pattern.compile("^(#{1,6}) (.*)$");
    private static final Pattern LINK = Pattern.compile("\\]\\(([^)#\\s]*)(?:#([^)\\s]+))?\\)");
    /** Headings that are names: keywords, functions, types and libraries. */
    private static final Pattern NAME = Pattern.compile("[A-Z0-9_]+|c0de[a-z_]*/[a-z]+");

    static Stream<Path> translations() throws IOException {
        List<Path> files = new ArrayList<>();
        try (Stream<Path> languages = Files.list(DOCS)) {
            for (Path language : languages
                    .filter(path -> path.getFileName().toString().matches("[a-z]{2}"))
                    .toList()) {
                for (String folder : FOLDERS) {
                    Path translated = language.resolve(folder);
                    if (Files.isDirectory(translated)) {
                        try (Stream<Path> listed = Files.list(translated)) {
                            listed.filter(file -> file.toString().endsWith(".md"))
                                    .sorted()
                                    .forEach(files::add);
                        }
                    }
                }
            }
        }
        assertFalse(files.isEmpty(), "There are no translations");
        return files.stream();
    }

    private static Path english(Path translation) {
        return DOCS.resolve(translation.getParent().getFileName().toString())
                .resolve(translation.getFileName().toString());
    }

    private static List<String> lines(Path file) throws IOException {
        return Files.readString(file, StandardCharsets.UTF_8)
                .replace("\r\n", "\n")
                .lines()
                .toList();
    }

    /** The headings outside of code blocks. */
    private static List<String> headings(Path file) throws IOException {
        List<String> result = new ArrayList<>();
        boolean code = false;
        for (String line : lines(file)) {
            if (line.startsWith("```")) {
                code = !code;
            } else if (!code && HEADING.matcher(line).matches()) {
                result.add(line.strip());
            }
        }
        return result;
    }

    /** The programs, with the text of their comments removed. */
    private static List<String> programs(Path file) throws IOException {
        List<String> result = new ArrayList<>();
        StringBuilder program = null;
        for (String line : lines(file)) {
            if (program != null) {
                if (line.startsWith("```")) {
                    result.add(program.toString());
                    program = null;
                } else {
                    int comment = line.indexOf("//");
                    program.append(comment < 0 ? line : line.substring(0, comment + 2))
                            .append('\n');
                }
            } else if (line.startsWith("```")) {
                program = new StringBuilder(line).append('\n');
            }
        }
        return result;
    }

    /** The anchor GitHub gives a heading, with the suffixes of repeated anchors. */
    private static List<String> anchors(Path file) throws IOException {
        List<String> result = new ArrayList<>();
        Map<String, Integer> seen = new HashMap<>();
        for (String heading : headings(file)) {
            String text = heading.replaceFirst("^#+ ", "")
                    .replaceAll("\\[([^\\]]*)\\]\\([^)]*\\)", "$1")
                    .replace("`", "")
                    .toLowerCase(Locale.ROOT)
                    .replaceAll("[^\\p{L}\\p{N}_ -]", "")
                    .replace(' ', '-');
            int count = seen.merge(text, 1, Integer::sum) - 1;
            result.add(count == 0 ? text : text + "-" + count);
        }
        return result;
    }

    @ParameterizedTest
    @MethodSource("translations")
    void theHeadingsMatchTheEnglishFile(Path translation) throws IOException {
        Path original = english(translation);
        assertTrue(Files.exists(original), () -> translation + " has no English file " + original);
        List<String> translated = headings(translation);
        List<String> headings = headings(original);
        assertEquals(headings.size(), translated.size(), () -> translation + ": number of headings");
        for (int i = 0; i < headings.size(); i++) {
            String english = headings.get(i);
            String german = translated.get(i);
            String level = english.substring(0, english.indexOf(' '));
            assertEquals(level, german.substring(0, german.indexOf(' ')), translation + ": level of " + german);
            String name = english.substring(level.length() + 1);
            if (NAME.matcher(name).matches()) {
                assertEquals(english, german, translation + ": names in headings stay unchanged");
            }
        }
    }

    @ParameterizedTest
    @MethodSource("translations")
    void theProgramsAreTheSame(Path translation) throws IOException {
        assertEquals(programs(english(translation)), programs(translation), translation + ": programs");
    }

    @ParameterizedTest
    @MethodSource("translations")
    void theLinksResolve(Path translation) throws IOException {
        Matcher matcher = LINK.matcher(Files.readString(translation, StandardCharsets.UTF_8));
        while (matcher.find()) {
            String target = matcher.group(1);
            if (target.startsWith("http:") || target.startsWith("https:")) {
                continue;
            }
            Path file = target.isEmpty()
                    ? translation
                    : translation.getParent().resolve(target).normalize();
            assertTrue(Files.exists(file), translation + ": the link target " + target + " doesn't exist");
            String anchor = matcher.group(2);
            if (anchor != null && file.toString().endsWith(".md")) {
                assertTrue(
                        anchors(file).contains(anchor),
                        translation + ": the anchor #" + anchor + " doesn't exist in " + target);
            }
        }
    }
}
