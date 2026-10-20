package de.joernott.c0de.core.cursor;

import de.joernott.c0de.core.library.LibraryRegistry;
import java.time.Clock;
import java.time.ZoneId;

/**
 * The environment of a cursor.
 *
 * @param stackSize the number of slots of the stack (setting {@code stackSize})
 * @param libraries the available libraries and built-in functions
 * @param clock the clock for the time functions
 * @param zone the time zone of the server
 * @param host access to the mod for c0de/minecraft, or null
 * @param randomSeed the seed of the random number generator of the process
 */
public record CursorSettings(
        int stackSize, LibraryRegistry libraries, Clock clock, ZoneId zone, Object host, long randomSeed) {
    /** Default stack size of the concept. */
    public static final int DEFAULT_STACK_SIZE = 2048;

    /** Settings for tests: default stack size, system clock in UTC, no host. */
    public static CursorSettings of(LibraryRegistry libraries) {
        return new CursorSettings(DEFAULT_STACK_SIZE, libraries, Clock.systemUTC(), ZoneId.of("UTC"), null, 0);
    }

    public CursorSettings withStackSize(int size) {
        return new CursorSettings(size, libraries, clock, zone, host, randomSeed);
    }

    public CursorSettings withClock(Clock newClock) {
        return new CursorSettings(stackSize, libraries, newClock, zone, host, randomSeed);
    }
}
