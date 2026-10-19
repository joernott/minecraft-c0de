package de.joernott.c0de.core.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class C0deConfigTest {
    @TempDir
    Path dir;

    @Test
    void defaultsMatchTheConcept() {
        C0deConfig config = C0deConfig.defaults();
        assertEquals(255, config.get(C0deConfig.MAX_BUILD_HEIGHT));
        assertEquals(2048, config.get(C0deConfig.STACK_SIZE));
        assertEquals(64, config.get(C0deConfig.SAY_RADIUS));
        assertFalse(config.get(C0deConfig.ALLOW_INVENTORY_CHANGES));
        assertFalse(config.get(C0deConfig.ALLOW_BLOCK_CHANGES));
        assertFalse(config.get(C0deConfig.ALLOW_SPAWNING));
        assertFalse(config.get(C0deConfig.ALLOW_TELEPORTING));
    }

    @Test
    void missingFileIsCreatedWithDefaults() throws IOException {
        C0deConfig config = C0deConfig.load(dir.resolve("config"));
        Path file = dir.resolve("config").resolve(C0deConfig.FILE_NAME);
        assertTrue(Files.exists(file));
        assertTrue(config.problems().isEmpty());
        String text = Files.readString(file, StandardCharsets.UTF_8);
        assertTrue(text.contains("stackSize=2048"));
        assertTrue(text.contains("# Default: 64"));
    }

    @Test
    void writtenFileIsReadBackUnchanged() throws IOException {
        StringWriter out = new StringWriter();
        C0deConfig.defaults().write(out);
        C0deConfig config = read(out.toString());
        assertTrue(config.problems().isEmpty(), config.problems().toString());
        assertEquals(255, config.get(C0deConfig.MAX_BUILD_HEIGHT));
    }

    @Test
    void validValuesAreRead() throws IOException {
        C0deConfig config = read("""
                maxBuildHeight=511
                stackSize=4096
                sayRadius=16
                allowBlockChanges=TRUE
                allowTeleporting = true
                """);
        assertTrue(config.problems().isEmpty(), config.problems().toString());
        assertEquals(511, config.get(C0deConfig.MAX_BUILD_HEIGHT));
        assertEquals(4096, config.get(C0deConfig.STACK_SIZE));
        assertEquals(16, config.get(C0deConfig.SAY_RADIUS));
        assertTrue(config.get(C0deConfig.ALLOW_BLOCK_CHANGES));
        assertTrue(config.get(C0deConfig.ALLOW_TELEPORTING));
        assertFalse(config.get(C0deConfig.ALLOW_SPAWNING));
    }

    @Test
    void invalidValuesFallBackToDefaults() throws IOException {
        C0deConfig config = read("""
                maxBuildHeight=300
                stackSize=100
                sayRadius=many
                allowSpawning=yes
                """);
        assertEquals(255, config.get(C0deConfig.MAX_BUILD_HEIGHT));
        assertEquals(2048, config.get(C0deConfig.STACK_SIZE));
        assertEquals(64, config.get(C0deConfig.SAY_RADIUS));
        assertFalse(config.get(C0deConfig.ALLOW_SPAWNING));
        assertEquals(4, config.problems().size(), config.problems().toString());
    }

    @Test
    void maxBuildHeightMustBeAMultipleOf16Minus1() {
        assertEquals(15, C0deConfig.MAX_BUILD_HEIGHT.parse("15"));
        assertEquals(2031, C0deConfig.MAX_BUILD_HEIGHT.parse("2031"));
        assertProblem("maxBuildHeight=16");
        assertProblem("maxBuildHeight=2047");
    }

    @Test
    void unknownSettingsAreReported() throws IOException {
        C0deConfig config = read("colour=blue\n");
        assertEquals(1, config.problems().size());
        assertTrue(config.problems().get(0).contains("colour"));
    }

    private static void assertProblem(String line) {
        try {
            assertFalse(read(line).problems().isEmpty(), line);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private static C0deConfig read(String text) throws IOException {
        C0deConfig config = C0deConfig.defaults();
        config.read(new StringReader(text));
        return config;
    }
}
