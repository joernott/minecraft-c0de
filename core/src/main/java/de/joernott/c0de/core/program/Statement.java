package de.joernott.c0de.core.program;

import de.joernott.c0de.core.error.C0deException;
import de.joernott.c0de.core.value.Type;
import de.joernott.c0de.core.value.Value;
import java.util.List;
import java.util.Map;

/**
 * A statement of a program, produced by the {@link ProgramParser}. Every statement knows the z coordinate
 * of its first line. Lines with syntax errors become {@link Invalid} statements, so that the cursor crashes
 * only when it reaches them.
 */
public sealed interface Statement {
    /** z coordinate of the first line of the statement. */
    int line();

    /** A statement with a syntax error. The cursor crashes with the error when it reaches the line. */
    record Invalid(int line, C0deException error) implements Statement {}

    record Include(int line, String library) implements Statement {}

    /** STRUCT declaration in the global scope. */
    record StructDeclaration(int line, String name, List<Element> elements, int endLine) implements Statement {
        public StructDeclaration {
            elements = List.copyOf(elements);
        }

        /** An element line of a struct declaration. */
        public record Element(int line, String name, TypeSpec type, Map<String, String> tags) {
            public Element {
                tags = Map.copyOf(tags);
            }
        }
    }

    /**
     * VARIABLE or CONSTANT declaration.
     *
     * @param literal the initial value of a simple type or STRING, or null
     * @param tags the tags: language to path, e.g. json to "server.port"
     * @param initializer the code block initializing an ARRAY, HASH or STRUCT, or null
     */
    record Declaration(
            int line,
            boolean constant,
            String name,
            TypeSpec type,
            Value literal,
            Map<String, String> tags,
            Initializer initializer)
            implements Statement {
        public Declaration {
            tags = Map.copyOf(tags);
        }

        /** z coordinate of the last line of the statement. */
        public int endLine() {
            return initializer == null ? line : initializer.endLine();
        }
    }

    /** FUNCTION declaration with its parameters, results and code block. */
    record FunctionDeclaration(int line, String name, List<Parameter> parameters, List<Result> results, CodeBlock body)
            implements Statement {
        public FunctionDeclaration {
            parameters = List.copyOf(parameters);
            results = List.copyOf(results);
        }

        /** A PARAMETER line; the default value is null for mandatory parameters. */
        public record Parameter(int line, String name, TypeSpec type, Value defaultValue) {}

        /** A RESULT line. */
        public record Result(int line, String name, TypeSpec type) {}
    }

    /**
     * ASSIGN statement.
     *
     * @param endLine z coordinate of the last line (continuation lines of the expression)
     */
    record Assign(int line, LabelRef target, Expression value, int endLine) implements Statement {}

    /**
     * CALL statement.
     *
     * @param errorLabel the variable receiving the error, or null
     * @param endLine z coordinate of the last continuation line
     */
    record Call(
            int line,
            String function,
            String errorLabel,
            List<CallParameter> parameters,
            List<CallResult> results,
            int endLine)
            implements Statement {
        public Call {
            parameters = List.copyOf(parameters);
            results = List.copyOf(results);
        }

        /** A PARAMETER line of a CALL statement. */
        public record CallParameter(int line, String name, Expression value) {}

        /** A RESULT line of a CALL statement. */
        public record CallResult(int line, String name, LabelRef target) {}
    }

    /**
     * IF statement.
     *
     * @param elseLine z coordinate of the ELSE line, or -1
     * @param otherwise the code block of the ELSE part, or null
     */
    record If(int line, Expression condition, CodeBlock then, int elseLine, CodeBlock otherwise) implements Statement {
        /** z coordinate of the END IF line. */
        public int endLine() {
            return otherwise == null ? then.endLine() : otherwise.endLine();
        }
    }

    /** FOR loop; the increment is null if it is omitted (1). */
    record For(
            int line,
            String variable,
            Type.Simple type,
            Expression from,
            Expression to,
            Expression step,
            CodeBlock body)
            implements Statement {}

    record Each(int line, String variable, LabelRef iterable, CodeBlock body) implements Statement {}

    record While(int line, Expression condition, CodeBlock body) implements Statement {}

    /** REPEAT loop; the condition is on the END REPEAT line. */
    record Repeat(int line, CodeBlock body, Expression condition) implements Statement {}

    /** BREAK statement; the condition is null if there is none. */
    record Break(int line, Expression condition) implements Statement {}

    /** RETURN statement; the error label is null if there is none. */
    record Return(int line, LabelRef error) implements Statement {}

    record Exit(int line, Expression code) implements Statement {}

    /** A code block: the lines from BEGIN to END. */
    record CodeBlock(int beginLine, List<Statement> statements, int endLine) {
        public CodeBlock {
            statements = List.copyOf(statements);
        }
    }
}
