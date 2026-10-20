package de.joernott.c0de.core.program;

import de.joernott.c0de.core.error.C0deException;
import de.joernott.c0de.core.error.ErrorCode;
import de.joernott.c0de.core.line.Block;
import de.joernott.c0de.core.line.Operator;
import java.util.List;

/**
 * Builds the syntax tree of an expression from its blocks (without the final {@code ;}), using precedence
 * climbing. Operators of the same precedence are evaluated from left to right, except for ^, which is
 * evaluated from right to left. A minus sign at the beginning of an expression or after an operator or an
 * opening parenthesis is a negation.
 */
public final class ExpressionParser {
    /** Maximum nesting depth of parentheses. */
    public static final int MAX_PARENTHESES_DEPTH = 8;

    private final List<Block> tokens;
    private final int line;
    private int position;
    private int depth;

    private ExpressionParser(List<Block> tokens, int line) {
        this.tokens = tokens;
        this.line = line;
    }

    /**
     * Parses an expression.
     *
     * @param tokens the LITERAL, LABEL and OPERATOR blocks of the expression without the final {@code ;}
     * @param line the z coordinate of the line, for error messages
     * @throws C0deException SYNTAX_ERROR_EXPRESSION if the expression is malformed
     */
    public static Expression parse(List<Block> tokens, int line) {
        if (tokens.isEmpty()) {
            throw error(line, "The expression is empty");
        }
        ExpressionParser parser = new ExpressionParser(tokens, line);
        Expression expression = parser.parseExpression(0);
        if (parser.position < tokens.size()) {
            Block next = tokens.get(parser.position);
            if (next instanceof Block.OperatorBlock(Operator operator) && operator == Operator.CLOSE) {
                throw error(line, "A closing parenthesis has no matching opening parenthesis");
            }
            throw error(line, "Two operands without an operator between them");
        }
        return expression;
    }

    /** Binding power of an operator: the higher, the stronger it binds (the inverse of the precedence). */
    private static int bindingPower(Operator operator) {
        return 20 - operator.precedence();
    }

    private Expression parseExpression(int minimumBindingPower) {
        Expression left = parsePrefix();
        while (position < tokens.size()) {
            Block next = tokens.get(position);
            if (!(next instanceof Block.OperatorBlock(Operator operator))) {
                throw error(line, "Two operands without an operator between them");
            }
            if (operator == null) {
                throw error(line, "An OPERATOR block has no symbol");
            }
            if (operator == Operator.CLOSE) {
                return left;
            }
            if (operator.arity() != Operator.Arity.BINARY && operator.arity() != Operator.Arity.BINARY_RIGHT) {
                throw error(line, "The operator " + operator.symbol() + " can't follow an operand");
            }
            int power = bindingPower(operator);
            if (power <= minimumBindingPower) {
                return left;
            }
            position++;
            int rightPower = operator.arity() == Operator.Arity.BINARY_RIGHT ? power - 1 : power;
            Expression right = parseExpression(rightPower);
            left = new Expression.Binary(operator, left, right);
        }
        return left;
    }

    private Expression parsePrefix() {
        if (position >= tokens.size()) {
            throw error(line, "An operand is missing at the end of the expression");
        }
        Block token = tokens.get(position++);
        return switch (token) {
            case Block.LiteralBlock literal -> {
                if (literal.value() == null) {
                    throw error(line, "A LITERAL has no value");
                }
                yield new Expression.Literal(literal.value());
            }
            case Block.LabelBlock label -> {
                if (label.name() == null) {
                    throw error(line, "A LABEL has no name");
                }
                yield new Expression.Reference(new LabelRef(label.name(), label.index()));
            }
            case Block.OperatorBlock(Operator operator) -> prefixOperator(operator);
            default -> throw error(line, "A " + describe(token) + " can't be part of an expression");
        };
    }

    private Expression prefixOperator(Operator operator) {
        if (operator == null) {
            throw error(line, "An OPERATOR block has no symbol");
        }
        switch (operator) {
            case OPEN -> {
                depth++;
                if (depth > MAX_PARENTHESES_DEPTH) {
                    throw error(line, "Parentheses are nested deeper than " + MAX_PARENTHESES_DEPTH + " levels");
                }
                Expression inner = parseExpression(0);
                if (position >= tokens.size()
                        || !(tokens.get(position) instanceof Block.OperatorBlock(Operator close))
                        || close != Operator.CLOSE) {
                    throw error(line, "An opening parenthesis has no matching closing parenthesis");
                }
                position++;
                depth--;
                return inner;
            }
            case SUBTRACT, NEGATE -> {
                return new Expression.Unary(Operator.NEGATE, parseExpression(bindingPower(Operator.NEGATE)));
            }
            case NOT, BITWISE_NOT -> {
                return new Expression.Unary(operator, parseExpression(bindingPower(operator)));
            }
            case CLOSE -> throw error(line, "An operand is missing before a closing parenthesis");
            default -> throw error(line, "An operand is missing before the operator " + operator.symbol());
        }
    }

    private static String describe(Block block) {
        return switch (block) {
            case Block.KeywordBlock keyword -> keyword.keyword().name() + " block";
            case Block.TagBlock tag -> "TAG block";
            case Block.CommentBlock comment -> "COMMENT block";
            default -> block.getClass().getSimpleName();
        };
    }

    private static C0deException error(int line, String message) {
        return C0deException.at(line, ErrorCode.SYNTAX_ERROR_EXPRESSION, message);
    }
}
