package de.joernott.c0de.core.program;

import de.joernott.c0de.core.line.Operator;
import de.joernott.c0de.core.value.Value;

/** The syntax tree of an expression. */
public sealed interface Expression {
    /** A LITERAL. */
    record Literal(Value value) implements Expression {}

    /** A LABEL referring to a variable or constant, optionally with an index. */
    record Reference(LabelRef label) implements Expression {}

    /** A unary operator: {@link Operator#NEGATE}, {@link Operator#NOT} or {@link Operator#BITWISE_NOT}. */
    record Unary(Operator operator, Expression operand) implements Expression {}

    /** A binary operator. */
    record Binary(Operator operator, Expression left, Expression right) implements Expression {}
}
