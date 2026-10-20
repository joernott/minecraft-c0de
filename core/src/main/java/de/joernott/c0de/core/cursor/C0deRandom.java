package de.joernott.c0de.core.cursor;

import java.util.random.RandomGenerator;

/**
 * The random number generator of a process (SplitMix64). Its state is a single number, so it can be
 * stored with the cursor and continue the same sequence after a server restart.
 */
public final class C0deRandom implements RandomGenerator {
    private long state;

    public C0deRandom(long seed) {
        this.state = seed;
    }

    /** The current state, for storing the cursor. */
    public long state() {
        return state;
    }

    @Override
    public long nextLong() {
        long z = (state += 0x9E3779B97F4A7C15L);
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }
}
