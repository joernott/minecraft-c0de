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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
 *   <li>wrench: moves the marked lines in front of the clicked line (a right click copies them, see
 *       {@link #wrench})
 *   <li>hammer: deletes a marked area or a single line; the following lines move up
 * </ul>
 */
public final class ToolActions {
    /** Extra distance to the interaction range of a player, as for dialogs. */
    private static final double REACH_TOLERANCE = 2.0;

    /**
     * Ticks between two actions of a tool. While the attack button is held, the client tries to start breaking
     * the block again every tick, which would repeat the action (e.g. toggle a mark several times).
     */
    private static final int COOLDOWN_TICKS = 6;

    /** The game time of the last action of every player on the server. Only used on the server thread. */
    private static final Map<UUID, Long> LAST_ACTIONS = new HashMap<>();

    /** The game time of the last action sent by this client, or null before the first one. */
    private static Long lastSent;


    private ToolActions() {}

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
            send(level, new ToolPayload(pos, -1));
        }
        return true;
    }

    /** Sends a tool action from the client, unless the last one was sent within the cooldown. */
    private static void send(Level level, ToolPayload payload) {
        long now = level.getGameTime();
        if (cooldownOver(now, lastSent)) {
            lastSent = now;
            Platform.INSTANCE.sendToServer(payload);
        }
    }

    private static boolean attackEntity(Player player, Level level, Entity entity) {
        if (!(player.getMainHandItem().getItem() instanceof DebugToolItem) || !(entity instanceof CursorEntity)) {
            return false;
        }
        if (level.isClientSide()) {
            send(level, new ToolPayload(entity.blockPosition(), entity.getId()));
        }
        return true;
    }

    private static void message(ServerPlayer player, String key, Object... arguments) {
        player.displayClientMessage(Component.translatable(key, arguments), true);
    }

    /**
     * True if a tool may act again: there was no action yet, the last one is at least {@link #COOLDOWN_TICKS}
     * ago, or the game time went back (another world).
     */
    public static boolean cooldownOver(long now, Long last) {
        return last == null || now < last || now - last >= COOLDOWN_TICKS;
    }

    /** Forgets the last action of a player, for tests that click faster than the cooldown. */
    public static void resetCooldown(ServerPlayer player) {
        LAST_ACTIONS.remove(player.getUUID());
    }

    /** Executes a left click on the server. */
    public static void handle(ToolPayload payload, ServerPlayer player) {
        perform(player, player.serverLevel(), payload, true);
    }

    /**
     * Executes a left click in a level.
     *
     * @param checkReach false for tests, whose players can't stand on the sheets without loading many chunks
     */
    public static void perform(ServerPlayer player, ServerLevel level, ToolPayload payload, boolean checkReach) {
        long now = level.getGameTime();
        if (!cooldownOver(now, LAST_ACTIONS.get(player.getUUID()))) {
            return;
        }
        LAST_ACTIONS.put(player.getUUID(), now);
        Item item = player.getMainHandItem().getItem();
        if (payload.entity() >= 0) {
            Entity entity = level.getEntity(payload.entity());
            if (item instanceof DebugToolItem
                    && entity instanceof CursorEntity cursor
                    && (!checkReach || player.distanceTo(cursor) <= player.entityInteractionRange() + REACH_TOLERANCE)) {
                message(
                        player,
                        "message.c0de.debug",
                        ProcessManager.get(player.server).debug(player.server, cursor.process(), player.isShiftKeyDown()));
            }
            return;
        }
        BlockPos pos = payload.pos();
        if (!level.isLoaded(pos) || (checkReach && !player.canInteractWithBlock(pos, REACH_TOLERANCE))) {
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
        if (!isOnLine(level, pos)) {
            return;
        }
        int sheet = LevelSheet.sheetOf(pos);
        int z = pos.getZ();
        if (item == C0deItems.MARKER.get()) {
            // Marking doesn't change the program, so it is allowed while processes run
            SheetMarks.toggle(level, sheet, z);
        } else if (item instanceof WrenchItem) {
            wrench(player, level, pos, false);
        } else if (item == C0deItems.HAMMER.get()) {
            hammer(player, level, pos, sheet, z);
        }
    }

    /** True for the code layer and the floor of the code area of a sheet: clicking either selects the line. */
    public static boolean isOnLine(Level level, BlockPos pos) {
        return LevelSheet.isOnSheet(level, pos)
                && (pos.getY() == SheetGeometry.CODE_Y || pos.getY() == SheetGeometry.FLOOR_Y)
                && SheetGeometry.isInCodeArea(pos.getX(), pos.getZ());
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
        level.playSound(null, pos, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 0.8F, 0.8F);
        message(player, "message.c0de.deleted", area[0], area[1]);
    }

    /**
     * Moves (left click) or copies (right click) the marked lines of the sheet in front of the clicked line. The
     * clicked line and the lines after it move south.
     */
    public static void wrench(ServerPlayer player, ServerLevel level, BlockPos pos, boolean copy) {
        int sheet = LevelSheet.sheetOf(pos);
        int target = pos.getZ();
        java.util.SortedSet<Integer> marked = SheetMarks.marked(level, sheet);
        if (marked.isEmpty()) {
            message(player, "message.c0de.no_marks");
            return;
        }
        if (marked.contains(target)) {
            message(player, "message.c0de.target_marked");
            return;
        }
        if (isProtected(player, level, pos)) {
            return;
        }
        if (copy) {
            Map<Item, Integer> missing = SheetEditor.copy(level, sheet, marked, target, player);
            if (!missing.isEmpty()) {
                StringBuilder text = new StringBuilder();
                missing.forEach((item, count) -> text.append(text.isEmpty() ? "" : ", ")
                        .append(count)
                        .append(" ")
                        .append(item.getDescription().getString()));
                message(player, "message.c0de.missing_blocks", text.toString());
                return;
            }
            level.playSound(null, pos, SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 0.5F, 1.2F);
            message(player, "message.c0de.copied", marked.size(), target);
        } else {
            SheetEditor.apply(level, sheet, LineMoves.move(marked, target).orElseThrow());
            level.playSound(null, pos, SoundEvents.PISTON_CONTRACT, SoundSource.BLOCKS, 0.5F, 1.2F);
            message(player, "message.c0de.moved", marked.size(), target);
        }
    }
}
