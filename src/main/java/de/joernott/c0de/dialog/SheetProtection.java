package de.joernott.c0de.dialog;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Protection of sheets with running processes (docs/concept/13-running-programs.md). Processes exist from
 * sprint 8 on (task 8.6); until then, no sheet is protected.
 */
public final class SheetProtection {
    private SheetProtection() {}

    /** True if the block at the position can't be changed because a process of its program is running. */
    public static boolean isProtected(Level level, BlockPos pos) {
        return false;
    }
}
