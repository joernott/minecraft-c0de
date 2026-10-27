package de.joernott.c0de.core.cursor;

import de.joernott.c0de.core.library.LibraryRegistry;
import de.joernott.c0de.core.value.Value;
import java.time.Clock;
import java.time.ZoneId;
import java.util.Map;

/**
 * The environment of a cursor.
 *
 * @param stackSize the number of slots of the stack (setting {@code stackSize})
 * @param libraries the available libraries and built-in functions
 * @param clock the clock for the time functions
 * @param zone the time zone of the server
 * @param host access to the mod for c0de/minecraft, or null
 * @param randomSeed the seed of the random number generator of the process
 * @param constants the values of built-in constants for this process, e.g. SHEET and PID (tasks 13.4, 13.5); they
 *     replace the default values of the built-in functions
 */
public record CursorSettings(
        int stackSize,
        LibraryRegistry libraries,
        Clock clock,
        ZoneId zone,
        Object host,
        long randomSeed,
        Map<String, Value> constants) {
    /** Default stack size of the concept. */
    public static final int DEFAULT_STACK_SIZE = 2048;

    public CursorSettings {
        constants = Map.copyOf(constants);
    }

    /** Settings without values for the built-in constants of a process. */
    public CursorSettings(
            int stackSize, LibraryRegistry libraries, Clock clock, ZoneId zone, Object host, long randomSeed) {
        this(stackSize, libraries, clock, zone, host, randomSeed, Map.of());
    }

    /** Settings for tests: default stack size, system clock in UTC, no host. */
    public static CursorSettings of(LibraryRegistry libraries) {
        return new CursorSettings(DEFAULT_STACK_SIZE, libraries, Clock.systemUTC(), ZoneId.of("UTC"), null, 0);
    }

    public CursorSettings withStackSize(int size) {
        return new CursorSettings(size, libraries, clock, zone, host, randomSeed, constants);
    }

    public CursorSettings withClock(Clock newClock) {
        return new CursorSettings(stackSize, libraries, newClock, zone, host, randomSeed, constants);
    }

    public CursorSettings withConstants(Map<String, Value> newConstants) {
        return new CursorSettings(stackSize, libraries, clock, zone, host, randomSeed, newConstants);
    }
}
