package de.joernott.c0de.core.validation;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The result of validating the program on a sheet.
 *
 * @param programName the name of the program, or null if the PROGRAM line is invalid
 * @param endLine z coordinate of the first empty line, which ends the program
 * @param problems the first problem of every invalid line, by z coordinate
 * @param functions the functions declared in the program, in the order of their lines
 */
public record Validation(String programName, int endLine, Map<Integer, Problem> problems, List<Function> functions) {
    public Validation {
        problems = Map.copyOf(problems);
        functions = List.copyOf(functions);
    }

    /** True if the line belongs to the program and has no problem. */
    public boolean isValid(int z) {
        return z >= 0 && z < endLine && !problems.containsKey(z);
    }

    /** The state of a line, shown by the color of its line number. */
    public enum LineState {
        /** The line belongs to the program and has no problem. */
        VALID,
        /** The line is correct so far, but its statement is not finished or has a problem in another line. */
        INCOMPLETE,
        /** The line has a problem, or it is behind the end of the program. */
        INVALID
    }

    /** The state of a line. */
    public LineState state(int z) {
        if (z < 0 || z >= endLine) {
            return LineState.INVALID;
        }
        Problem problem = problems.get(z);
        if (problem == null) {
            return LineState.VALID;
        }
        return problem.incomplete() || problem.line() != z ? LineState.INCOMPLETE : LineState.INVALID;
    }

    /** The problem of a line. */
    public Optional<Problem> problem(int z) {
        return Optional.ofNullable(problems.get(z));
    }

    /** The validation with an additional problem, e.g. a program name that is used by another sheet. */
    public Validation withProblem(Problem problem) {
        Map<Integer, Problem> all = new java.util.HashMap<>(problems);
        all.putIfAbsent(problem.line(), problem);
        return new Validation(programName, endLine, all, functions);
    }

    /** True if the program has no problems. */
    public boolean isValid() {
        return problems.isEmpty();
    }

    /**
     * A function of the program.
     *
     * @param name the name
     * @param line z coordinate of the FUNCTION line
     * @param parameters the names of the parameters
     * @param mandatory the names of the parameters without default value
     * @param results the names of the results
     */
    public record Function(
            String name, int line, List<String> parameters, List<String> mandatory, List<String> results) {
        public Function {
            parameters = List.copyOf(parameters);
            mandatory = List.copyOf(mandatory);
            results = List.copyOf(results);
        }
    }
}
