package de.joernott.c0de.client;

import de.joernott.c0de.core.program.Names;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.client.Minecraft;

/**
 * The folder {@code c0de} in the Minecraft folder of the player (task 11.11.9): the disk drive saves programs there
 * as YAML files and loads them from there, like Create does with its schematics. File names are program names,
 * which must be valid names (task 15.7), so a name can neither hide what it is nor reach outside the folder.
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

    /** The names of the programs in the folder, sorted; files whose names aren't valid program names are left out. */
    public static List<String> list() throws IOException {
        try (Stream<Path> files = Files.list(folder())) {
            return files.map(file -> file.getFileName().toString())
                    .filter(name -> name.endsWith(EXTENSION))
                    .map(name -> name.substring(0, name.length() - EXTENSION.length()))
                    .filter(Names::isValid)
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

    /**
     * The file of a program. Only valid program names are accepted (letters, digits and _, starting with a letter),
     * and the file must lie directly in the folder.
     */
    private static Path file(String program) throws IOException {
        if (!Names.isValid(program)) {
            throw new IOException("Not a valid program name: " + Names.shown(program));
        }
        Path folder = folder().toAbsolutePath().normalize();
        Path file = folder.resolve(program + EXTENSION).normalize();
        if (!folder.equals(file.getParent())) {
            throw new IOException("Not a valid program name: " + Names.shown(program));
        }
        return file;
    }
}
