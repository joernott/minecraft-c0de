package de.joernott.c0de.sheet;

import de.joernott.c0de.content.C0deEntities;
import de.joernott.c0de.core.sheet.SheetGeometry;
import de.joernott.c0de.entity.MarkEntity;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;

/**
 * The marked lines of the sheets (task 9.3). Every mark is a {@link MarkEntity} in the middle of the code area
 * of its line, so marks are saved with the chunks and shown to all players without further synchronization.
 */
public final class SheetMarks {
    /** Relative x of the middle of the code area (x=5 to x=28), where the marks stand. */
    public static final int MIDDLE_X = (SheetGeometry.CODE_MIN_X + SheetGeometry.CODE_MAX_X + 1) / 2;

    private SheetMarks() {}

    private static List<MarkEntity> marks(ServerLevel level, int sheet, int from, int to) {
        double x = SheetGeometry.absoluteX(sheet, MIDDLE_X);
        AABB area = new AABB(x - 0.5, SheetGeometry.CODE_Y - 1, from, x + 0.5, SheetGeometry.CODE_Y + 2, to + 1);
        // Bounding boxes of marks are as deep as they are wide, so only marks standing in the lines count
        return level.getEntitiesOfClass(MarkEntity.class, area, mark -> {
            int line = (int) Math.floor(mark.getZ());
            return line >= from && line <= to;
        });
    }

    /** All marked lines of a sheet (in loaded chunks), in ascending order. */
    public static java.util.SortedSet<Integer> marked(ServerLevel level, int sheet) {
        java.util.SortedSet<Integer> lines = new java.util.TreeSet<>();
        for (MarkEntity mark : marks(level, sheet, 0, LevelSheet.MAX_LINES)) {
            lines.add((int) Math.floor(mark.getZ()));
        }
        return lines;
    }

    /** True if the line is marked. */
    public static boolean isMarked(ServerLevel level, int sheet, int z) {
        return !marks(level, sheet, z, z).isEmpty();
    }

    /** Marks a line or removes its mark. */
    public static void toggle(ServerLevel level, int sheet, int z) {
        List<MarkEntity> existing = marks(level, sheet, z, z);
        if (!existing.isEmpty()) {
            existing.forEach(MarkEntity::discard);
            return;
        }
        MarkEntity mark = new MarkEntity(C0deEntities.MARK.get(), level);
        mark.setPos(SheetGeometry.absoluteX(sheet, MIDDLE_X), SheetGeometry.CODE_Y, z + 0.5);
        level.addFreshEntity(mark);
    }

    /** Marks a line or removes its mark; a line that is already in that state stays unchanged. */
    public static void set(ServerLevel level, int sheet, int z, boolean marked) {
        if (isMarked(level, sheet, z) != marked) {
            toggle(level, sheet, z);
        }
    }

    /**
     * The marked area that contains a line: the consecutive marked lines around it.
     *
     * @return the first and last line, or empty if the line is not marked
     */
    public static Optional<int[]> area(ServerLevel level, int sheet, int z) {
        if (!isMarked(level, sheet, z)) {
            return Optional.empty();
        }
        int from = z;
        while (from > 0 && isMarked(level, sheet, from - 1)) {
            from--;
        }
        int to = z;
        while (isMarked(level, sheet, to + 1)) {
            to++;
        }
        return Optional.of(new int[] {from, to});
    }

    /** Moves the marks with their lines; marks of deleted lines are removed. */
    public static void move(ServerLevel level, int sheet, Map<Integer, Integer> moves) {
        int from = moves.keySet().stream().mapToInt(Integer::intValue).min().orElse(0);
        int to = moves.keySet().stream().mapToInt(Integer::intValue).max().orElse(-1);
        for (MarkEntity mark : marks(level, sheet, from, to)) {
            Integer target = moves.get((int) Math.floor(mark.getZ()));
            if (target == null) {
                continue;
            }
            if (target < 0) {
                mark.discard();
            } else {
                mark.setPos(mark.getX(), mark.getY(), target + 0.5);
            }
        }
    }

    /** Removes the marks of some lines. */
    public static void clear(ServerLevel level, int sheet, int from, int to) {
        marks(level, sheet, from, to).forEach(MarkEntity::discard);
    }
}
