package de.joernott.c0de.core.eval;

import de.joernott.c0de.core.error.C0deException;
import de.joernott.c0de.core.error.ErrorCode;
import de.joernott.c0de.core.line.Operator;
import de.joernott.c0de.core.program.Expression;
import de.joernott.c0de.core.value.BooleanValue;
import de.joernott.c0de.core.value.FloatValue;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.StringValue;
import de.joernott.c0de.core.value.TimeValue;
import de.joernott.c0de.core.value.Value;
import de.joernott.c0de.core.value.Values;

/**
 * Evaluates expressions following the type rules of docs/concept/05-expressions.md. AND and OR only evaluate
 * their right operand if the left one doesn't decide the result already.
 */
public final class ExpressionEvaluator {
    private ExpressionEvaluator() {}

    /**
     * Evaluates an expression. Values of variables are not copied; callers that store the result copy it
     * (see {@link Values#convert}).
     *
     * @throws C0deException TYPE_MISMATCH, DIVISION_BY_ZERO, NUMERIC_OVERFLOW and the errors of
     *     {@link IndexedAccess#read}
     */
    public static Value evaluate(Expression expression, Scope scope) {
        return switch (expression) {
            case Expression.Literal literal -> literal.value();
            case Expression.Reference reference -> IndexedAccess.read(reference.label(), scope);
            case Expression.Unary unary -> unary(unary.operator(), evaluate(unary.operand(), scope));
            case Expression.Binary binary -> binary(binary, scope);
        };
    }

    /** Evaluates an expression that must result in a BOOLEAN, e.g. a condition. */
    public static boolean evaluateBoolean(Expression expression, Scope scope) {
        Value value = evaluate(expression, scope);
        if (!(value instanceof BooleanValue b)) {
            throw C0deException.of(ErrorCode.TYPE_MISMATCH, "A BOOLEAN is required, not a " + value.type());
        }
        return b.value();
    }

    private static Value unary(Operator operator, Value operand) {
        return switch (operator) {
            case NEGATE ->
                switch (operand) {
                    case IntegerValue i -> new IntegerValue(exact(() -> Math.negateExact(i.value())));
                    case FloatValue f -> new FloatValue(-f.value());
                    default -> throw mismatch(operator, operand);
                };
            case NOT -> new BooleanValue(!bool(operator, operand));
            case BITWISE_NOT -> new IntegerValue(~integer(operator, operand));
            default -> throw new IllegalArgumentException("Not a unary operator: " + operator);
        };
    }

    private static Value binary(Expression.Binary binary, Scope scope) {
        Operator operator = binary.operator();
        Value left = evaluate(binary.left(), scope);
        if (operator == Operator.AND || operator == Operator.OR) {
            boolean l = bool(operator, left);
            if (operator == Operator.AND && !l) {
                return BooleanValue.FALSE;
            }
            if (operator == Operator.OR && l) {
                return BooleanValue.TRUE;
            }
            return BooleanValue.of(bool(operator, evaluate(binary.right(), scope)));
        }
        Value right = evaluate(binary.right(), scope);
        return switch (operator) {
            case ADD -> add(left, right);
            case SUBTRACT -> subtract(left, right);
            case MULTIPLY -> arithmetic(operator, left, right, Math::multiplyExact, (a, b) -> a * b);
            case DIVIDE -> divide(left, right);
            case MODULUS -> modulus(left, right);
            case POWER -> power(left, right);
            case CONCAT -> new StringValue(left.text() + right.text());
            case EQUAL -> BooleanValue.of(Values.equal(left, right));
            case NOT_EQUAL -> BooleanValue.of(!Values.equal(left, right));
            case LESS -> BooleanValue.of(Values.compare(left, right) < 0);
            case GREATER -> BooleanValue.of(Values.compare(left, right) > 0);
            case LESS_OR_EQUAL -> BooleanValue.of(Values.compare(left, right) <= 0);
            case GREATER_OR_EQUAL -> BooleanValue.of(Values.compare(left, right) >= 0);
            case XOR -> BooleanValue.of(bool(operator, left) ^ bool(operator, right));
            case BITWISE_AND -> new IntegerValue(integer(operator, left) & integer(operator, right));
            case BITWISE_OR -> new IntegerValue(integer(operator, left) | integer(operator, right));
            case BITWISE_XOR -> new IntegerValue(integer(operator, left) ^ integer(operator, right));
            case SHIFT_LEFT -> new IntegerValue(integer(operator, left) << integer(operator, right));
            case SHIFT_RIGHT -> new IntegerValue(integer(operator, left) >> integer(operator, right));
            default -> throw new IllegalArgumentException("Not a binary operator: " + operator);
        };
    }

    private static Value add(Value left, Value right) {
        if (left instanceof TimeValue time && right instanceof IntegerValue millis) {
            return new TimeValue(exact(() -> Math.addExact(time.millis(), millis.value())));
        }
        if (left instanceof IntegerValue millis && right instanceof TimeValue time) {
            return new TimeValue(exact(() -> Math.addExact(time.millis(), millis.value())));
        }
        return arithmetic(Operator.ADD, left, right, Math::addExact, Double::sum);
    }

    private static Value subtract(Value left, Value right) {
        if (left instanceof TimeValue time && right instanceof IntegerValue millis) {
            return new TimeValue(exact(() -> Math.subtractExact(time.millis(), millis.value())));
        }
        if (left instanceof TimeValue a && right instanceof TimeValue b) {
            return new IntegerValue(exact(() -> Math.subtractExact(a.millis(), b.millis())));
        }
        return arithmetic(Operator.SUBTRACT, left, right, Math::subtractExact, (a, b) -> a - b);
    }

    private static Value arithmetic(
            Operator operator, Value left, Value right, LongOperation integers, DoubleOperation floats) {
        if (left instanceof IntegerValue a && right instanceof IntegerValue b) {
            return new IntegerValue(exact(() -> integers.apply(a.value(), b.value())));
        }
        if (Values.isNumber(left) && Values.isNumber(right)) {
            return new FloatValue(floats.apply(Values.toDouble(left), Values.toDouble(right)));
        }
        throw mismatch(operator, left, right);
    }

    private static Value divide(Value left, Value right) {
        if (!Values.isNumber(left) || !Values.isNumber(right)) {
            throw mismatch(Operator.DIVIDE, left, right);
        }
        double divisor = Values.toDouble(right);
        if (divisor == 0.0) {
            throw C0deException.of(ErrorCode.DIVISION_BY_ZERO, "Division by 0");
        }
        return new FloatValue(Values.toDouble(left) / divisor);
    }

    private static Value modulus(Value left, Value right) {
        long a = integer(Operator.MODULUS, left);
        long b = integer(Operator.MODULUS, right);
        if (b == 0) {
            throw C0deException.of(ErrorCode.DIVISION_BY_ZERO, "Modulus by 0");
        }
        return new IntegerValue(a % b);
    }

    /**
     * Exponentiation: INTEGER ^ INTEGER with a non-negative exponent is an INTEGER, all other combinations of
     * numbers are a FLOAT.
     */
    private static Value power(Value left, Value right) {
        if (left instanceof IntegerValue base && right instanceof IntegerValue exponent && exponent.value() >= 0) {
            return new IntegerValue(exact(() -> integerPower(base.value(), exponent.value())));
        }
        if (Values.isNumber(left) && Values.isNumber(right)) {
            return new FloatValue(Math.pow(Values.toDouble(left), Values.toDouble(right)));
        }
        throw mismatch(Operator.POWER, left, right);
    }

    private static long integerPower(long base, long exponent) {
        long result = 1;
        long factor = base;
        long remaining = exponent;
        while (remaining > 0) {
            if ((remaining & 1) == 1) {
                result = Math.multiplyExact(result, factor);
            }
            remaining >>= 1;
            if (remaining > 0) {
                factor = Math.multiplyExact(factor, factor);
            }
        }
        return result;
    }

    private static boolean bool(Operator operator, Value value) {
        if (!(value instanceof BooleanValue b)) {
            throw mismatch(operator, value);
        }
        return b.value();
    }

    private static long integer(Operator operator, Value value) {
        if (!(value instanceof IntegerValue i)) {
            throw mismatch(operator, value);
        }
        return i.value();
    }

    private static long exact(LongSupplier operation) {
        try {
            return operation.get();
        } catch (ArithmeticException e) {
            throw C0deException.of(ErrorCode.NUMERIC_OVERFLOW, "The result doesn't fit into an INTEGER");
        }
    }

    private static C0deException mismatch(Operator operator, Value operand) {
        return C0deException.of(
                ErrorCode.TYPE_MISMATCH,
                "The operator " + operator.symbol() + " can't be applied to a " + operand.type());
    }

    private static C0deException mismatch(Operator operator, Value left, Value right) {
        return C0deException.of(
                ErrorCode.TYPE_MISMATCH,
                "The operator " + operator.symbol() + " can't be applied to a " + left.type() + " and a "
                        + right.type());
    }

    @FunctionalInterface
    private interface LongOperation {
        long apply(long a, long b);
    }

    @FunctionalInterface
    private interface DoubleOperation {
        double apply(double a, double b);
    }

    @FunctionalInterface
    private interface LongSupplier {
        long get();
    }
}
