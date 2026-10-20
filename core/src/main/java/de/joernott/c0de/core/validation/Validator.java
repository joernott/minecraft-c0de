package de.joernott.c0de.core.validation;

import de.joernott.c0de.core.library.LibraryRegistry;
import de.joernott.c0de.core.line.Sheet;
import de.joernott.c0de.core.program.Program;
import de.joernott.c0de.core.program.ProgramParser;
import de.joernott.c0de.core.program.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Checks a program without running it (static validation). It reports the syntax errors of the parser and
 * the problems with names the cursor would find, line by line, so that the game can show for every block
 * whether its statement is valid.
 */
public final class Validator {
    private Validator() {}

    /** Validates the program on a sheet. */
    public static Validation validate(Sheet sheet, LibraryRegistry libraries) {
        Program program = ProgramParser.parse(sheet);
        ProgramAnalyzer analyzer = new ProgramAnalyzer(program, libraries, -1);
        analyzer.analyze();
        Map<Integer, Problem> problems = new LinkedHashMap<>();
        analyzer.problems().forEach((z, problem) -> {
            // Problems of the PROGRAM line and the line after it can be reported for lines behind the end
            if (z < Math.max(program.endLine(), 1)) {
                problems.put(z, problem);
            }
        });
        return new Validation(program.name(), program.endLine(), problems, functions(program));
    }

    /**
     * The names that are visible in a line: variables and constants, functions that can be called and struct
     * types. The LABEL dialog lists them.
     */
    public static Visible visibleAt(Sheet sheet, LibraryRegistry libraries, int z) {
        Program program = ProgramParser.parse(sheet);
        ProgramAnalyzer analyzer = new ProgramAnalyzer(program, libraries, z);
        analyzer.analyze();
        ProgramAnalyzer.Context context = analyzer.context();
        List<Symbol> symbols = new ArrayList<>(context.symbols());
        List<Validation.Function> functions = new ArrayList<>();
        if (!context.inFunction()) {
            // A function that can't be parsed yet, e.g. because its END is missing: its names come from its lines
            LineScope.at(sheet, z).ifPresent(function -> {
                symbols.addAll(function.symbols());
                if (function.name() != null && !context.functions().containsKey(function.name())) {
                    functions.add(new Validation.Function(function.name(), -1, List.of(), List.of(), List.of()));
                }
            });
        }
        context.functions()
                .values()
                .forEach(signature -> functions.add(new Validation.Function(
                        signature.name(),
                        signature.line(),
                        signature.parameters(),
                        signature.mandatory(),
                        signature.results())));
        return new Visible(symbols, functions, context.structTypes());
    }

    private static List<Validation.Function> functions(Program program) {
        List<Validation.Function> functions = new ArrayList<>();
        for (Statement statement : program.statements()) {
            if (statement instanceof Statement.FunctionDeclaration function) {
                functions.add(new Validation.Function(
                        function.name(),
                        function.line(),
                        function.parameters().stream()
                                .map(Statement.FunctionDeclaration.Parameter::name)
                                .toList(),
                        function.parameters().stream()
                                .filter(parameter -> parameter.defaultValue() == null)
                                .map(Statement.FunctionDeclaration.Parameter::name)
                                .toList(),
                        function.results().stream()
                                .map(Statement.FunctionDeclaration.Result::name)
                                .toList()));
            }
        }
        return functions;
    }

    /**
     * The names visible in a line.
     *
     * @param symbols the variables and constants, including the constants of libraries and the built-in ones
     * @param functions the functions that can be called (built-in, included and of the program)
     * @param structTypes the names of the struct types
     */
    public record Visible(List<Symbol> symbols, List<Validation.Function> functions, List<String> structTypes) {
        public Visible {
            symbols = List.copyOf(symbols);
            functions = List.copyOf(functions);
            structTypes = List.copyOf(structTypes);
        }

        /** The function with this name, if it can be called. */
        public java.util.Optional<Validation.Function> function(String name) {
            return functions.stream().filter(f -> f.name().equals(name)).findFirst();
        }
    }
}
