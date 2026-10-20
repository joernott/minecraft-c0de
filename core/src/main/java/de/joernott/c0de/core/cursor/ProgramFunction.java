package de.joernott.c0de.core.cursor;

import de.joernott.c0de.core.error.C0deException;
import de.joernott.c0de.core.error.ErrorCode;
import de.joernott.c0de.core.eval.TypeRegistry;
import de.joernott.c0de.core.program.Statement;
import de.joernott.c0de.core.value.Type;
import de.joernott.c0de.core.value.Value;
import de.joernott.c0de.core.value.Values;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** A function of the program with the resolved types of its parameters and results. */
record ProgramFunction(Statement.FunctionDeclaration declaration, List<Parameter> parameters, List<Result> results)
        implements Cursor.FunctionEntry {
    record Parameter(String name, Type type, Value defaultValue, int line) {}

    record Result(String name, Type type, int line) {}

    /**
     * Resolves the types of a function declaration.
     *
     * @throws C0deException (crash) UNKNOWN_STRUCT_ENTITY for unknown struct types,
     *     SYNTAX_ERROR_TYPE_MISMATCH for default values of the wrong type
     */
    static ProgramFunction resolve(Statement.FunctionDeclaration declaration, TypeRegistry types) {
        List<Parameter> parameters = new ArrayList<>();
        for (Statement.FunctionDeclaration.Parameter parameter : declaration.parameters()) {
            Type type = located(parameter.line(), () -> types.resolve(parameter.type()));
            Value defaultValue = null;
            if (parameter.defaultValue() != null) {
                try {
                    defaultValue = Values.convert(parameter.defaultValue(), type);
                } catch (C0deException e) {
                    throw C0deException.at(parameter.line(), ErrorCode.SYNTAX_ERROR_TYPE_MISMATCH, e.getMessage())
                            .asCrash();
                }
            }
            parameters.add(new Parameter(parameter.name(), type, defaultValue, parameter.line()));
        }
        List<Result> results = new ArrayList<>();
        for (Statement.FunctionDeclaration.Result result : declaration.results()) {
            results.add(new Result(
                    result.name(), located(result.line(), () -> types.resolve(result.type())), result.line()));
        }
        return new ProgramFunction(declaration, parameters, results);
    }

    private static Type located(int line, java.util.function.Supplier<Type> operation) {
        try {
            return operation.get();
        } catch (C0deException e) {
            throw e.inLine(line).asCrash();
        }
    }

    @Override
    public String name() {
        return declaration.name();
    }

    @Override
    public List<String> resultNames() {
        return results.stream().map(Result::name).toList();
    }

    Optional<Parameter> parameter(String parameterName) {
        return parameters.stream().filter(p -> p.name().equals(parameterName)).findFirst();
    }
}
