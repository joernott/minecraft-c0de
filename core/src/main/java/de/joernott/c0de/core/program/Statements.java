package de.joernott.c0de.core.program;

import java.util.ArrayList;
import java.util.List;

/** Helpers for walking through statements. */
public final class Statements {
    private Statements() {}

    /** All statements, including those in code blocks, in the order of their lines. */
    public static List<Statement> flatten(List<Statement> statements) {
        List<Statement> result = new ArrayList<>();
        for (Statement statement : statements) {
            add(statement, result);
        }
        return result;
    }

    private static void add(Statement statement, List<Statement> result) {
        result.add(statement);
        switch (statement) {
            case Statement.FunctionDeclaration function -> addAll(function.body(), result);
            case Statement.If condition -> {
                addAll(condition.then(), result);
                if (condition.otherwise() != null) {
                    addAll(condition.otherwise(), result);
                }
            }
            case Statement.For loop -> addAll(loop.body(), result);
            case Statement.Each loop -> addAll(loop.body(), result);
            case Statement.While loop -> addAll(loop.body(), result);
            case Statement.Repeat loop -> addAll(loop.body(), result);
            default -> {}
        }
    }

    private static void addAll(Statement.CodeBlock block, List<Statement> result) {
        for (Statement statement : block.statements()) {
            add(statement, result);
        }
    }
}
