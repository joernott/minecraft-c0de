package de.joernott.c0de.core.error;

/**
 * An error of a c0de program. It either makes the cursor crash or is raised in the current function
 * (see docs/concept/15-cursor.md, "Error handling").
 */
public class C0deException extends RuntimeException {
    /** Value of {@link #line()} if the line is not known yet. */
    public static final int NO_LINE = -1;

    private final ErrorCode code;
    private final boolean crash;
    private final int line;
    private final boolean incomplete;

    private C0deException(ErrorCode code, String message, boolean crash, int line, boolean incomplete) {
        super(message);
        this.code = code;
        this.crash = crash;
        this.line = line;
        this.incomplete = incomplete;
    }

    /** An error that is handled as defined for its code. */
    public static C0deException of(ErrorCode code, String message) {
        return new C0deException(code, message, code.handling() == ErrorCode.Handling.CRASH, NO_LINE, false);
    }

    /** An error that makes the cursor crash, regardless of the handling of its code. */
    public static C0deException crash(ErrorCode code, String message) {
        return new C0deException(code, message, true, NO_LINE, false);
    }

    /** An error in a line, handled as defined for its code. */
    public static C0deException at(int line, ErrorCode code, String message) {
        return new C0deException(code, message, code.handling() == ErrorCode.Handling.CRASH, line, false);
    }

    /**
     * A syntax error of a statement that is not finished yet, e.g. a BEGIN line without END line or a FUNCTION
     * without code block at the end of the program. The lines written so far may be correct.
     */
    public static C0deException incomplete(int line, ErrorCode code, String message) {
        return new C0deException(code, message, code.handling() == ErrorCode.Handling.CRASH, line, true);
    }

    /** The same error, located in a line (if it isn't located yet). */
    public C0deException inLine(int line) {
        return this.line == NO_LINE ? new C0deException(code, getMessage(), crash, line, incomplete) : this;
    }

    /** The same error, but making the cursor crash. */
    public C0deException asCrash() {
        return crash ? this : new C0deException(code, getMessage(), true, line, incomplete);
    }

    public ErrorCode code() {
        return code;
    }

    /** True if the error makes the cursor crash, false if it is raised in the current function. */
    public boolean crash() {
        return crash;
    }

    /** True for a statement that is not finished yet, see {@link #incomplete}. */
    public boolean isIncomplete() {
        return incomplete;
    }

    /** The z coordinate of the line the error occurred in, or {@link #NO_LINE}. */
    public int line() {
        return line;
    }

    @Override
    public String toString() {
        return code + (line == NO_LINE ? "" : " in line " + line) + ": " + getMessage();
    }
}
