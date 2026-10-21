package de.joernott.c0de.core.edit;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;

/**
 * How lines move when the wrench moves or copies the marked lines or the hammer deletes lines
 * (docs/concept/14-items-and-tools.md). The results map the z coordinate of every line that changes its place to
 * its new z coordinate; deleted lines map to {@link #DELETED}.
 */
public final class LineMoves {
    /** A deleted line in the result of {@link #delete}. */
    public static final int DELETED = -1;

    private LineMoves() {}

    /**
     * Moves the marked lines in front of (north of) the line {@code target}, in their order. The marked lines
     * don't need to be consecutive; the other lines keep their order and close the gaps.
     *
     * @return the new places (empty if nothing changes), or empty if the target is marked
     */
    public static Optional<Map<Integer, Integer>> move(SortedSet<Integer> marked, int target) {
        if (marked.isEmpty() || marked.contains(target)) {
            return Optional.empty();
        }
        int from = Math.min(marked.first(), target);
        int to = Math.max(marked.last(), target - 1);
        List<Integer> order = new ArrayList<>();
        for (int z = from; z <= to; z++) {
            if (!marked.contains(z)) {
                order.add(z);
            }
        }
        int index = order.indexOf(target);
        order.addAll(index < 0 ? order.size() : index, marked);
        Map<Integer, Integer> moves = new LinkedHashMap<>();
        for (int i = 0; i < order.size(); i++) {
            if (order.get(i) != from + i) {
                moves.put(order.get(i), from + i);
            }
        }
        return Optional.of(moves);
    }

    /**
     * Makes room for {@code count} lines in front of the line {@code target}: the lines from {@code target} to
     * {@code end} (exclusive) move south.
     *
     * @return the new places
     */
    public static Map<Integer, Integer> insert(int target, int count, int end) {
        Map<Integer, Integer> moves = new LinkedHashMap<>();
        for (int z = end - 1; z >= target; z--) {
            moves.put(z, z + count);
        }
        return moves;
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
