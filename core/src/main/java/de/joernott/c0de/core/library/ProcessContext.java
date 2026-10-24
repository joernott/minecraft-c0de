package de.joernott.c0de.core.library;

import java.time.Clock;
import java.time.ZoneId;
import java.util.random.RandomGenerator;

/** What a native function can access of the process that calls it. */
public interface ProcessContext {
    /** The random number generator of the process (RANDOM, RANDOMIZE). */
    RandomGenerator random();

    /** Replaces the random number generator of the process with one seeded with this value (RANDOMIZE). */
    void randomize(long seed);

    /** The clock (NOW, TIMER, ...). */
    Clock clock();

    /** The time zone of the server. */
    ZoneId zone();

    /**
     * Access to the mod, e.g. the world and the trigger block for c0de/minecraft. Null in tests and for
     * processes without a host.
     */
    Object host();

    /** The devices of the process: screens, I/O blocks and terminals (docs/concept/19-input-output.md). */
    default de.joernott.c0de.core.io.Console console() {
        return de.joernott.c0de.core.io.Console.NONE;
    }
}
