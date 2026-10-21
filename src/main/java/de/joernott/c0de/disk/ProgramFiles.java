package de.joernott.c0de.disk;

import de.joernott.c0de.core.program.Names;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

/**
 * The YAML files of the c0de disk in the folder {@code c0de/programs} of the world. File names are program names,
 * which consist only of letters, digits and underscores, so they can't leave the folder.
 */
public final class ProgramFiles {
    private static final String EXTENSION = ".yaml";

    private ProgramFiles() {}

    private static Path folder(MinecraftServer server) {
        return server.getWorldPath(LevelResource.ROOT).resolve("c0de").resolve("programs");
    }

    private static Path file(MinecraftServer server, String program) {
        if (!Names.isValid(program)) {
            throw new IllegalArgumentException("Not a program name: " + program);
        }
        return folder(server).resolve(program + EXTENSION);
    }

    /** The names of the programs in the folder, sorted. */
    public static List<String> list(MinecraftServer server) throws IOException {
        Path folder = folder(server);
        if (!Files.isDirectory(folder)) {
            return List.of();
        }
        try (Stream<Path> files = Files.list(folder)) {
            return files.map(file -> file.getFileName().toString())
                    .filter(name -> name.endsWith(EXTENSION))
                    .map(name -> name.substring(0, name.length() - EXTENSION.length()))
                    .filter(Names::isValid)
                    .sorted()
                    .toList();
        }
    }

    public static boolean exists(MinecraftServer server, String program) {
        return Files.exists(file(server, program));
    }

    public static String read(MinecraftServer server, String program) throws IOException {
        return Files.readString(file(server, program), StandardCharsets.UTF_8);
    }

    public static void write(MinecraftServer server, String program, String yaml) throws IOException {
        Path file = file(server, program);
        Files.createDirectories(file.getParent());
        Files.writeString(file, yaml, StandardCharsets.UTF_8);
    }
}
