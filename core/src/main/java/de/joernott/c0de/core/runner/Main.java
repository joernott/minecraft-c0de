package de.joernott.c0de.core.runner;

import de.joernott.c0de.core.error.C0deException;
import de.joernott.c0de.core.library.standard.JsonValues;
import de.joernott.c0de.core.library.standard.StandardLibraries;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * The command line of the program runner (task 14.1), the main class of {@code c0de-runner-<version>.jar}:
 *
 * <pre>
 * java -jar c0de-runner.jar [--options options.yaml] [--expected expected.yaml] program.c0de
 * </pre>
 *
 * <p>It prints the report of {@link ProgramRunner} as YAML. The exit code is 0 if the program is valid and ran to
 * its end (or was only validated) and matches the expected values, 1 if not, and 2 for a wrong command line or a
 * file that can't be read. To run programs with the libraries of other mods, put their runner jars on the class
 * path: {@code java -cp c0de-runner.jar:c0de-net-runner.jar de.joernott.c0de.core.runner.Main ...}.
 */
public final class Main {
    private static final String USAGE =
            "Usage: c0de-runner [--options options.yaml] [--expected expected.yaml] program.c0de";

    private Main() {}

    public static void main(String[] args) {
        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        PrintStream err = new PrintStream(System.err, true, StandardCharsets.UTF_8);
        System.exit(run(args, out, err));
    }

    /** Runs the command line and returns the exit code. */
    static int run(String[] args, PrintStream out, PrintStream err) {
        Path options = null;
        Path expected = null;
        Path program = null;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--options", "--expected" -> {
                    if (i + 1 >= args.length) {
                        err.println(USAGE);
                        return 2;
                    }
                    Path file = Path.of(args[++i]);
                    if (args[i - 1].equals("--options")) {
                        options = file;
                    } else {
                        expected = file;
                    }
                }
                case "--version" -> {
                    out.println("c0de-runner " + StandardLibraries.version());
                    return 0;
                }
                case "--help", "-h" -> {
                    out.println(USAGE);
                    return 0;
                }
                default -> {
                    if (program != null || args[i].startsWith("--")) {
                        err.println(USAGE);
                        return 2;
                    }
                    program = Path.of(args[i]);
                }
            }
        }
        if (program == null) {
            err.println(USAGE);
            return 2;
        }
        try {
            ProgramRunner.Options settings =
                    options == null ? ProgramRunner.Options.DEFAULT : ProgramRunner.Options.of(yaml(options));
            Map<String, Object> report = ProgramRunner.run(read(program), settings, ProgramRunner.libraries());
            out.print(JsonValues.writeYaml(report));
            if (expected != null) {
                List<String> differences = ProgramRunner.compare(yaml(expected), report);
                for (String difference : differences) {
                    err.println(program + ": " + difference);
                }
                return differences.isEmpty() ? 0 : 1;
            }
            return ProgramRunner.succeeded(report) ? 0 : 1;
        } catch (IOException | IllegalArgumentException | C0deException e) {
            err.println(program + ": " + e.getMessage());
            return 2;
        }
    }

    private static String read(Path file) throws IOException {
        return Files.readString(file, StandardCharsets.UTF_8).replace("\r\n", "\n");
    }

    private static Object yaml(Path file) throws IOException {
        return JsonValues.parseYaml(read(file));
    }
}
