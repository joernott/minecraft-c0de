package de.joernott.c0de.core.runner;

import de.joernott.c0de.core.cursor.Cursor;
import de.joernott.c0de.core.cursor.CursorSettings;
import de.joernott.c0de.core.cursor.CursorState;
import de.joernott.c0de.core.error.C0deException;
import de.joernott.c0de.core.error.ErrorCode;
import de.joernott.c0de.core.eval.Declarations;
import de.joernott.c0de.core.eval.TypeRegistry;
import de.joernott.c0de.core.io.Console;
import de.joernott.c0de.core.io.TextScreen;
import de.joernott.c0de.core.library.LibraryProvider;
import de.joernott.c0de.core.library.LibraryRegistry;
import de.joernott.c0de.core.library.standard.JsonValues;
import de.joernott.c0de.core.library.standard.StandardLibraries;
import de.joernott.c0de.core.line.Notation;
import de.joernott.c0de.core.line.Sheet;
import de.joernott.c0de.core.program.Program;
import de.joernott.c0de.core.program.ProgramParser;
import de.joernott.c0de.core.program.Statement;
import de.joernott.c0de.core.validation.Problem;
import de.joernott.c0de.core.validation.Validation;
import de.joernott.c0de.core.validation.Validator;
import de.joernott.c0de.core.value.StructValue;
import de.joernott.c0de.core.value.Type;
import de.joernott.c0de.core.value.Value;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * Runs programs in the text notation of the guide outside of Minecraft, for the tests of the documentation and of
 * the tutorials of c0de.at (task 14.1): it validates the program, runs one function with the parameters of the
 * options, and reports the problems, the results, the error and what the program printed, as a tree that the
 * command line writes as YAML.
 *
 * <p>The library c0de/minecraft is part of the mod: programs that include it are validated, apart from that
 * library, but not run. The libraries of other mods are added by {@link LibraryProvider}s on the class path.
 */
public final class ProgramRunner {
    /** The library of the mod that the runner doesn't have. */
    static final String MINECRAFT = "c0de/minecraft";

    private ProgramRunner() {}

    /**
     * How a program is run.
     *
     * @param function the entry function
     * @param parameters the values of its parameters by name, as YAML trees
     * @param input the values for INPUT, in the order the program asks for them
     * @param keys the keys for INKEY, one character each
     * @param seed the seed of the random number generator
     * @param time the time of the clock, which stands still
     * @param zone the time zone
     * @param columns the width of the screen
     * @param rows the height of the screen
     * @param maxSteps the number of steps after which the program is stopped
     */
    public record Options(
            String function,
            Map<String, Object> parameters,
            List<Object> input,
            String keys,
            long seed,
            Instant time,
            ZoneId zone,
            int columns,
            int rows,
            int maxSteps) {
        public static final Options DEFAULT = new Options(
                "main",
                Map.of(),
                List.of(),
                "",
                0,
                Instant.parse("2026-01-01T12:00:00Z"),
                ZoneId.of("UTC"),
                48,
                20,
                1_000_000);

        public Options {
            parameters = Map.copyOf(parameters);
            input = List.copyOf(input);
        }

        /**
         * Reads the options from a YAML tree; missing keys keep their default values.
         *
         * @throws IllegalArgumentException for unknown keys or values of the wrong kind
         */
        public static Options of(Object tree) {
            if (tree == null) {
                return DEFAULT;
            }
            if (!(tree instanceof Map<?, ?> map)) {
                throw new IllegalArgumentException("The options must be a map");
            }
            Options o = DEFAULT;
            String function = o.function;
            Map<String, Object> parameters = o.parameters;
            List<Object> input = o.input;
            String keys = o.keys;
            long seed = o.seed;
            Instant time = o.time;
            ZoneId zone = o.zone;
            int columns = o.columns;
            int rows = o.rows;
            int maxSteps = o.maxSteps;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                Object value = entry.getValue();
                switch (String.valueOf(entry.getKey())) {
                    case "function" -> function = String.valueOf(value);
                    case "parameters" -> parameters = stringMap(value, "parameters");
                    case "input" -> input = value instanceof List<?> list ? new ArrayList<>(list) : List.of(value);
                    case "keys" -> keys = String.valueOf(value);
                    case "seed" -> seed = number(value, "seed");
                    case "time" -> time = Instant.parse(String.valueOf(value));
                    case "zone" -> zone = ZoneId.of(String.valueOf(value));
                    case "columns" -> columns = (int) number(value, "columns");
                    case "rows" -> rows = (int) number(value, "rows");
                    case "max_steps" -> maxSteps = (int) number(value, "max_steps");
                    default -> throw new IllegalArgumentException("Unknown option: " + entry.getKey());
                }
            }
            return new Options(function, parameters, input, keys, seed, time, zone, columns, rows, maxSteps);
        }

        private static Map<String, Object> stringMap(Object value, String name) {
            if (!(value instanceof Map<?, ?> map)) {
                throw new IllegalArgumentException("The option " + name + " must be a map");
            }
            Map<String, Object> result = new LinkedHashMap<>();
            map.forEach((key, element) -> result.put(String.valueOf(key), element));
            return result;
        }

        private static long number(Object value, String name) {
            if (!(value instanceof Number number)) {
                throw new IllegalArgumentException("The option " + name + " must be a number");
            }
            return number.longValue();
        }
    }

    /** The standard libraries and the libraries of the providers on the class path. */
    public static LibraryRegistry libraries() {
        LibraryRegistry libraries = StandardLibraries.registry();
        ServiceLoader.load(LibraryProvider.class).forEach(provider -> provider.register(libraries));
        return libraries;
    }

    /**
     * Validates and runs a program.
     *
     * @param text the program in the text notation of the guide
     * @return the report: program, function, valid, problems, run (false if the program was only validated),
     *     state, exit_code, results, error, screen, written
     */
    public static Map<String, Object> run(String text, Options options, LibraryRegistry libraries) {
        Map<String, Object> report = new LinkedHashMap<>();
        Sheet sheet = Notation.parse(text, options.zone());
        Program program = ProgramParser.parse(sheet);
        report.put("program", program.name());
        report.put("function", options.function());
        boolean minecraft =
                includes(program, MINECRAFT) && libraries.find(MINECRAFT).isEmpty();
        List<Map<String, Object>> problems = new ArrayList<>();
        Validation validation = Validator.validate(sheet, libraries);
        for (Problem problem : validation.problems().values()) {
            if (minecraft && ofMinecraft(problem)) {
                continue;
            }
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("line", problem.line());
            entry.put("code", problem.code().name());
            entry.put("message", problem.message());
            problems.add(entry);
        }
        report.put("valid", problems.isEmpty());
        report.put("problems", problems);
        if (!problems.isEmpty() || minecraft) {
            report.put("run", false);
            return report;
        }
        Optional<Statement.FunctionDeclaration> entry = function(program, options.function());
        if (entry.isEmpty()) {
            report.put("run", false);
            report.put(
                    "problems",
                    List.of(Map.of(
                            "line",
                            0,
                            "code",
                            "UNKNOWN_FUNCTION",
                            "message",
                            "The function " + options.function() + " doesn't exist")));
            report.put("valid", false);
            return report;
        }
        RunnerConsole console = new RunnerConsole(options);
        CursorSettings settings = new CursorSettings(
                CursorSettings.DEFAULT_STACK_SIZE,
                libraries,
                Clock.fixed(options.time(), options.zone()),
                options.zone(),
                console,
                options.seed());
        Map<String, Value> inputs;
        try {
            inputs = inputs(program, entry.get(), options.parameters(), libraries);
        } catch (IllegalArgumentException e) {
            report.put("run", false);
            report.put(
                    "problems",
                    List.of(Map.of("line", entry.get().line(), "code", "TYPE_MISMATCH", "message", e.getMessage())));
            report.put("valid", false);
            return report;
        }
        Cursor cursor = new Cursor(program, options.function(), inputs, settings);
        int steps = cursor.run(options.maxSteps());
        report.put("run", true);
        report.put("state", cursor.ended() ? cursor.state().name() : "STOPPED");
        report.put("steps", steps);
        if (cursor.ended()) {
            report.put("exit_code", cursor.exitCode());
        }
        if (cursor.state() == CursorState.FINISHED) {
            Map<String, Object> results = new LinkedHashMap<>();
            cursor.results().forEach((name, value) -> results.put(name, JsonValues.tree(value)));
            report.put("results", results);
        }
        if (cursor.state() == CursorState.CRASHED && cursor.error() != null) {
            report.put("error", error(cursor.error()));
        }
        if (!cursor.ended()) {
            report.put(
                    "stopped",
                    cursor.state() == CursorState.WAITING
                            ? "The program waits for a value (INPUT) or for room (WRITE)"
                            : "The program didn't end within " + options.maxSteps() + " steps");
        }
        report.put("screen", console.screen());
        report.put("written", console.written);
        return report;
    }

    /** True if the report is of a program that is valid and ran to its end, or that was only validated. */
    public static boolean succeeded(Map<String, Object> report) {
        if (!Boolean.TRUE.equals(report.get("valid"))) {
            return false;
        }
        return !Boolean.TRUE.equals(report.get("run")) || "FINISHED".equals(report.get("state"));
    }

    /**
     * Compares a report with the expected values: every key of a map in {@code expected} must be in the report
     * with the same value; lists must have the same elements, numbers the same value.
     *
     * @return the differences, empty if the report matches
     */
    public static List<String> compare(Object expected, Object actual) {
        List<String> differences = new ArrayList<>();
        compare("", expected, actual, differences);
        return differences;
    }

    private static void compare(String path, Object expected, Object actual, List<String> differences) {
        if (expected instanceof Map<?, ?> map) {
            if (!(actual instanceof Map<?, ?> actualMap)) {
                differences.add(path + ": expected a map, got " + actual);
                return;
            }
            map.forEach((key, value) -> {
                String child = path.isEmpty() ? String.valueOf(key) : path + "." + key;
                if (!actualMap.containsKey(String.valueOf(key))) {
                    differences.add(child + ": missing, expected " + value);
                } else {
                    compare(child, value, actualMap.get(String.valueOf(key)), differences);
                }
            });
        } else if (expected instanceof List<?> list) {
            if (!(actual instanceof List<?> actualList) || actualList.size() != list.size()) {
                differences.add(path + ": expected " + list + ", got " + actual);
                return;
            }
            for (int i = 0; i < list.size(); i++) {
                compare(path + "[" + i + "]", list.get(i), actualList.get(i), differences);
            }
        } else if (expected instanceof Number number && actual instanceof Number other) {
            if (new BigDecimal(number.toString()).compareTo(new BigDecimal(other.toString())) != 0) {
                differences.add(path + ": expected " + expected + ", got " + actual);
            }
        } else if (!Objects.equals(expected, actual)) {
            differences.add(path + ": expected " + expected + ", got " + actual);
        }
    }

    private static boolean includes(Program program, String library) {
        return program.statements().stream()
                .anyMatch(statement -> statement instanceof Statement.Include include
                        && include.library().equals(library));
    }

    /** Problems that only exist because the runner doesn't have c0de/minecraft. */
    private static boolean ofMinecraft(Problem problem) {
        return problem.code() == ErrorCode.UNKNOWN_LIBRARY || problem.code() == ErrorCode.UNKNOWN_FUNCTION;
    }

    private static Optional<Statement.FunctionDeclaration> function(Program program, String name) {
        return program.statements().stream()
                .filter(Statement.FunctionDeclaration.class::isInstance)
                .map(Statement.FunctionDeclaration.class::cast)
                .filter(function -> function.name().equals(name))
                .findFirst();
    }

    /** The values of the parameters of the entry function, converted to their types like FROM_YAML does. */
    private static Map<String, Value> inputs(
            Program program,
            Statement.FunctionDeclaration function,
            Map<String, Object> parameters,
            LibraryRegistry libraries) {
        TypeRegistry types = structTypes(program, libraries);
        Map<String, Value> inputs = new LinkedHashMap<>();
        for (String name : parameters.keySet()) {
            if (function.parameters().stream()
                    .noneMatch(parameter -> parameter.name().equals(name))) {
                throw new IllegalArgumentException("The function " + function.name() + " has no parameter " + name);
            }
        }
        for (Statement.FunctionDeclaration.Parameter parameter : function.parameters()) {
            if (!parameters.containsKey(parameter.name())) {
                if (parameter.defaultValue() == null) {
                    throw new IllegalArgumentException("The parameter " + parameter.name() + " has no value");
                }
                continue;
            }
            try {
                Type type = types.resolve(parameter.type());
                inputs.put(parameter.name(), JsonValues.read(parameters.get(parameter.name()), type));
            } catch (C0deException e) {
                throw new IllegalArgumentException(
                        "The value of " + parameter.name() + " doesn't fit its type: " + e.getMessage());
            }
        }
        return inputs;
    }

    /** The struct types of the included libraries and of the program, like the trigger block uses them. */
    private static TypeRegistry structTypes(Program program, LibraryRegistry libraries) {
        TypeRegistry types = new TypeRegistry();
        for (Statement statement : program.statements()) {
            if (statement instanceof Statement.Include include) {
                libraries
                        .find(include.library())
                        .ifPresent(library -> library.types()
                                .forEach((name, definition) -> types.add(library.name() + "/" + name, definition)));
            }
        }
        for (Statement statement : program.statements()) {
            if (statement instanceof Statement.StructDeclaration struct
                    && types.find(struct.name()).isEmpty()) {
                try {
                    types.add(struct.name(), Declarations.defineStruct(struct, types));
                } catch (C0deException e) {
                    // The cursor crashes when it reaches the declaration
                }
            }
        }
        return types;
    }

    private static Map<String, Object> error(StructValue error) {
        Map<String, Object> result = new LinkedHashMap<>();
        long code = ((Number) JsonValues.tree(error.get("code"))).longValue();
        result.put("code", code);
        ErrorCode named = code < ErrorCode.FIRST_USER_CODE ? ErrorCode.of((int) code) : null;
        if (named != null) {
            result.put("name", named.name());
        }
        result.put("message", error.get("message").text());
        result.put("context", error.get("context").text());
        result.put("line", JsonValues.tree(error.get("line")));
        return result;
    }

    /** The devices of a program in the runner: one screen, the values for INPUT and the keys for INKEY. */
    private static final class RunnerConsole implements Console {
        private final TextScreen screen;
        private final Deque<Object> input;
        private final Deque<String> keys = new ArrayDeque<>();
        private final List<Map<String, Object>> written = new ArrayList<>();

        RunnerConsole(Options options) {
            screen = new TextScreen(options.columns(), options.rows(), 0);
            input = new ArrayDeque<>(options.input());
            options.keys().codePoints().forEach(key -> keys.add(Character.toString(key)));
        }

        /** The rows of the screen without trailing spaces and without the empty rows at the end. */
        List<String> screen() {
            List<String> rows = new ArrayList<>();
            for (TextScreen.Row row : screen.rows()) {
                rows.add(row.text().stripTrailing());
            }
            while (!rows.isEmpty() && rows.getLast().isEmpty()) {
                rows.removeLast();
            }
            return rows;
        }

        @Override
        public void print(String text) {
            screen.println(text);
        }

        @Override
        public boolean write(String name, Value value) {
            written.add(Map.of(name, JsonValues.tree(value)));
            return true;
        }

        @Override
        public Optional<Value> input(String name, Type type) {
            if (input.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(JsonValues.read(input.poll(), type));
        }

        @Override
        public void cls() {
            screen.clear();
        }

        @Override
        public void locate(int row, int column) {
            screen.locate(row, column);
        }

        @Override
        public int row() {
            return screen.row();
        }

        @Override
        public int column() {
            return screen.column();
        }

        @Override
        public void color(int foreground, int background) {
            screen.color(foreground, background);
        }

        @Override
        public String inkey() {
            return keys.isEmpty() ? "" : keys.poll();
        }
    }
}
