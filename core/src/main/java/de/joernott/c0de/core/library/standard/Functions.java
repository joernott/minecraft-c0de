package de.joernott.c0de.core.library.standard;

import de.joernott.c0de.core.error.C0deException;
import de.joernott.c0de.core.error.ErrorCode;
import de.joernott.c0de.core.library.NativeCall;
import de.joernott.c0de.core.library.NativeFunction;
import de.joernott.c0de.core.library.NativeResult;
import de.joernott.c0de.core.value.ArrayValue;
import de.joernott.c0de.core.value.BooleanValue;
import de.joernott.c0de.core.value.CharValue;
import de.joernott.c0de.core.value.HashValue;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.StringValue;
import de.joernott.c0de.core.value.TimeValue;
import de.joernott.c0de.core.value.Value;
import de.joernott.c0de.core.value.Values;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/** Helpers for defining native functions. */
final class Functions {
    static final Predicate<Value> ANY = value -> true;
    static final Predicate<Value> INTEGER = value -> value instanceof IntegerValue;
    static final Predicate<Value> NUMBER = Values::isNumber;
    static final Predicate<Value> BOOLEAN = value -> value instanceof BooleanValue;
    static final Predicate<Value> CHAR = value -> value instanceof CharValue;
    static final Predicate<Value> TIME = value -> value instanceof TimeValue;
    /** A STRING or a CHAR (which can be used wherever a STRING is expected). */
    static final Predicate<Value> TEXT = value -> value instanceof StringValue || value instanceof CharValue;

    static final Predicate<Value> ARRAY = value -> value instanceof ArrayValue;
    static final Predicate<Value> HASH = value -> value instanceof HashValue;

    /** Largest number of characters a function creates (SPACE, FILL), to protect the server's memory. */
    static final int MAX_TEXT_LENGTH = 1_000_000;

    private Functions() {}

    /** Collects the functions of a library by name. */
    static final class Builder {
        private final Map<String, NativeFunction> functions = new LinkedHashMap<>();

        Builder add(
                String name,
                List<NativeFunction.Parameter> parameters,
                List<String> results,
                NativeFunction.Implementation implementation) {
            functions.put(name, new NativeFunction(name, parameters, results, implementation));
            return this;
        }

        /** A function with one result named {@code result}. */
        Builder add(String name, List<NativeFunction.Parameter> parameters, ResultFunction implementation) {
            return add(name, parameters, List.of("result"), call -> NativeResult.of(implementation.apply(call)));
        }

        Map<String, NativeFunction> build() {
            return Map.copyOf(functions);
        }
    }

    /** The code of a function with a single result. */
    @FunctionalInterface
    interface ResultFunction {
        Value apply(NativeCall call);
    }

    static NativeFunction.Parameter required(String name, String types, Predicate<Value> accepts) {
        return NativeFunction.Parameter.required(name, types, accepts);
    }

    static NativeFunction.Parameter optional(String name, String types, Predicate<Value> accepts, Value defaultValue) {
        return NativeFunction.Parameter.withDefault(name, types, accepts, defaultValue);
    }

    static NativeFunction.Parameter optional(String name, String types, Predicate<Value> accepts) {
        return NativeFunction.Parameter.optional(name, types, accepts);
    }

    static long integer(NativeCall call, String name) {
        return ((IntegerValue) call.argument(name)).value();
    }

    static double number(NativeCall call, String name) {
        return Values.toDouble(call.argument(name));
    }

    /** A STRING or CHAR argument as text. */
    static String text(NativeCall call, String name) {
        return call.argument(name).text();
    }

    static int[] codePoints(String text) {
        return text.codePoints().toArray();
    }

    static String fromCodePoints(int[] codePoints, int from, int to) {
        return new String(codePoints, from, Math.max(0, to - from));
    }

    /** An int position or length; values outside of 0 to {@link Integer#MAX_VALUE} raise INVALID_ARGUMENT. */
    static int nonNegative(long value, String what) {
        if (value < 0 || value > Integer.MAX_VALUE) {
            throw invalid(what + " must not be negative: " + value);
        }
        return (int) value;
    }

    static C0deException invalid(String message) {
        return C0deException.of(ErrorCode.INVALID_ARGUMENT, message);
    }

    static C0deException overflow() {
        return C0deException.of(ErrorCode.NUMERIC_OVERFLOW, "The result doesn't fit into an INTEGER");
    }

    /** A double as long; values outside of the range of INTEGER raise NUMERIC_OVERFLOW. */
    static long toLong(double value) {
        if (Double.isNaN(value) || value < -0x1p63 || value >= 0x1p63) {
            throw overflow();
        }
        return (long) value;
    }
}
