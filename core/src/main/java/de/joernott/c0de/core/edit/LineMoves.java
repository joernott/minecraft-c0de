package de.joernott.c0de.core.edit;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * How lines move when the wrench moves marked lines or the hammer deletes lines
 * (docs/concept/14-items-and-tools.md). The result maps the z coordinate of every line that changes its place to
 * its new z coordinate; deleted lines map to -1.
 */
public final class LineMoves {
    /** A deleted line in the result of {@link #delete}. */
    public static final int DELETED = -1;

    private LineMoves() {}

    /**
     * Moves the lines {@code from} to {@code to} in front of (north of) the line {@code target}.
     *
     * @return the new places, or empty if the target is inside the moved lines or directly after them
     */
    public static Optional<Map<Integer, Integer>> move(int from, int to, int target) {
        if (from > to || from < 0 || (target >= from && target <= to + 1)) {
            return Optional.empty();
        }
        int count = to - from + 1;
        Map<Integer, Integer> moves = new LinkedHashMap<>();
        if (target < from) {
            for (int z = from; z <= to; z++) {
                moves.put(z, target + z - from);
            }
            for (int z = target; z < from; z++) {
                moves.put(z, z + count);
            }
        } else {
            for (int z = from; z <= to; z++) {
                moves.put(z, target - count + z - from);
            }
            for (int z = to + 1; z < target; z++) {
                moves.put(z, z - count);
            }
        }
        return Optional.of(moves);
    }

    /**
     * Deletes the lines {@code from} to {@code to}; the following lines up to {@code end} (exclusive) move up.
     *
     * @return the new places
     */
    public static Map<Integer, Integer> delete(int from, int to, int end) {
        int count = to - from + 1;
        Map<Integer, Integer> moves = new LinkedHashMap<>();
        for (int z = from; z <= to; z++) {
            moves.put(z, DELETED);
        }
        for (int z = to + 1; z < end; z++) {
            moves.put(z, z - count);
        }
        return moves;
    }
}
