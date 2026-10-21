package de.joernott.c0de.tool;

import de.joernott.c0de.block.CodeBlock;
import de.joernott.c0de.block.TriggerBlock;
import de.joernott.c0de.blockentity.TriggerBlockEntity;
import de.joernott.c0de.content.C0deItems;
import de.joernott.c0de.core.edit.LineMoves;
import de.joernott.c0de.core.sheet.SheetGeometry;
import de.joernott.c0de.dialog.SheetProtection;
import de.joernott.c0de.entity.CursorEntity;
import de.joernott.c0de.item.DebugToolItem;
import de.joernott.c0de.item.WrenchItem;
import de.joernott.c0de.network.ToolPayload;
import de.joernott.c0de.platform.Platform;
import de.joernott.c0de.process.ProcessManager;
import de.joernott.c0de.sheet.LevelSheet;
import de.joernott.c0de.sheet.SheetEditor;
import de.joernott.c0de.sheet.SheetMarks;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * The left clicks of the c0de tools (docs/concept/14-items-and-tools.md):
 *
 * <ul>
 *   <li>debugging tool: starts a debug process at a debug trigger block, controls debug cursors
 *   <li>text marker: marks a line or removes its mark
 *   <li>wrench: selects a marked area, which a right click on another line moves (see {@link #wrenchTarget})
 *   <li>hammer: deletes a marked area or a single line; the following lines move up
 * </ul>
 */
public final class ToolActions {
    /** Extra distance to the interaction range of a player, as for dialogs. */
    private static final double REACH_TOLERANCE = 2.0;

    /** The marked areas selected with the wrench, by player. Only used on the server thread. */
    private static final Map<UUID, Selection> SELECTIONS = new HashMap<>();

    private ToolActions() {}

    /** A marked area selected with the wrench. */
    private record Selection(int sheet, int from, int to) {}

    /** Registers the handlers of left clicks. Called once when the mod is initialized. */
    public static void register() {
        Platform.INSTANCE.onAttackBlock(ToolActions::attackBlock);
        Platform.INSTANCE.onAttackEntity(ToolActions::attackEntity);
        Platform.INSTANCE.registerServerbound(ToolPayload.TYPE, ToolPayload.CODEC, ToolActions::handle);
    }

    private static boolean isTool(Item item) {
        return item instanceof DebugToolItem
                || item == C0deItems.MARKER.get()
                || item == C0deItems.HAMMER.get()
                || item instanceof WrenchItem;
    }

    private static boolean attackBlock(Player player, Level level, BlockPos pos) {
        if (!isTool(player.getMainHandItem().getItem())) {
            return false;
        }
        if (level.isClientSide()) {
            Platform.INSTANCE.sendToServer(new ToolPayload(pos, -1));
        }
        return true;
    }

    private static boolean attackEntity(Player player, Level level, Entity entity) {
        if (!(player.getMainHandItem().getItem() instanceof DebugToolItem) || !(entity instanceof CursorEntity)) {
            return false;
        }
        if (level.isClientSide()) {
            Platform.INSTANCE.sendToServer(new ToolPayload(entity.blockPosition(), entity.getId()));
        }
        return true;
    }

    private static void message(ServerPlayer player, String key, Object... arguments) {
        player.displayClientMessage(Component.translatable(key, arguments), true);
    }

    /** Executes a left click on the server. */
    public static void handle(ToolPayload payload, ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Item item = player.getMainHandItem().getItem();
        if (payload.entity() >= 0) {
            Entity entity = level.getEntity(payload.entity());
            if (item instanceof DebugToolItem
                    && entity instanceof CursorEntity cursor
                    && player.distanceTo(cursor) <= player.entityInteractionRange() + REACH_TOLERANCE) {
                message(
                        player,
                        "message.c0de.debug",
                        ProcessManager.get(player.server).debug(player.server, cursor.process(), player.isShiftKeyDown()));
            }
            return;
        }
        BlockPos pos = payload.pos();
        if (!level.isLoaded(pos) || !player.canInteractWithBlock(pos, REACH_TOLERANCE)) {
            return;
        }
        if (item instanceof DebugToolItem) {
            if (level.getBlockState(pos).getBlock() instanceof TriggerBlock block
                    && block.kind() == TriggerBlock.Kind.DEBUG
                    && level.getBlockEntity(pos) instanceof TriggerBlockEntity trigger) {
                trigger.startDebug()
                        .ifPresentOrElse(
                                problem -> message(player, "message.c0de.not_started", problem),
                                () -> message(player, "message.c0de.debug_started", trigger.pid()));
            }
            return;
        }
        if (!LevelSheet.isOnSheet(level, pos)
                || pos.getY() != SheetGeometry.CODE_Y
                || !SheetGeometry.isInCodeArea(pos.getX(), pos.getZ())) {
            return;
        }
        int sheet = LevelSheet.sheetOf(pos);
        int z = pos.getZ();
        if (item == C0deItems.MARKER.get()) {
            // Marking doesn't change the program, so it is allowed while processes run
            SheetMarks.toggle(level, sheet, z);
        } else if (item instanceof WrenchItem) {
            Optional<int[]> area = SheetMarks.area(level, sheet, z);
            if (area.isEmpty()) {
                message(player, "message.c0de.not_marked");
                return;
            }
            SELECTIONS.put(player.getUUID(), new Selection(sheet, area.get()[0], area.get()[1]));
            message(player, "message.c0de.selected", area.get()[0], area.get()[1]);
        } else if (item == C0deItems.HAMMER.get()) {
            hammer(player, level, pos, sheet, z);
        }
    }

    private static boolean isProtected(ServerPlayer player, ServerLevel level, BlockPos pos) {
        Optional<Component> reason = SheetProtection.reason(level, pos);
        reason.ifPresent(message -> player.displayClientMessage(message, true));
        return reason.isPresent();
    }

    /** Deletes the marked area that contains the line, or the line itself; the following lines move up. */
    private static void hammer(ServerPlayer player, ServerLevel level, BlockPos pos, int sheet, int z) {
        if (isProtected(player, level, pos)) {
            return;
        }
        int[] area = SheetMarks.area(level, sheet, z).orElse(new int[] {z, z});
        boolean hasCode = false;
        for (int line = area[0]; line <= area[1] && !hasCode; line++) {
            for (int x = SheetGeometry.CODE_MIN_X; x <= SheetGeometry.CODE_MAX_X; x++) {
                if (level.getBlockState(LevelSheet.codePos(sheet, x, line)).getBlock() instanceof CodeBlock) {
                    hasCode = true;
                    break;
                }
            }
        }
        if (!hasCode) {
            return;
        }
        int end = SheetEditor.endOfFollowingLines(level, sheet, area[1]);
        List<ItemStack> removed = SheetEditor.apply(level, sheet, LineMoves.delete(area[0], area[1], end));
        if (!player.isCreative()) {
            removed.forEach(stack -> {
                if (!player.getInventory().add(stack)) {
                    player.drop(stack, false);
                }
            });
        }
        SELECTIONS.values().removeIf(selection -> selection.sheet() == sheet);
        message(player, "message.c0de.deleted", area[0], area[1]);
    }

    /**
     * Moves the area selected with the wrench in front of the line that was right-clicked.
     *
     * @return true if the click was used
     */
    public static boolean wrenchTarget(ServerPlayer player, BlockPos pos) {
        ServerLevel level = player.serverLevel();
        if (!LevelSheet.isOnSheet(level, pos) || !SheetGeometry.isInCodeArea(pos.getX(), pos.getZ())) {
            return false;
        }
        Selection selection = SELECTIONS.get(player.getUUID());
        int sheet = LevelSheet.sheetOf(pos);
        if (selection == null || selection.sheet() != sheet) {
            message(player, "message.c0de.no_selection");
            return true;
        }
        if (isProtected(player, level, pos)) {
            return true;
        }
        Optional<Map<Integer, Integer>> moves = LineMoves.move(selection.from(), selection.to(), pos.getZ());
        if (moves.isEmpty()) {
            message(player, "message.c0de.move_inside");
            return true;
        }
        SheetEditor.apply(level, sheet, moves.get());
        SELECTIONS.remove(player.getUUID());
        message(player, "message.c0de.moved", selection.from(), selection.to(), pos.getZ());
        return true;
    }
}
