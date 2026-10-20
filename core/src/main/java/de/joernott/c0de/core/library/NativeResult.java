package de.joernott.c0de.core.library;

import de.joernott.c0de.core.error.C0deException;
import de.joernott.c0de.core.value.Value;
import java.util.Map;

/**
 * The outcome of a native function: result values, or an error, and optionally a number of steps the
 * cursor waits before it continues (SLEEP).
 *
 * <p>Errors can also be reported by throwing a {@link C0deException}; it is raised (or makes the cursor
 * crash) at the CALL statement like an error of a program function.
 *
 * @param results the result values by result name
 * @param errorCode the code of the returned error, 0 if there is none
 * @param errorMessage the message of the returned error
 * @param waitSteps the number of steps the cursor waits
 */
public record NativeResult(Map<String, Value> results, long errorCode, String errorMessage, int waitSteps) {
    public NativeResult {
        results = Map.copyOf(results);
    }

    /** No results. */
    public static NativeResult none() {
        return new NativeResult(Map.of(), 0, "", 0);
    }

    /** A single result named {@code result}. */
    public static NativeResult of(Value result) {
        return new NativeResult(Map.of("result", result), 0, "", 0);
    }

    /** Several results. */
    public static NativeResult of(Map<String, Value> results) {
        return new NativeResult(results, 0, "", 0);
    }

    /** An error returned to the caller (e.g. by RAISE). */
    public static NativeResult error(long code, String message) {
        return new NativeResult(Map.of(), code, message, 0);
    }

    /** The cursor waits the given number of steps (SLEEP). */
    public static NativeResult waitSteps(int steps) {
        return new NativeResult(Map.of(), 0, "", steps);
    }
}
