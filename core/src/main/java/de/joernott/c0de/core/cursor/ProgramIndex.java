package de.joernott.c0de.core.cursor;

import de.joernott.c0de.core.program.Program;
import de.joernott.c0de.core.program.Statement;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The lines a cursor executes, by z coordinate: statements, END lines and ELSE lines. Continuation lines,
 * the content lines of declarations and NOOP and comment lines are not part of it, so the cursor skips
 * them.
 */
final class ProgramIndex {
    /** A line the cursor can execute. */
    sealed interface Node {}

    /**
     * A statement.
     *
     * @param function the function the statement is in, or null in the global scope
     */
    record StatementNode(Statement statement, Statement.FunctionDeclaration function) implements Node {}

    /** The END line of a code block. */
    record EndNode(Statement owner, Statement.CodeBlock block, Statement.FunctionDeclaration function)
            implements Node {}

    /** The ELSE line of an IF statement. */
    record ElseNode(Statement.If owner) implements Node {}

    private final TreeMap<Integer, Node> nodes = new TreeMap<>();

    ProgramIndex(Program program) {
        for (Statement statement : program.statements()) {
            add(statement, null);
        }
    }

    private void add(Statement statement, Statement.FunctionDeclaration function) {
        nodes.put(statement.line(), new StatementNode(statement, function));
        switch (statement) {
            case Statement.FunctionDeclaration declaration -> block(declaration, declaration.body(), declaration);
            case Statement.If condition -> {
                block(condition, condition.then(), function);
                if (condition.otherwise() != null) {
                    nodes.put(condition.elseLine(), new ElseNode(condition));
                    block(condition, condition.otherwise(), function);
                }
            }
            case Statement.For loop -> block(loop, loop.body(), function);
            case Statement.Each loop -> block(loop, loop.body(), function);
            case Statement.While loop -> block(loop, loop.body(), function);
            case Statement.Repeat loop -> block(loop, loop.body(), function);
            default -> {}
        }
    }

    private void block(Statement owner, Statement.CodeBlock block, Statement.FunctionDeclaration function) {
        for (Statement statement : block.statements()) {
            add(statement, function);
        }
        nodes.put(block.endLine(), new EndNode(owner, block, function));
    }

    /** The first executable line at or after z, or null. */
    Integer next(int z) {
        return nodes.ceilingKey(z);
    }

    Node node(int z) {
        return nodes.get(z);
    }

    /** The statement in line z. */
    Statement statement(int z) {
        return nodes.get(z) instanceof StatementNode node ? node.statement() : null;
    }

    /** The END line node of the code block ending in line z. */
    EndNode end(int z) {
        return nodes.get(z) instanceof EndNode node ? node : null;
    }

    /** All nodes, for tests. */
    Map<Integer, Node> nodes() {
        return nodes;
    }

    /** The statements of the global scope (for restoring cursors). */
    static List<Statement> globals(Program program) {
        return program.statements();
    }
}
