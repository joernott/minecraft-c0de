package de.joernott.c0de.gametest;

import de.joernott.c0de.blockentity.LabelBlockEntity;
import de.joernott.c0de.blockentity.TriggerBlockEntity;
import de.joernott.c0de.content.C0deBlocks;
import de.joernott.c0de.content.C0deItems;
import de.joernott.c0de.content.CodeBlockType;
import de.joernott.c0de.core.edit.LineMoves;
import de.joernott.c0de.dimension.C0deDimension;
import de.joernott.c0de.item.ValueItem;
import de.joernott.c0de.process.ProcessManager;
import de.joernott.c0de.sheet.SheetEditor;
import de.joernott.c0de.sheet.SheetMarks;
import de.joernott.c0de.sheet.SheetValidation;
import de.joernott.c0de.tool.Inspector;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

/** GameTests of the debugging tool, the text marker, the wrench and the hammer (sprint 9). */
public final class ToolTests {
    private ToolTests() {}

    private static ServerLevel c0de(GameTestHelper helper) {
        ServerLevel level = helper.getLevel().getServer().getLevel(C0deDimension.LEVEL);
        helper.assertTrue(level != null, "The c0de dimension doesn't exist");
        return level;
    }

    private static int line(ServerLevel level, SheetBuilder sheet, int z) {
        return level.getBlockEntity(sheet.pos(6, z)) instanceof LabelBlockEntity label
                ? Integer.parseInt(label.name().substring(1))
                : -1;
    }

    /** A debug process starts paused, moves one line per click and runs when it is switched on. */
    public static void debugCursorsArePausedAndStepped(GameTestHelper helper) {
        ServerLevel level = c0de(helper);
        SheetBuilder sheet = new SheetBuilder(level, 520)
                .place(
                        """
                        PROGRAM debugging
                        NOOP
                        FUNCTION run
                            RESULT total INTEGER
                        BEGIN
                            ASSIGN total 1 ;
                            ASSIGN total total + 1 ;
                        END FUNCTION
                        """);
        SheetValidation.validate(level, 520);
        helper.setBlock(BlockPos.ZERO, C0deBlocks.DEBUG_TRIGGER_BLOCK.get());
        TriggerBlockEntity trigger = (TriggerBlockEntity) helper.getBlockEntity(BlockPos.ZERO);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        trigger.configure(player, "debugging", "run", 20);
        ItemStack empty = new ItemStack(C0deItems.VARIABLE_ITEM.get());
        new ValueItem.Contents("", "", "").writeTo(empty);
        trigger.input().setItem(0, empty);
        helper.assertTrue(trigger.startDebug().isEmpty(), "The debug process didn't start: " + trigger.status());
        ProcessManager processes = ProcessManager.get(level.getServer());
        helper.runAfterDelay(5, () -> {
            helper.assertTrue(trigger.isRunning(), "The paused process ended");
            String first = processes.debug(level.getServer(), trigger.processId(), false);
            String second = processes.debug(level.getServer(), trigger.processId(), false);
            helper.assertTrue(
                    first.startsWith("PAUSED") && second.startsWith("PAUSED") && !first.equals(second),
                    "The cursor didn't step: " + first + ", " + second);
            processes.debug(level.getServer(), trigger.processId(), true);
            helper.runAfterDelay(20, () -> {
                try {
                    helper.assertTrue(!trigger.isRunning(), "The running debug process didn't end");
                    ValueItem.Contents result = ValueItem.Contents.of(trigger.output().getItem(0));
                    helper.assertTrue(result.value().equals("2"), "The result is " + result);
                } finally {
                    sheet.clear();
                    SheetValidation.validate(level, 520);
                }
                helper.succeed();
            });
        });
    }

    /** Marked lines are moved with their blocks and properties; deleted lines make the following lines move up. */
    public static void linesAreMarkedMovedAndDeleted(GameTestHelper helper) {
        ServerLevel level = c0de(helper);
        // Every line holds a NOOP block and a label with its original line number
        SheetBuilder sheet = new SheetBuilder(level, 521);
        for (int z = 0; z < 6; z++) {
            sheet.place(new de.joernott.c0de.core.line.Block.KeywordBlock(de.joernott.c0de.core.line.Keyword.ASSIGN), 5, z);
            sheet.place(new de.joernott.c0de.core.line.Block.LabelBlock("l" + z), 6, z);
        }
        try {
            SheetMarks.toggle(level, 521, 3);
            SheetMarks.toggle(level, 521, 4);
            int[] area = SheetMarks.area(level, 521, 4).orElseThrow();
            helper.assertTrue(area[0] == 3 && area[1] == 4, "The marked area is " + area[0] + " to " + area[1]);

            // Lines 3 and 4 in front of line 1
            SheetEditor.apply(level, 521, LineMoves.move(SheetMarks.marked(level, 521), 1).orElseThrow());
            List<Integer> order = List.of(
                    line(level, sheet, 0),
                    line(level, sheet, 1),
                    line(level, sheet, 2),
                    line(level, sheet, 3),
                    line(level, sheet, 4),
                    line(level, sheet, 5));
            helper.assertTrue(order.equals(List.of(0, 3, 4, 1, 2, 5)), "The lines are " + order);
            helper.assertTrue(
                    SheetMarks.isMarked(level, 521, 1) && SheetMarks.isMarked(level, 521, 2)
                            && !SheetMarks.isMarked(level, 521, 4),
                    "The marks didn't move with their lines");

            // Deleting lines 1 and 2 moves the following lines up
            List<ItemStack> removed = SheetEditor.apply(
                    level, 521, LineMoves.delete(1, 2, SheetEditor.endOfFollowingLines(level, 521, 2)));
            helper.assertTrue(removed.size() == 4, "Removed items: " + removed);
            order = List.of(line(level, sheet, 0), line(level, sheet, 1), line(level, sheet, 2), line(level, sheet, 3));
            helper.assertTrue(order.equals(List.of(0, 1, 2, 5)), "After deleting, the lines are " + order);
            helper.assertTrue(level.getBlockState(sheet.pos(6, 4)).isAir(), "The last line was not moved up");
            helper.assertTrue(!SheetMarks.isMarked(level, 521, 1), "The marks of deleted lines remain");
        } finally {
            for (int z = 0; z < 6; z++) {
                level.setBlockAndUpdate(sheet.pos(5, z), Blocks.AIR.defaultBlockState());
                level.setBlockAndUpdate(sheet.pos(6, z), Blocks.AIR.defaultBlockState());
                if (SheetMarks.isMarked(level, 521, z)) {
                    SheetMarks.toggle(level, 521, z);
                }
            }
        }
        helper.succeed();
    }

    /** The debugging tool inspects c0de blocks, not other blocks. */
    public static void onlyC0deBlocksAreInspected(GameTestHelper helper) {
        helper.setBlock(BlockPos.ZERO, C0deBlocks.get(CodeBlockType.LABEL));
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        helper.assertTrue(Inspector.isInspectable(helper.getLevel(), helper.absolutePos(BlockPos.ZERO)), "LABEL");
        helper.setBlock(BlockPos.ZERO, Blocks.DIRT);
        helper.assertTrue(
                !Inspector.inspectBlock(player, helper.absolutePos(BlockPos.ZERO)), "Dirt can be inspected");
        helper.succeed();
    }

    /** The tools as a player uses them: mark, move with the wrench, unmark and mark again, copy. */
    public static void toolsWorkAfterMovingLines(GameTestHelper helper) {
        ServerLevel level = c0de(helper);
        SheetBuilder sheet = new SheetBuilder(level, 522);
        for (int z = 0; z < 6; z++) {
            sheet.place(new de.joernott.c0de.core.line.Block.KeywordBlock(de.joernott.c0de.core.line.Keyword.ASSIGN), 5, z);
            sheet.place(new de.joernott.c0de.core.line.Block.LabelBlock("l" + z), 6, z);
        }
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        BlockPos stand = sheet.pos(6, 3).above();
        player.teleportTo(level, stand.getX() + 0.5, stand.getY(), stand.getZ() + 0.5, 0, 0);
        try {
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(C0deItems.MARKER.get()));
            // A block of the line and the floor of the line both mark it
            click(helper, player, sheet.pos(6, 3));
            click(helper, player, sheet.pos(8, 4).below());
            helper.assertTrue(SheetMarks.isMarked(level, 522, 3) && SheetMarks.isMarked(level, 522, 4), "Not marked");
            // A left click with the wrench moves the marked lines in front of the clicked line
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(C0deItems.WRENCH.get()));
            click(helper, player, sheet.pos(8, 1).below());
            helper.assertTrue(line(level, sheet, 1) == 3 && line(level, sheet, 3) == 1, "Not moved");
            helper.assertTrue(SheetMarks.isMarked(level, 522, 1), "The mark didn't move");
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(C0deItems.MARKER.get()));
            click(helper, player, sheet.pos(6, 1));
            helper.assertTrue(!SheetMarks.isMarked(level, 522, 1), "The moved mark can't be removed");
            click(helper, player, sheet.pos(6, 5));
            helper.assertTrue(SheetMarks.isMarked(level, 522, 5), "A new line can't be marked");

            // A right click copies the marked lines (now 2 and 5) in front of line 0. The mock player of GameTests
            // is always in creative mode, so the blocks are not taken from its inventory.
            de.joernott.c0de.tool.ToolActions.wrench(player, sheet.pos(6, 0), true);
            List<Integer> order = new java.util.ArrayList<>();
            for (int z = 0; z < 8; z++) {
                order.add(line(level, sheet, z));
            }
            helper.assertTrue(order.equals(List.of(4, 5, 0, 3, 4, 1, 2, 5)), "After copying, the lines are " + order);
        } finally {
            for (int z = 0; z < 8; z++) {
                level.setBlockAndUpdate(sheet.pos(5, z), Blocks.AIR.defaultBlockState());
                level.setBlockAndUpdate(sheet.pos(6, z), Blocks.AIR.defaultBlockState());
                if (SheetMarks.isMarked(level, 522, z)) {
                    SheetMarks.toggle(level, 522, z);
                }
            }
        }
        helper.succeed();
    }

    /** The cooldown of the tools lets the first click through and blocks repeats within 6 ticks. */
    public static void toolCooldown(GameTestHelper helper) {
        helper.assertTrue(de.joernott.c0de.tool.ToolActions.cooldownOver(0, null), "The first click is blocked");
        helper.assertTrue(
                de.joernott.c0de.tool.ToolActions.cooldownOver(123456789L, null), "The first click is blocked");
        helper.assertTrue(!de.joernott.c0de.tool.ToolActions.cooldownOver(105, 100L), "A repeat is not blocked");
        helper.assertTrue(de.joernott.c0de.tool.ToolActions.cooldownOver(106, 100L), "A later click is blocked");
        helper.assertTrue(de.joernott.c0de.tool.ToolActions.cooldownOver(5, 100L), "A click in another world is blocked");
        helper.succeed();
    }

    /** A left click of a tool, as the client sends it, with the cooldown of the tools in between. */
    private static void click(GameTestHelper helper, ServerPlayer player, BlockPos pos) {
        de.joernott.c0de.tool.ToolActions.resetCooldown(player);
        de.joernott.c0de.tool.ToolActions.handle(new de.joernott.c0de.network.ToolPayload(pos, -1), player);
    }
}
