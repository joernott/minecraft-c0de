package de.joernott.c0de.core.cursor;

/** The state of a cursor. */
public enum CursorState {
    /** The cursor processes one line per step. */
    RUNNING,
    /** A debug cursor waits for the player; steps are only executed with {@link Cursor#stepOnce()}. */
    PAUSED,
    /** The process ended normally (end of the entry function, RETURN or EXIT). */
    FINISHED,
    /** The process ended with an error. */
    CRASHED
}
