package de.joernott.c0de.core.error;

import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.Value;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** The error codes of the language (see docs/concept/16-error-codes.md). */
public enum ErrorCode {
    OK(0, Handling.NONE),
    SYNTAX_ERROR_INDENTATION(100, Handling.CRASH),
    SYNTAX_ERROR_PLACEMENT(101, Handling.CRASH),
    SYNTAX_ERROR_BEGIN_WITHOUT_END(102, Handling.CRASH),
    SYNTAX_ERROR_UNKNOWN_STATEMENT(103, Handling.CRASH),
    SYNTAX_ERROR_STRUCT(104, Handling.CRASH),
    SYNTAX_ERROR_VARIABLE(105, Handling.CRASH),
    SYNTAX_ERROR_CONSTANT(106, Handling.CRASH),
    SYNTAX_ERROR_TYPE_MISMATCH(107, Handling.CRASH),
    SYNTAX_ERROR_FUNCTION(108, Handling.CRASH),
    SYNTAX_ERROR_CALL(109, Handling.CRASH),
    SYNTAX_ERROR_ASSIGN(110, Handling.CRASH),
    SYNTAX_ERROR_CONDITION(111, Handling.CRASH),
    SYNTAX_ERROR_LOOP(112, Handling.CRASH),
    SYNTAX_ERROR_EXPRESSION(113, Handling.CRASH),
    SYNTAX_ERROR_EXIT(114, Handling.CRASH),
    UNKNOWN_LIBRARY(200, Handling.CRASH),
    BROKEN_LIBRARY(201, Handling.CRASH),
    UNKNOWN_FUNCTION(202, Handling.CRASH),
    STACK_OVERFLOW(203, Handling.CRASH),
    FUNCTION_MISSING_INPUT(204, Handling.CRASH),
    INVALID_EXIT_CODE(205, Handling.CRASH),
    PROCESS_ABORTED(206, Handling.CRASH),
    UNKNOWN_NAME(300, Handling.RAISE),
    UNKNOWN_STRUCT_ENTITY(301, Handling.RAISE),
    TYPE_MISMATCH(302, Handling.RAISE),
    DIVISION_BY_ZERO(303, Handling.RAISE),
    NUMERIC_OVERFLOW(304, Handling.RAISE),
    INDEX_OUT_OF_BOUNDS(305, Handling.RAISE),
    UNKNOWN_KEY(306, Handling.RAISE),
    STRING_TOO_LONG(307, Handling.RAISE),
    INVALID_LOOP_STEP(308, Handling.RAISE),
    INVALID_ARGUMENT(309, Handling.RAISE),
    CHUNK_NOT_LOADED(310, Handling.RAISE),
    PERMISSION_DENIED(311, Handling.RAISE),
    INVENTORY_FULL(312, Handling.RAISE),
    ITEM_MISSING(313, Handling.RAISE);

    /** Lowest code of user-defined errors. */
    public static final int FIRST_USER_CODE = 1000;

    private final int code;
    private final Handling handling;

    ErrorCode(int code, Handling handling) {
        this.code = code;
        this.handling = handling;
    }

    /** The number of the error, stored in {@code error.code}. */
    public int code() {
        return code;
    }

    /**
     * Whether the error makes the cursor crash or is raised in the current function. Some errors that are
     * normally raised crash when they occur in a declaration (see {@link C0deException#crash()}).
     */
    public Handling handling() {
        return handling;
    }

    /** The error with this number, or null for user-defined and unknown numbers. */
    public static ErrorCode of(int code) {
        for (ErrorCode error : values()) {
            if (error.code == code) {
                return error;
            }
        }
        return null;
    }

    /** The built-in INTEGER constants with the names of all error codes, e.g. {@code DIVISION_BY_ZERO = 303}. */
    public static Map<String, Value> builtinConstants() {
        Map<String, Value> constants = new LinkedHashMap<>();
        for (ErrorCode error : values()) {
            constants.put(error.name(), new IntegerValue(error.code));
        }
        return Collections.unmodifiableMap(constants);
    }

    /** How an error is handled. */
    public enum Handling {
        /** Not an error. */
        NONE,
        /** The process ends immediately. */
        CRASH,
        /** The current function ends and the error is passed to the caller. */
        RAISE
    }
}
