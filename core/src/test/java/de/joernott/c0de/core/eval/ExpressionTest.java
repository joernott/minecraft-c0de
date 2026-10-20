package de.joernott.c0de.core.eval;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.joernott.c0de.core.error.C0deException;
import de.joernott.c0de.core.error.ErrorCode;
import de.joernott.c0de.core.line.Block;
import de.joernott.c0de.core.line.Line;
import de.joernott.c0de.core.line.Notation;
import de.joernott.c0de.core.program.Expression;
import de.joernott.c0de.core.program.ExpressionParser;
import de.joernott.c0de.core.value.ArrayValue;
import de.joernott.c0de.core.value.BooleanValue;
import de.joernott.c0de.core.value.FloatValue;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.StringValue;
import de.joernott.c0de.core.value.TimeValue;
import de.joernott.c0de.core.value.Type;
import de.joernott.c0de.core.value.Value;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;

class ExpressionTest {
    private final MapScope scope = new MapScope()
            .add(new Variable("i", Type.Simple.INTEGER, new IntegerValue(3), false, java.util.Map.of()))
            .add(new Variable("f", Type.Simple.FLOAT, new FloatValue(0.5), false, java.util.Map.of()))
            .add(new Variable("t", Type.Simple.BOOLEAN, BooleanValue.TRUE, false, java.util.Map.of()))
            .add(new Variable("s", Type.Str.UNLIMITED, new StringValue("abc"), false, java.util.Map.of()))
            .add(new Variable(
                    "values",
                    new Type.Array(Type.Simple.INTEGER),
                    new ArrayValue(Type.Simple.INTEGER, List.of(new IntegerValue(10), new IntegerValue(20))),
                    false,
                    java.util.Map.of()));

    static Expression parse(String expression) {
        Line line = Notation.parseLine(0, expression, ZoneOffset.UTC);
        List<Block> blocks = line.cells().stream().map(Line.Cell::block).toList();
        return ExpressionParser.parse(blocks, 0);
    }

    private Value eval(String expression) {
        return ExpressionEvaluator.evaluate(parse(expression), scope);
    }

    private void assertValue(Value expected, String expression) {
        assertEquals(expected, eval(expression), expression);
    }

    private void assertError(ErrorCode code, String expression) {
        C0deException e = assertThrows(C0deException.class, () -> eval(expression), expression);
        assertEquals(code, e.code(), expression + ": " + e.getMessage());
    }

    @Test
    void precedenceAndAssociativity() {
        assertValue(new IntegerValue(7), "1 + 2 * 3");
        assertValue(new IntegerValue(9), "( 1 + 2 ) * 3");
        assertValue(new IntegerValue(-4), "- 2 ^ 2");
        assertValue(new IntegerValue(512), "2 ^ 3 ^ 2");
        assertValue(new IntegerValue(2), "10 - 5 - 3");
        // Modulus binds weaker than multiplication: 7 \ (3 * 2)
        assertValue(new IntegerValue(1), "7 \\ 3 * 2");
        assertValue(new IntegerValue(-6), "- i * 2");
        assertValue(new FloatValue(0.125), "2 ^ - 3");
        assertValue(BooleanValue.TRUE, "1 + 1 = 2 AND 3 > 2");
        assertValue(BooleanValue.TRUE, "TRUE OR FALSE AND FALSE");
        assertValue(BooleanValue.FALSE, "TRUE XOR TRUE");
    }

    @Test
    void arithmeticTypeRules() {
        assertValue(new FloatValue(3.5), "i + f");
        assertValue(new FloatValue(1.5), "i / 2");
        assertValue(new IntegerValue(1), "7 \\ 2");
        assertValue(new IntegerValue(-1), "-7 \\ 2");
        assertError(ErrorCode.TYPE_MISMATCH, "7.0 \\ 2");
        assertError(ErrorCode.TYPE_MISMATCH, "s + 1");
        assertError(ErrorCode.TYPE_MISMATCH, "t * 2");
    }

    @Test
    void arithmeticErrors() {
        assertError(ErrorCode.DIVISION_BY_ZERO, "1 / 0");
        assertError(ErrorCode.DIVISION_BY_ZERO, "1.5 / 0.0");
        assertError(ErrorCode.DIVISION_BY_ZERO, "5 \\ 0");
        assertError(ErrorCode.NUMERIC_OVERFLOW, "9223372036854775807 + 1");
        assertError(ErrorCode.NUMERIC_OVERFLOW, "2 ^ 63");
        assertError(ErrorCode.NUMERIC_OVERFLOW, "- -9223372036854775808");
    }

    @Test
    void timeArithmetic() {
        assertValue(new TimeValue(1_000), "@1970-01-01T00:00:00Z + 1000");
        assertValue(new TimeValue(1_000), "1000 + @1970-01-01T00:00:00Z");
        assertValue(new TimeValue(500), "@1970-01-01T00:00:01Z - 500");
        assertValue(new IntegerValue(250), "@1970-01-01T00:00:01.250Z - @1970-01-01T00:00:01Z");
        assertValue(BooleanValue.TRUE, "@2026-01-01T00:00:00Z < @2026-10-19T12:00:00Z");
        assertError(ErrorCode.TYPE_MISMATCH, "@1970-01-01T00:00:00Z * 2");
        assertError(ErrorCode.TYPE_MISMATCH, "@1970-01-01T00:00:00Z + 1.5");
    }

    @Test
    void comparisons() {
        assertValue(BooleanValue.TRUE, "1 = 1.0");
        assertValue(BooleanValue.TRUE, "'a' = \"a\"");
        assertValue(BooleanValue.TRUE, "\"abc\" < \"abd\"");
        assertValue(BooleanValue.TRUE, "\"ab\" < \"abc\"");
        assertValue(BooleanValue.TRUE, "'b' ≥ 'a'");
        assertValue(BooleanValue.TRUE, "TRUE ≠ FALSE");
        assertValue(BooleanValue.TRUE, "i <= 3");
        assertError(ErrorCode.TYPE_MISMATCH, "TRUE < FALSE");
        assertError(ErrorCode.TYPE_MISMATCH, "1 = \"1\"");
    }

    @Test
    void logicalOperatorsShortCircuit() {
        // The right side would raise DIVISION_BY_ZERO
        assertValue(BooleanValue.FALSE, "FALSE AND 1 / 0 > 1");
        assertValue(BooleanValue.TRUE, "TRUE OR 1 / 0 > 1");
        assertValue(BooleanValue.FALSE, "NOT t");
        // NOT binds stronger than comparisons
        assertError(ErrorCode.TYPE_MISMATCH, "NOT i = 3");
        assertError(ErrorCode.TYPE_MISMATCH, "1 AND TRUE");
    }

    @Test
    void bitwiseOperators() {
        assertValue(new IntegerValue(2), "6 & 3");
        assertValue(new IntegerValue(7), "6 | 3");
        assertValue(new IntegerValue(5), "6 ⊻ 3");
        assertValue(new IntegerValue(-1), "¬ 0");
        assertValue(new IntegerValue(16), "1 ← 4");
        assertValue(new IntegerValue(-2), "-8 → 2");
        assertError(ErrorCode.TYPE_MISMATCH, "1.0 & 1");
    }

    @Test
    void concatenationUsesTheTextRepresentation() {
        assertValue(new StringValue("abc3"), "s ⊕ i");
        assertValue(new StringValue("0.5true"), "f ⊕ t");
        assertValue(new StringValue("[10, 20]!"), "values ⊕ \"!\"");
        assertValue(new StringValue("1970-01-01T00:00:00.000Z"), "\"\" ⊕ @1970-01-01T00:00:00Z");
    }

    @Test
    void labelsWithIndex() {
        assertValue(new IntegerValue(20), "values[1]");
        assertValue(new IntegerValue(30), "values[0] + values[1] \\ 30 + 0");
        assertError(ErrorCode.UNKNOWN_NAME, "unknown + 1");
        assertError(ErrorCode.INDEX_OUT_OF_BOUNDS, "values[2]");
    }

    @Test
    void parenthesesCanBeNestedEightLevels() {
        assertValue(new IntegerValue(1), "( ( ( ( ( ( ( ( 1 ) ) ) ) ) ) ) )");
        assertError(ErrorCode.SYNTAX_ERROR_EXPRESSION, "( ( ( ( ( ( ( ( ( 1 ) ) ) ) ) ) ) ) )");
    }

    @Test
    void malformedExpressions() {
        for (String expression : List.of(
                "1 +",
                "+ 1",
                "1 2",
                "( 1 + 2",
                "1 + 2 )",
                "( )",
                "OPERATOR",
                "1 + LITERAL",
                "LABEL",
                "i i",
                "1 * * 2")) {
            assertError(ErrorCode.SYNTAX_ERROR_EXPRESSION, expression);
        }
        C0deException e = assertThrows(C0deException.class, () -> ExpressionParser.parse(List.of(), 4));
        assertEquals(ErrorCode.SYNTAX_ERROR_EXPRESSION, e.code());
        assertEquals(4, e.line());
        assertTrue(e.crash());
    }
}
