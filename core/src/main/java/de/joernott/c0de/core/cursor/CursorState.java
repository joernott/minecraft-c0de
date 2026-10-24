package de.joernott.c0de.core.cursor;

/** The state of a cursor. */
public enum CursorState {
    /** The cursor processes one line per step. */
    RUNNING,
    /**
     * A running cursor waits for a value (INPUT) or for room (WRITE): it stays on the CALL line and tries again
     * in every step. It runs like a RUNNING cursor.
     */
    WAITING,
    /** A debug cursor waits for the player; steps are only executed with {@link Cursor#stepOnce()}. */
    PAUSED,
    /** The process ended normally (end of the entry function, RETURN or EXIT). */
    FINISHED,
    /** The process ended with an error. */
    CRASHED
}
