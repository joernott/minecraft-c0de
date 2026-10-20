package de.joernott.c0de.core.line;

import java.util.List;
import java.util.Optional;

/**
 * The symbols of OPERATOR blocks: operators, parentheses and the end of an expression. The precedence
 * follows docs/concept/05-expressions.md, where 1 is the highest.
 */
public enum Operator {
    BITWISE_NOT("¬", 2, Arity.UNARY, "~"),
    NOT("NOT", 3, Arity.UNARY),
    POWER("^", 4, Arity.BINARY_RIGHT),
    NEGATE("-", 5, Arity.UNARY),
    MULTIPLY("*", 6, Arity.BINARY),
    DIVIDE("/", 6, Arity.BINARY),
    MODULUS("\\", 7, Arity.BINARY),
    ADD("+", 8, Arity.BINARY),
    SUBTRACT("-", 8, Arity.BINARY),
    SHIFT_LEFT("←", 9, Arity.BINARY, "<<"),
    SHIFT_RIGHT("→", 9, Arity.BINARY, ">>"),
    CONCAT("⊕", 10, Arity.BINARY, "++"),
    EQUAL("=", 11, Arity.BINARY),
    NOT_EQUAL("≠", 11, Arity.BINARY, "<>", "!="),
    LESS("<", 11, Arity.BINARY),
    GREATER(">", 11, Arity.BINARY),
    LESS_OR_EQUAL("≤", 11, Arity.BINARY, "<="),
    GREATER_OR_EQUAL("≥", 11, Arity.BINARY, ">="),
    BITWISE_AND("&", 12, Arity.BINARY),
    BITWISE_XOR("⊻", 13, Arity.BINARY),
    BITWISE_OR("|", 14, Arity.BINARY),
    AND("AND", 15, Arity.BINARY),
    XOR("XOR", 16, Arity.BINARY),
    OR("OR", 17, Arity.BINARY),
    OPEN("(", 1, Arity.GROUPING),
    CLOSE(")", 1, Arity.GROUPING),
    END_OF_EXPRESSION(";", 0, Arity.GROUPING);

    private final String symbol;
    private final int precedence;
    private final Arity arity;
    private final List<String> aliases;

    Operator(String symbol, int precedence, Arity arity, String... aliases) {
        this.symbol = symbol;
        this.precedence = precedence;
        this.arity = arity;
        this.aliases = List.of(aliases);
    }

    /** The symbol shown on the block. The minus sign is the same symbol for subtraction and negation. */
    public String symbol() {
        return symbol;
    }

    /** Precedence from 1 (highest, parentheses) to 17 (lowest, OR). */
    public int precedence() {
        return precedence;
    }

    public Arity arity() {
        return arity;
    }

    /**
     * The operator block with this symbol (or an ASCII alias such as {@code <>} for ≠). The minus sign
     * returns {@link #SUBTRACT}; whether it negates is decided by its position in the expression.
     */
    public static Optional<Operator> ofSymbol(String symbol) {
        for (Operator operator : values()) {
            if (operator != NEGATE && (operator.symbol.equals(symbol) || operator.aliases.contains(symbol))) {
                return Optional.of(operator);
            }
        }
        return Optional.empty();
    }

    /** Kinds of operators. */
    public enum Arity {
        /** A prefix operator with one operand. */
        UNARY,
        /** An operator with two operands, evaluated from left to right. */
        BINARY,
        /** An operator with two operands, evaluated from right to left (exponentiation). */
        BINARY_RIGHT,
        /** Parentheses and the end of an expression. */
        GROUPING
    }
}
