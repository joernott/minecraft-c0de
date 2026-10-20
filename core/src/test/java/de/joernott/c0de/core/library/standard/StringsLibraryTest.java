package de.joernott.c0de.core.library.standard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.joernott.c0de.core.error.C0deException;
import de.joernott.c0de.core.error.ErrorCode;
import de.joernott.c0de.core.library.Library;
import de.joernott.c0de.core.library.NativeResult;
import de.joernott.c0de.core.value.BooleanValue;
import de.joernott.c0de.core.value.CharValue;
import de.joernott.c0de.core.value.FloatValue;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.StringValue;
import de.joernott.c0de.core.value.TimeValue;
import de.joernott.c0de.core.value.Value;
import org.junit.jupiter.api.Test;

/** c0de/strings: string and character functions (task 5.3) and FORMAT (task 5.4). */
class StringsLibraryTest {
    private static final Library STRINGS = new StringsLibrary();

    private static FakeCall call(String function) {
        return FakeCall.of(STRINGS, function);
    }

    private static StringValue string(String value) {
        return new StringValue(value);
    }

    private static IntegerValue integer(long value) {
        return new IntegerValue(value);
    }

    private static void assertCode(ErrorCode code, Runnable operation) {
        C0deException e = assertThrows(C0deException.class, operation::run);
        assertEquals(code, e.code(), e.getMessage());
    }

    @Test
    void substrings() {
        // The smiley is one character (two Java chars)
        StringValue text = string("a😀cde");
        assertEquals(
                string("a😀"),
                call("LEFT").with("text", text).with("length", integer(2)).result());
        assertEquals(
                string("a😀cde"),
                call("LEFT").with("text", text).with("length", integer(99)).result());
        assertEquals(
                string("de"),
                call("RIGHT").with("text", text).with("length", integer(2)).result());
        assertEquals(
                string("😀c"),
                call("MID")
                        .with("text", text)
                        .with("start", integer(1))
                        .with("length", integer(2))
                        .result());
        assertEquals(
                string("cde"),
                call("MID").with("text", text).with("start", integer(2)).result());
        assertEquals(
                string(""),
                call("MID").with("text", text).with("start", integer(9)).result());
        assertCode(
                ErrorCode.INVALID_ARGUMENT,
                () -> call("LEFT")
                        .with("text", text)
                        .with("length", integer(-1))
                        .result());
        assertCode(
                ErrorCode.INVALID_ARGUMENT,
                () -> call("MID").with("text", text).with("start", integer(-1)).result());
    }

    @Test
    void replaceAndSearch() {
        assertEquals(
                string("aXYde"),
                call("REPLACE")
                        .with("text", string("abcde"))
                        .with("start", integer(1))
                        .with("insert", string("XY"))
                        .result());
        assertEquals(
                string("abcXY"),
                call("REPLACE")
                        .with("text", string("abcde"))
                        .with("start", integer(3))
                        .with("insert", string("XYZ"))
                        .result());
        assertCode(
                ErrorCode.INVALID_ARGUMENT,
                () -> call("REPLACE")
                        .with("text", string("ab"))
                        .with("start", integer(3))
                        .with("insert", string("x"))
                        .result());
        assertEquals(
                integer(2),
                call("INSTR")
                        .with("text", string("a😀bcbc"))
                        .with("search", string("bc"))
                        .result());
        assertEquals(
                integer(4),
                call("INSTR")
                        .with("text", string("a😀bcbc"))
                        .with("search", string("bc"))
                        .with("start", integer(3))
                        .result());
        assertEquals(
                integer(-1),
                call("INSTR")
                        .with("text", string("abc"))
                        .with("search", string("x"))
                        .result());
    }

    @Test
    void charactersAndConversions() {
        assertEquals(string("ÄBC"), call("UCASE").with("text", string("äbc")).result());
        assertEquals(string("abc"), call("LCASE").with("text", string("ABC")).result());
        assertEquals(string("   "), call("SPACE").with("count", integer(3)).result());
        assertEquals(
                string("**"),
                call("FILL")
                        .with("count", integer(2))
                        .with("char", new CharValue('*'))
                        .result());
        assertCode(
                ErrorCode.INVALID_ARGUMENT,
                () -> call("SPACE").with("count", integer(-1)).result());
        assertCode(
                ErrorCode.INVALID_ARGUMENT,
                () -> call("SPACE").with("count", integer(10_000_000)).result());
        assertEquals(integer(65), call("ASC").with("char", new CharValue('A')).result());
        assertEquals(
                new CharValue(0x1F600),
                call("CHR").with("code", integer(0x1F600)).result());
        assertCode(
                ErrorCode.INVALID_ARGUMENT,
                () -> call("CHR").with("code", integer(-1)).result());
        assertEquals(
                string("2.5"), call("STR").with("value", new FloatValue(2.5)).result());
        assertEquals(string("FF"), call("HEX").with("value", integer(255)).result());
        assertEquals(
                string("FFFFFFFFFFFFFFFF"),
                call("HEX").with("value", integer(-1)).result());
        assertEquals(string("17"), call("OCT").with("value", integer(15)).result());
    }

    @Test
    void val() {
        NativeResult integer = call("VAL").with("text", string(" 42 ")).call();
        assertEquals(new FloatValue(42), integer.results().get("value"));
        assertEquals(BooleanValue.TRUE, integer.results().get("isInteger"));
        NativeResult floating = call("VAL").with("text", string("-1.5e2")).call();
        assertEquals(new FloatValue(-150), floating.results().get("value"));
        assertEquals(BooleanValue.FALSE, floating.results().get("isInteger"));
        assertCode(
                ErrorCode.INVALID_ARGUMENT,
                () -> call("VAL").with("text", string("abc")).call());
        assertCode(
                ErrorCode.INVALID_ARGUMENT,
                () -> call("VAL").with("text", string("NaN")).call());
    }

    private static String format(Value value, String format) {
        return ((StringValue) call("FORMAT")
                        .with("value", value)
                        .with("format", string(format))
                        .result())
                .value();
    }

    @Test
    void formatExamplesOfTheConcept() {
        assertEquals("   42", format(integer(42), "%5d"));
        assertEquals("00042", format(integer(42), "%05d"));
        assertEquals("3.14", format(new FloatValue(3.14159), "%.2f"));
        assertEquals("1,234,567", format(integer(1234567), "%,d"));
        assertEquals("0xFF", format(integer(255), "0x%X"));
        assertEquals("[c0de    ]", format(string("c0de"), "[%-8s]"));
        TimeValue now = new TimeValue(FakeCall.NOW);
        assertEquals("2026-10-19 14:05:09", format(now, "%tF %tT"));
        assertEquals("Monday, 19. October 2026", format(now, "%tA, %te. %tB %tY"));
    }

    @Test
    void formatConversions() {
        assertEquals("ff 377 +5  5", format(integer(255), "%x %o") + " " + format(integer(5), "%+d % d"));
        assertEquals("1.234560e+03", format(new FloatValue(1234.56), "%e"));
        assertEquals("2.000000", format(integer(2), "%f"));
        assertEquals("0.500000", format(new FloatValue(0.5), "%g"));
        assertEquals("1.000000e+20", format(new FloatValue(1e20), "%g"));
        assertEquals("x", format(new CharValue('x'), "%c"));
        assertEquals("true", format(BooleanValue.TRUE, "%b"));
        assertEquals("ab", format(string("abc"), "%.2s"));
        assertEquals("100% done\n", format(integer(100), "%d%% done%n"));
        TimeValue now = new TimeValue(FakeCall.NOW);
        assertEquals("26 10 Oct 292 Mon 1 43", format(now, "%ty %tm %tb %tj %ta %tu %tV"));
        assertEquals("02 pm 250 +0200 CEST", format(now, "%tI %tp %tL %tz %tZ"));
        assertEquals("1792411509 1792411509250 14:05", format(now, "%ts %tQ %tR"));
        assertEquals("  1", format(now, "%3tu"));
    }

    @Test
    void formatErrors() {
        assertCode(ErrorCode.TYPE_MISMATCH, () -> format(string("x"), "%d"));
        assertCode(ErrorCode.TYPE_MISMATCH, () -> format(new FloatValue(1.5), "%x"));
        assertCode(ErrorCode.TYPE_MISMATCH, () -> format(integer(1), "%tY"));
        assertCode(ErrorCode.TYPE_MISMATCH, () -> format(integer(1), "%c"));
        assertCode(ErrorCode.INVALID_ARGUMENT, () -> format(integer(1), "%"));
        assertCode(ErrorCode.INVALID_ARGUMENT, () -> format(integer(1), "%q"));
        assertCode(ErrorCode.INVALID_ARGUMENT, () -> format(integer(1), "%.d"));
        assertCode(ErrorCode.INVALID_ARGUMENT, () -> format(integer(1), "%,x"));
        assertCode(ErrorCode.INVALID_ARGUMENT, () -> format(new TimeValue(0), "%tK"));
    }
}
