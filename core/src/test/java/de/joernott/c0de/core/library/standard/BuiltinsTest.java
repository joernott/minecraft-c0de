package de.joernott.c0de.core.library.standard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.joernott.c0de.core.error.C0deException;
import de.joernott.c0de.core.error.ErrorCode;
import de.joernott.c0de.core.library.Library;
import de.joernott.c0de.core.library.NativeResult;
import de.joernott.c0de.core.value.ArrayValue;
import de.joernott.c0de.core.value.BooleanValue;
import de.joernott.c0de.core.value.HashValue;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.StringValue;
import de.joernott.c0de.core.value.Type;
import de.joernott.c0de.core.value.Value;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** The built-in functions (task 5.1). */
class BuiltinsTest {
    private static final Library BUILTINS = new Builtins();

    private static IntegerValue integer(long value) {
        return new IntegerValue(value);
    }

    private static ArrayValue array(long... values) {
        return new ArrayValue(
                Type.Simple.INTEGER,
                java.util.Arrays.stream(values).mapToObj(IntegerValue::new).toList());
    }

    private static HashValue hash() {
        return new HashValue(Type.Simple.INTEGER, Map.of("a", integer(1), "b", integer(2)));
    }

    private static void assertCode(ErrorCode code, Runnable operation) {
        C0deException e = assertThrows(C0deException.class, operation::run);
        assertEquals(code, e.code(), e.getMessage());
    }

    @Test
    void lengthAndKeys() {
        assertEquals(
                integer(2),
                FakeCall.of(BUILTINS, "LEN")
                        .with("value", new StringValue("😀x"))
                        .result());
        assertEquals(
                integer(3),
                FakeCall.of(BUILTINS, "LEN").with("value", array(1, 2, 3)).result());
        assertEquals(
                integer(2), FakeCall.of(BUILTINS, "LEN").with("value", hash()).result());
        assertEquals(
                BooleanValue.TRUE,
                FakeCall.of(BUILTINS, "HAS_KEY")
                        .with("hash", hash())
                        .with("key", new StringValue("a"))
                        .result());
        assertEquals(
                BooleanValue.FALSE,
                FakeCall.of(BUILTINS, "HAS_KEY")
                        .with("hash", hash())
                        .with("key", new StringValue("c"))
                        .result());
    }

    @Test
    void removeAndInsertReturnChangedCopies() {
        ArrayValue original = array(1, 2, 3);
        assertEquals(
                array(1, 3),
                FakeCall.of(BUILTINS, "REMOVE")
                        .with("container", original)
                        .with("index", integer(1))
                        .result());
        assertEquals(array(1, 2, 3), original, "the argument is not changed");
        HashValue removed = (HashValue) FakeCall.of(BUILTINS, "REMOVE")
                .with("container", hash())
                .with("key", new StringValue("a"))
                .result();
        assertEquals(List.of("b"), List.copyOf(removed.elements().keySet()));
        assertCode(
                ErrorCode.INDEX_OUT_OF_BOUNDS,
                () -> FakeCall.of(BUILTINS, "REMOVE")
                        .with("container", original)
                        .with("index", integer(3))
                        .result());
        assertCode(
                ErrorCode.UNKNOWN_KEY,
                () -> FakeCall.of(BUILTINS, "REMOVE")
                        .with("container", hash())
                        .with("key", new StringValue("x"))
                        .result());
        assertCode(
                ErrorCode.INVALID_ARGUMENT,
                () -> FakeCall.of(BUILTINS, "REMOVE")
                        .with("container", original)
                        .result());

        assertEquals(
                array(1, 9, 2, 3),
                FakeCall.of(BUILTINS, "INSERT")
                        .with("array", original)
                        .with("index", integer(1))
                        .with("value", integer(9))
                        .result());
        assertEquals(
                array(1, 2, 3, 9),
                FakeCall.of(BUILTINS, "INSERT")
                        .with("array", original)
                        .with("index", integer(3))
                        .with("value", integer(9))
                        .result());
        assertCode(
                ErrorCode.INDEX_OUT_OF_BOUNDS,
                () -> FakeCall.of(BUILTINS, "INSERT")
                        .with("array", original)
                        .with("index", integer(4))
                        .with("value", integer(9))
                        .result());
        assertCode(
                ErrorCode.TYPE_MISMATCH,
                () -> FakeCall.of(BUILTINS, "INSERT")
                        .with("array", original)
                        .with("index", integer(0))
                        .with("value", new StringValue("x"))
                        .result());
    }

    @Test
    void clearAndSwap() {
        assertEquals(
                array(),
                FakeCall.of(BUILTINS, "CLEAR").with("container", array(1, 2)).result());
        assertEquals(
                new HashValue(Type.Simple.INTEGER),
                FakeCall.of(BUILTINS, "CLEAR").with("container", hash()).result());
        Map<String, Value> swapped = FakeCall.of(BUILTINS, "SWAP")
                .with("a", integer(1))
                .with("b", integer(2))
                .call()
                .results();
        assertEquals(Map.of("a", integer(2), "b", integer(1)), swapped);
        assertCode(
                ErrorCode.TYPE_MISMATCH,
                () -> FakeCall.of(BUILTINS, "SWAP")
                        .with("a", integer(1))
                        .with("b", new StringValue("2"))
                        .call());
    }

    @Test
    void raiseReturnsAnErrorOfTheCaller() {
        NativeResult result =
                FakeCall.of(BUILTINS, "RAISE").with("code", integer(1001)).call();
        assertEquals(1001, result.errorCode());
        assertEquals("", result.errorMessage());
        assertTrue(result.callerContext());
        assertCode(
                ErrorCode.INVALID_ARGUMENT,
                () -> FakeCall.of(BUILTINS, "RAISE").with("code", integer(999)).call());
    }
}
