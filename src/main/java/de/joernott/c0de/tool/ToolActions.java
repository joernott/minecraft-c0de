package de.joernott.c0de.tool;

import de.joernott.c0de.block.CodeBlock;
import de.joernott.c0de.block.TriggerBlock;
import de.joernott.c0de.blockentity.TriggerBlockEntity;
import de.joernott.c0de.compat.Compat;
import de.joernott.c0de.content.C0deItems;
import de.joernott.c0de.content.CodeBlockType;
import de.joernott.c0de.core.edit.FunctionJumps;
import de.joernott.c0de.core.edit.Indentation;
import de.joernott.c0de.core.edit.LineMoves;
import de.joernott.c0de.core.edit.SheetView;
import de.joernott.c0de.core.line.Notation;
import de.joernott.c0de.core.sheet.SheetGeometry;
import de.joernott.c0de.core.validation.Validation;
import de.joernott.c0de.core.validation.Validator;
import de.joernott.c0de.dialog.SheetProtection;
import de.joernott.c0de.dimension.C0deDimension;
import de.joernott.c0de.entity.CursorEntity;
import de.joernott.c0de.io.DisplayBlock;
import de.joernott.c0de.io.LaptopBlock;
import de.joernott.c0de.item.DebugToolItem;
import de.joernott.c0de.item.WrenchItem;
import de.joernott.c0de.library.ModLibraries;
import de.joernott.c0de.network.ToolPayload;
import de.joernott.c0de.platform.Platform;
import de.joernott.c0de.process.Activator;
import de.joernott.c0de.process.ProcessManager;
import de.joernott.c0de.sheet.LevelSheet;
import de.joernott.c0de.sheet.SheetEditor;
import de.joernott.c0de.sheet.SheetMarks;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * The left and right clicks of the c0de tools (docs/concept/14-items-and-tools.md):
 *
 * <ul>
 *   <li>debugging tool: starts a debug process at a debug trigger block, controls debug cursors
 *   <li>text marker: marks a line or removes its mark; a right click does the same with all lines from the
 *       line of the last left click to the clicked line
 *   <li>wrench: moves the marked lines in front of the clicked line (a right click copies them, see
 *       {@link #wrench})
 *   <li>hammer: deletes a marked area or a single line, the following lines move up; a right click inserts an
 *       empty line, the clicked line and the following lines move south
 *   <li>pliers: move a marked area or a single line one block to the left (a right click: to the right)
 *   <li>boots: a left click on a FUNCTION block teleports the player to the other FUNCTION block of the function, a
 *       right click to the PROGRAM block
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

    /**
     * The line of the last left click of every player with the text marker, and whether it was marked by it. Only
     * used on the server thread.
     */
    private static final Map<UUID, LastMark> LAST_MARKS = new HashMap<>();

    private record LastMark(int sheet, int z, boolean marked) {}

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
                || item == C0deItems.PLIERS.get()
                || item instanceof WrenchItem;
    }

    /** True for boots of any kind, which teleport between the FUNCTION blocks of a function. */
    public static boolean isBoots(ItemStack stack) {
        return stack.is(ItemTags.FOOT_ARMOR);
    }

    /** True for a FUNCTION block on the code layer of a sheet. */
    public static boolean isFunctionBlock(Level level, BlockPos pos) {
        return isOnLine(level, pos)
                && pos.getY() == SheetGeometry.CODE_Y
                && level.getBlockState(pos).getBlock() instanceof CodeBlock block
                && block.type() == CodeBlockType.FUNCTION;
    }

    private static boolean attackBlock(Player player, Level level, BlockPos pos, net.minecraft.core.Direction face) {
        ItemStack stack = player.getMainHandItem();
        if (!isTool(stack.getItem()) && !(isBoots(stack) && isFunctionBlock(level, pos))) {
            return false;
        }
        if (level.isClientSide()) {
            send(level, new ToolPayload(pos, -1, face == null ? -1 : face.get3DDataValue()));
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

    /**
     * Shows the line of a cursor in the chat of the player, in the notation of the line. A wrong line is red, a line
     * of an unfinished statement gold; then the problem follows.
     */
    private static void showLine(ServerPlayer player, int sheet, int z) {
        ServerLevel c0de = Compat.server(player).getLevel(C0deDimension.LEVEL);
        if (c0de == null || !LevelSheet.isLoaded(c0de, sheet, z)) {
            return;
        }
        String text = Notation.format(LevelSheet.readLine(c0de, sheet, z)).strip();
        Optional<Validation> validation =
                LevelSheet.read(c0de, sheet).map(lines -> Validator.validate(lines, ModLibraries.registry()));
        Validation.LineState state = validation.map(v -> v.state(z)).orElse(Validation.LineState.VALID);
        ChatFormatting color = switch (state) {
            case VALID -> ChatFormatting.WHITE;
            case INCOMPLETE -> ChatFormatting.GOLD;
            case INVALID -> ChatFormatting.RED;
        };
        MutableComponent message = Component.translatable("message.c0de.cursor_line", z)
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(text.isEmpty() ? "(empty)" : text).withStyle(color));
        validation.flatMap(v -> v.problem(z)).ifPresent(problem ->
                message.append(Component.literal("  " + problem.message()).withStyle(ChatFormatting.RED)));
        player.sendSystemMessage(message);
    }

    private static void message(ServerPlayer player, String key, Object... arguments) {
        Compat.message(player, Component.translatable(key, arguments), true);
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
        perform(player, Compat.level(player), payload, true);
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
                ProcessManager processes = ProcessManager.get(Compat.server(player));
                message(
                        player,
                        "message.c0de.debug",
                        processes.debug(Compat.server(player), cursor.process(), player.isShiftKeyDown()));
                processes.position(cursor.process()).ifPresent(position -> showLine(player, position[0], position[1]));
            }
            return;
        }
        BlockPos pos = payload.pos();
        if (!level.isLoaded(pos) || (checkReach && !Compat.canReach(player, pos, REACH_TOLERANCE))) {
            return;
        }
        if (item instanceof DebugToolItem) {
            if (level.getBlockState(pos).getBlock() instanceof TriggerBlock block
                    && block.kind() == TriggerBlock.Kind.DEBUG
                    && level.getBlockEntity(pos) instanceof TriggerBlockEntity trigger) {
                trigger.startDebug(Activator.player(player))
                        .ifPresentOrElse(
                                problem -> message(player, "message.c0de.not_started", problem),
                                () -> message(player, "message.c0de.debug_started", trigger.pid()));
            }
            return;
        }
        if (item instanceof WrenchItem
                && payload.face() >= 0
                && level.getBlockState(pos).getBlock() instanceof DisplayBlock) {
            // The wrench turns display blocks around the axis through the clicked face
            DisplayBlock.turn(level, pos, net.minecraft.core.Direction.from3DDataValue(payload.face()));
            return;
        }
        if (item instanceof WrenchItem && level.getBlockState(pos).getBlock() instanceof LaptopBlock) {
            // The wrench turns laptops clockwise, seen from above
            LaptopBlock.turn(level, pos);
            return;
        }
        if (isBoots(player.getMainHandItem())) {
            if (isFunctionBlock(level, pos)) {
                jump(player, level, pos);
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
            LAST_MARKS.put(player.getUUID(), new LastMark(sheet, z, SheetMarks.isMarked(level, sheet, z)));
        } else if (item instanceof WrenchItem) {
            wrench(player, level, pos, false);
        } else if (item == C0deItems.HAMMER.get()) {
            hammer(player, level, pos, sheet, z);
        } else if (item == C0deItems.PLIERS.get()) {
            indent(player, level, pos, sheet, z, -1);
        }
    }

    /**
     * Executes a right click of the text marker, the hammer or the pliers on a line.
     *
     * @return false if the item has no right click
     */
    public static boolean rightClick(ServerPlayer player, ServerLevel level, BlockPos pos) {
        Item item = player.getMainHandItem().getItem();
        if (item != C0deItems.MARKER.get() && item != C0deItems.HAMMER.get() && item != C0deItems.PLIERS.get()) {
            return false;
        }
        long now = level.getGameTime();
        if (!cooldownOver(now, LAST_ACTIONS.get(player.getUUID()))) {
            // Holding the button uses the item again every few ticks
            return true;
        }
        LAST_ACTIONS.put(player.getUUID(), now);
        int sheet = LevelSheet.sheetOf(pos);
        int z = pos.getZ();
        if (item == C0deItems.MARKER.get()) {
            markRange(player, level, sheet, z);
        } else if (item == C0deItems.HAMMER.get()) {
            insertLine(player, level, pos, sheet, z);
        } else {
            indent(player, level, pos, sheet, z, 1);
        }
        return true;
    }

    /**
     * A right click with boots on a FUNCTION block teleports the player to the PROGRAM block of the sheet. Called by
     * the code blocks on the client and the server.
     *
     * @return false if the item is no boots or the block no FUNCTION block
     */
    public static boolean useBoots(Player player, Level level, BlockPos pos, ItemStack stack) {
        if (!isBoots(stack) || !isFunctionBlock(level, pos)) {
            return false;
        }
        if (player instanceof ServerPlayer serverPlayer && level instanceof ServerLevel serverLevel) {
            teleportToLine(serverPlayer, serverLevel, LevelSheet.sheetOf(pos), 0);
        }
        return true;
    }

    /** Teleports the player from a FUNCTION block to the other FUNCTION block of its function. */
    private static void jump(ServerPlayer player, ServerLevel level, BlockPos pos) {
        OptionalInt target = jumpTarget(level, pos);
        if (target.isEmpty()) {
            message(player, "message.c0de.no_counterpart");
            return;
        }
        teleportToLine(player, level, LevelSheet.sheetOf(pos), target.getAsInt());
    }

    /**
     * The line that boots take a player to from a FUNCTION block: the END FUNCTION line of a FUNCTION line or the
     * other way round.
     *
     * @return the line, or empty if the block is no such FUNCTION block or its function is not complete
     */
    public static OptionalInt jumpTarget(ServerLevel level, BlockPos pos) {
        return FunctionJumps.counterpart(
                LevelSheet.loadingView(level, LevelSheet.sheetOf(pos)), SheetGeometry.relativeX(pos.getX()), pos.getZ());
    }

    /**
     * Teleports the player next to the start of a line: into the margin west of the cursor rail, looking east at
     * the line.
     */
    private static void teleportToLine(ServerPlayer player, ServerLevel level, int sheet, int z) {
        Compat.teleport(
                player,
                level,
                SheetGeometry.absoluteX(sheet, SheetGeometry.CURSOR_X - 1) + 0.5,
                SheetGeometry.CODE_Y,
                z + 0.5,
                -90.0F, // facing east
                35.0F);
        level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.5F, 1.2F);
    }

    /**
     * Moves the marked area that contains the line, or the line itself, by one block: to the left (west) for
     * {@code dx} = -1, to the right (east) for 1. Nothing moves if a block would leave the code area.
     */
    private static void indent(ServerPlayer player, ServerLevel level, BlockPos pos, int sheet, int z, int dx) {
        if (isProtected(player, level, pos)) {
            return;
        }
        int[] area = SheetMarks.area(level, sheet, z).orElse(new int[] {z, z});
        SheetView view = LevelSheet.loadingView(level, sheet);
        if (Indentation.isEmpty(view, area[0], area[1])) {
            return;
        }
        String direction = dx < 0 ? "left" : "right";
        OptionalInt blocked = Indentation.blocked(view, area[0], area[1], dx);
        if (blocked.isPresent()) {
            message(player, "message.c0de.not_indented_" + direction, blocked.getAsInt());
            return;
        }
        SheetEditor.shift(level, sheet, area[0], area[1], dx);
        level.playSound(null, pos, SoundEvents.PISTON_CONTRACT, SoundSource.BLOCKS, 0.5F, 1.5F);
        message(player, "message.c0de.indented_" + direction, area[0], area[1]);
    }

    /**
     * Marks the lines from the line of the last left click with the marker to the clicked line, or removes their
     * marks if the left click removed a mark.
     */
    private static void markRange(ServerPlayer player, ServerLevel level, int sheet, int z) {
        LastMark last = LAST_MARKS.get(player.getUUID());
        if (last == null || last.sheet() != sheet) {
            message(player, "message.c0de.no_last_mark");
            return;
        }
        for (int line = Math.min(last.z(), z); line <= Math.max(last.z(), z); line++) {
            SheetMarks.set(level, sheet, line, last.marked());
        }
        // A following right click continues from here
        LAST_MARKS.put(player.getUUID(), new LastMark(sheet, z, last.marked()));
    }

    /** Inserts an empty line in front of the clicked line: the line and the lines after it move south. */
    private static void insertLine(ServerPlayer player, ServerLevel level, BlockPos pos, int sheet, int z) {
        if (isProtected(player, level, pos)) {
            return;
        }
        // Up to a gap of two empty lines, so the moved lines don't join the lines after the gap
        int end = SheetEditor.endOfContent(level, sheet, z, 2);
        if (end <= z) {
            return;
        }
        SheetEditor.apply(level, sheet, LineMoves.insert(z, 1, end));
        level.playSound(null, pos, SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 0.5F, 1.0F);
        message(player, "message.c0de.inserted", z);
    }

    /** True for the code layer and the floor of the code area of a sheet: clicking either selects the line. */
    public static boolean isOnLine(Level level, BlockPos pos) {
        return LevelSheet.isOnSheet(level, pos)
                && (pos.getY() == SheetGeometry.CODE_Y || pos.getY() == SheetGeometry.FLOOR_Y)
                && SheetGeometry.isInCodeArea(pos.getX(), pos.getZ());
    }

    private static boolean isProtected(ServerPlayer player, ServerLevel level, BlockPos pos) {
        Optional<Component> reason = SheetProtection.reason(player, level, pos, true);
        reason.ifPresent(message -> Compat.message(player, message, true));
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
            // Empty lines are removed too: the content after them moves up, up to a gap of two empty lines
            int end = SheetEditor.endOfContent(level, sheet, area[1] + 1, 2);
            if (end > area[1] + 1) {
                SheetEditor.apply(level, sheet, LineMoves.delete(area[0], area[1], end));
                level.playSound(null, pos, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 0.8F, 0.8F);
                message(player, "message.c0de.deleted", area[0], area[1]);
            }
            return;
        }
        int end = SheetEditor.endOfFollowingLines(level, sheet, area[1]);
        List<ItemStack> removed = SheetEditor.apply(level, sheet, LineMoves.delete(area[0], area[1], end));
        if (!player.isCreative()) {
            removed.forEach(stack -> Compat.give(player, stack));
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
                        .append(Compat.name(item).getString()));
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
