package de.joernott.c0de.core.program;

import java.util.List;

/**
 * A parsed program.
 *
 * @param name the name of the program, or null if the PROGRAM statement is invalid
 * @param statements the statements of the global scope in the order of their lines, starting with the
 *     INCLUDE statements; errors in the PROGRAM line and the line after it are included as
 *     {@link Statement.Invalid}
 * @param endLine z coordinate of the first empty line, which ends the program
 */
public record Program(String name, List<Statement> statements, int endLine) {
    public Program {
        statements = List.copyOf(statements);
    }

    /** All syntax errors of the program, including those nested in functions, in the order of their lines. */
    public List<Statement.Invalid> errors() {
        return Statements.flatten(statements).stream()
                .filter(Statement.Invalid.class::isInstance)
                .map(Statement.Invalid.class::cast)
                .toList();
    }
}
