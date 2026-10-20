package de.joernott.c0de.dialog;

import de.joernott.c0de.core.sheet.SheetGeometry;
import de.joernott.c0de.platform.Platform;
import de.joernott.c0de.process.ProcessManager;
import de.joernott.c0de.sheet.LevelSheet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

/**
 * Protection of sheets with running processes (docs/concept/13-running-programs.md): no block can be placed on
 * or removed from such a sheet, from the floor up to the maximum build height, and the dialogs of its blocks
 * open read-only. Language blocks can't be moved by pistons or destroyed by explosions at all.
 */
public final class SheetProtection {
    private SheetProtection() {}

    /** Registers the checks of players breaking and placing blocks. Called once when the mod is initialized. */
    public static void register() {
        Platform.INSTANCE.onBlockChange((player, level, pos) -> reason(level, pos));
    }

    /** True if the block at the position can't be changed because a process of its program is running. */
    public static boolean isProtected(Level level, BlockPos pos) {
        return reason(level, pos).isPresent();
    }

    /** The message for players who try to change a protected sheet: the program and the PIDs of its processes. */
    public static Optional<Component> reason(Level level, BlockPos pos) {
        if (level.isClientSide()
                || level.getServer() == null
                || pos.getY() < SheetGeometry.FLOOR_Y
                || !LevelSheet.isOnSheet(level, pos)) {
            return Optional.empty();
        }
        ProcessManager processes = ProcessManager.get(level.getServer());
        int sheet = LevelSheet.sheetOf(pos);
        List<Integer> pids = processes.pids(sheet);
        if (pids.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Component.translatable(
                "message.c0de.protected",
                processes.program(sheet).orElse("?"),
                pids.stream().map(String::valueOf).collect(Collectors.joining(", "))));
    }
}
