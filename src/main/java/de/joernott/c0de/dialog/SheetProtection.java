package de.joernott.c0de.dialog;

import de.joernott.c0de.core.sheet.SheetGeometry;
import de.joernott.c0de.platform.Platform;
import de.joernott.c0de.process.ProcessManager;
import de.joernott.c0de.sheet.LevelSheet;
import de.joernott.c0de.sheet.SheetOwners;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Protection of sheets (docs/concept/13-running-programs.md, docs/concept/01-dimension.md): no block can be placed
 * on or removed from a sheet with running processes, from the floor up to the maximum build height, and the
 * dialogs of its blocks open read-only. The same applies to players who are neither the owner of the sheet
 * ({@link SheetOwners}) nor administrators. Language blocks can't be moved by pistons or destroyed by explosions
 * at all.
 */
public final class SheetProtection {
    private SheetProtection() {}

    /** Registers the checks of players breaking and placing blocks. Called once when the mod is initialized. */
    public static void register() {
        Platform.INSTANCE.onBlockChange((player, level, pos) -> reason(player, level, pos, true));
    }

    /** True if the block at the position can't be changed because a process of its program is running. */
    public static boolean isProtected(Level level, BlockPos pos) {
        return reason(level, pos).isPresent();
    }

    /**
     * The message for a player who may not change the block at the position: a process of its program is running,
     * or the sheet belongs to another player.
     *
     * @param claim true if the player changes the sheet now, so that an unowned sheet becomes theirs
     */
    public static Optional<Component> reason(Player player, Level level, BlockPos pos, boolean claim) {
        Optional<Component> running = reason(level, pos);
        if (running.isPresent()
                || !(player instanceof ServerPlayer serverPlayer)
                || !(level instanceof ServerLevel serverLevel)
                || pos.getY() < SheetGeometry.FLOOR_Y
                || !LevelSheet.isOnSheet(level, pos)) {
            return running;
        }
        return SheetOwners.get(serverLevel).check(serverPlayer, LevelSheet.sheetOf(pos), claim);
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
