package de.joernott.c0de.core.library.standard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.joernott.c0de.core.error.C0deException;
import de.joernott.c0de.core.error.ErrorCode;
import de.joernott.c0de.core.library.Library;
import de.joernott.c0de.core.value.FloatValue;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.Literals;
import de.joernott.c0de.core.value.TimeValue;
import de.joernott.c0de.core.value.Value;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

/** c0de/math (task 5.2) and c0de/time (task 5.7). */
class MathAndTimeLibraryTest {
    private static final Library MATH = new MathLibrary();
    private static final Library TIME = new TimeLibrary();

    private static Value math(String function, Value value) {
        return FakeCall.of(MATH, function).with("value", value).result();
    }

    private static IntegerValue integer(long value) {
        return new IntegerValue(value);
    }

    private static FloatValue floating(double value) {
        return new FloatValue(value);
    }

    private static void assertCode(ErrorCode code, Runnable operation) {
        C0deException e = assertThrows(C0deException.class, operation::run);
        assertEquals(code, e.code(), e.getMessage());
    }

    @Test
    void numbers() {
        assertEquals(integer(5), math("ABS", integer(-5)));
        assertEquals(floating(2.5), math("ABS", floating(-2.5)));
        assertCode(ErrorCode.NUMERIC_OVERFLOW, () -> math("ABS", integer(Long.MIN_VALUE)));
        assertEquals(integer(-1), math("SGN", floating(-0.1)));
        assertEquals(integer(0), math("SGN", integer(0)));
        assertEquals(integer(1), math("SGN", integer(7)));
        assertEquals(floating(3.0), math("SQR", integer(9)));
        assertCode(ErrorCode.INVALID_ARGUMENT, () -> math("SQR", integer(-1)));
        assertEquals(floating(1.0), math("EXP", integer(0)));
        assertEquals(floating(0.0), math("LOG", integer(1)));
        assertCode(ErrorCode.INVALID_ARGUMENT, () -> math("LOG", integer(0)));
        assertEquals(floating(0.0), math("SIN", integer(0)));
        assertEquals(floating(1.0), math("COS", integer(0)));
        assertEquals(floating(0.0), math("TAN", integer(0)));
        assertEquals(floating(Math.PI / 4), math("ATN", integer(1)));
        assertEquals(new FloatValue(Math.PI), MATH.constants().get("PI"));
    }

    @Test
    void rounding() {
        assertEquals(integer(-13), math("INT", floating(-12.85)));
        assertEquals(integer(12), math("INT", floating(12.85)));
        assertEquals(integer(-12), math("FIX", floating(-12.85)));
        assertEquals(integer(12), math("ROUND", floating(12.5)));
        assertEquals(integer(14), math("ROUND", floating(13.5)));
        assertEquals(integer(-2), math("ROUND", floating(-2.5)));
        assertEquals(integer(3), math("ROUND", integer(3)), "INTEGER values are accepted");
        assertCode(ErrorCode.NUMERIC_OVERFLOW, () -> math("FIX", floating(1e19)));
        assertEquals(floating(4.0), math("TO_FLOAT", integer(4)));
    }

    @Test
    void integerDivision() {
        assertEquals(
                integer(3),
                FakeCall.of(MATH, "DIV")
                        .with("dividend", integer(7))
                        .with("divisor", integer(2))
                        .result());
        assertEquals(
                integer(-3),
                FakeCall.of(MATH, "DIV")
                        .with("dividend", integer(-7))
                        .with("divisor", integer(2))
                        .result());
        assertCode(
                ErrorCode.DIVISION_BY_ZERO,
                () -> FakeCall.of(MATH, "DIV")
                        .with("dividend", integer(1))
                        .with("divisor", integer(0))
                        .result());
        assertCode(
                ErrorCode.NUMERIC_OVERFLOW,
                () -> FakeCall.of(MATH, "DIV")
                        .with("dividend", integer(Long.MIN_VALUE))
                        .with("divisor", integer(-1))
                        .result());
    }

    @Test
    void randomNumbersAreReproducibleAfterRandomize() {
        FakeCall call = FakeCall.of(MATH, "RANDOM");
        call.randomize(7);
        double first = ((FloatValue) call.result()).value();
        double second = ((FloatValue) call.result()).value();
        assertTrue(first >= 0 && first < 1 && second >= 0 && second < 1);
        FakeCall again = FakeCall.of(MATH, "RANDOMIZE").with("seed", integer(7));
        again.call();
        assertEquals(first, again.random().nextDouble());
    }

    private static Value time(String function) {
        return FakeCall.of(TIME, function).result();
    }

    private static Value time(String function, String isoTime) {
        return FakeCall.of(TIME, function)
                .with("time", new TimeValue(Literals.parseTime(isoTime, ZoneOffset.UTC)))
                .result();
    }

    @Test
    void partsOfTheCurrentTimeInTheServerTimeZone() {
        // 2026-10-19T12:05:09.250Z is 14:05:09.250 in Berlin (CEST), a Monday in ISO week 43
        assertEquals(new TimeValue(FakeCall.NOW), time("NOW"));
        assertEquals(integer(250), time("MILLISECOND"));
        assertEquals(integer(9), time("SECOND"));
        assertEquals(integer(5), time("MINUTE"));
        assertEquals(integer(14), time("HOUR"));
        assertEquals(integer(19), time("DAY"));
        assertEquals(integer(10), time("MONTH"));
        assertEquals(integer(2026), time("YEAR"));
        assertEquals(integer(1), time("WEEKDAY"));
        assertEquals(integer(43), time("WEEK"));
        assertEquals(floating(14 * 3600 + 5 * 60 + 9.25), time("TIMER"));
    }

    @Test
    void isoWeeksAroundNewYear() {
        // 2027-01-01 is a Friday in week 53 of 2026; 2027-01-04 is a Monday in week 1 of 2027
        assertEquals(integer(53), time("WEEK", "2027-01-01T12:00:00Z"));
        assertEquals(integer(5), time("WEEKDAY", "2027-01-01T12:00:00Z"));
        assertEquals(integer(1), time("WEEK", "2027-01-04T12:00:00Z"));
        // 23:30 UTC on New Year's Eve is already the next year in Berlin
        assertEquals(integer(2027), time("YEAR", "2026-12-31T23:30:00Z"));
    }

    @Test
    void sleepWaitsTicks() {
        assertEquals(
                20, FakeCall.of(TIME, "SLEEP").with("ticks", integer(20)).call().waitSteps());
        assertCode(
                ErrorCode.INVALID_ARGUMENT,
                () -> FakeCall.of(TIME, "SLEEP").with("ticks", integer(0)).call());
        assertCode(
                ErrorCode.INVALID_ARGUMENT,
                () -> FakeCall.of(TIME, "SLEEP").with("ticks", integer(72_001)).call());
    }
}
