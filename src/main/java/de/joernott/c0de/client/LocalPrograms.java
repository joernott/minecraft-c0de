package de.joernott.c0de.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.client.Minecraft;

/**
 * The folder {@code c0de} in the Minecraft folder of the player (task 11.11.9): the disk drive saves programs there
 * as YAML files and loads them from there, like Create does with its schematics. File names are program names.
 */
public final class LocalPrograms {
    private static final String EXTENSION = ".yaml";

    private LocalPrograms() {}

    /** The folder; it is created if it doesn't exist. */
    public static Path folder() throws IOException {
        Path folder = Minecraft.getInstance().gameDirectory.toPath().resolve("c0de");
        Files.createDirectories(folder);
        return folder;
    }

    /** The names of the programs in the folder, sorted. */
    public static List<String> list() throws IOException {
        try (Stream<Path> files = Files.list(folder())) {
            return files.map(file -> file.getFileName().toString())
                    .filter(name -> name.endsWith(EXTENSION))
                    .map(name -> name.substring(0, name.length() - EXTENSION.length()))
                    .sorted()
                    .toList();
        }
    }

    public static String read(String program) throws IOException {
        return Files.readString(file(program), StandardCharsets.UTF_8);
    }

    /** Saves a program; a file with the same name is replaced. */
    public static Path write(String program, String yaml) throws IOException {
        Path file = file(program);
        Files.writeString(file, yaml, StandardCharsets.UTF_8);
        return file;
    }

    /** The file of a program; names that aren't valid file names are refused. */
    private static Path file(String program) throws IOException {
        if (!program.matches("[A-Za-z0-9_][A-Za-z0-9_-]*")) {
            throw new IOException("Not a valid program name: " + program);
        }
        return folder().resolve(program + EXTENSION);
    }
}
