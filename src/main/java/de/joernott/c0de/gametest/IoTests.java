package de.joernott.c0de.gametest;

import de.joernott.c0de.blockentity.TriggerBlockEntity;
import de.joernott.c0de.content.C0deBlocks;
import de.joernott.c0de.dimension.C0deDimension;
import de.joernott.c0de.io.DisplayBlock;
import de.joernott.c0de.io.DisplayBlockEntity;
import de.joernott.c0de.sheet.SheetValidation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;

/** GameTests of the devices of input and output (tasks 11.6 to 11.9). */
public final class IoTests {
    private IoTests() {}

    /**
     * Two display blocks next to each other form a wall with one screen, held by the block at the top left. A
     * process of the bound program prints on it.
     */
    public static void displaysShowWhatProgramsPrint(GameTestHelper helper) {
        ServerLevel c0de = helper.getLevel().getServer().getLevel(C0deDimension.LEVEL);
        SheetBuilder sheet = new SheetBuilder(c0de, 524).place("""
                PROGRAM printing
                NOOP
                FUNCTION hello
                BEGIN
                    PRINT "Hello" ;
                    CALL LOCATE
                        PARAMETER row 3 ;
                    PRINT "World" ;
                END FUNCTION
                """);
        SheetValidation.validate(c0de, 524);
        // Facing south, the text runs to the east: the block in the west is the top left one
        BlockPos left = new BlockPos(1, 2, 1);
        BlockPos right = new BlockPos(2, 2, 1);
        helper.setBlock(left, C0deBlocks.DISPLAY_BLOCK.get().defaultBlockState().setValue(DisplayBlock.FACING, Direction.SOUTH));
        helper.setBlock(right, C0deBlocks.DISPLAY_BLOCK.get().defaultBlockState().setValue(DisplayBlock.FACING, Direction.SOUTH));
        DisplayBlockEntity origin = (DisplayBlockEntity) helper.getBlockEntity(left);
        helper.assertTrue(origin.isOrigin(), "The west block is not the origin of the wall");
        helper.assertTrue(
                origin.screen().width() == 2 * DisplayBlockEntity.COLUMNS && origin.screen().height() == DisplayBlockEntity.ROWS,
                "The screen is " + origin.screen().width() + " x " + origin.screen().height());
        helper.assertTrue(!((DisplayBlockEntity) helper.getBlockEntity(right)).isOrigin(), "Both blocks are origins");
        ((DisplayBlockEntity) helper.getBlockEntity(right)).bind(helper.getLevel(), "printing", "hello");
        helper.assertTrue(origin.program().equals("printing"), "Binding one block didn't bind the wall");

        helper.setBlock(BlockPos.ZERO, C0deBlocks.TRIGGER_BLOCK.get());
        TriggerBlockEntity trigger = (TriggerBlockEntity) helper.getBlockEntity(BlockPos.ZERO);
        trigger.configure(helper.makeMockServerPlayerInLevel(), "printing", "hello", 20);
        trigger.start();
        helper.assertTrue(trigger.isRunning(), "The process didn't start: " + trigger.status());
        helper.runAfterDelay(30, () -> {
            try {
                helper.assertTrue(!trigger.isRunning(), "The process is still running");
                String first = origin.screen().rows().get(0).text();
                String fourth = origin.screen().rows().get(3).text();
                helper.assertTrue(
                        first.equals("Hello") && fourth.equals("World"), "The screen shows " + first + " / " + fourth);
                // A third block joins the wall and takes its binding
                BlockPos third = new BlockPos(3, 2, 1);
                helper.setBlock(
                        third, C0deBlocks.DISPLAY_BLOCK.get().defaultBlockState().setValue(DisplayBlock.FACING, Direction.SOUTH));
                DisplayBlockEntity joined = (DisplayBlockEntity) helper.getBlockEntity(third);
                helper.assertTrue(joined.program().equals("printing"), "The new block didn't take the binding");
                helper.assertTrue(
                        origin.screen().width() == 3 * DisplayBlockEntity.COLUMNS, "The wall didn't grow");
                helper.assertTrue(
                        origin.screen().rows().get(0).text().equals("Hello"), "The screen lost its content");
            } finally {
                sheet.clear();
                SheetValidation.validate(c0de, 524);
            }
            helper.succeed();
        });
    }
}
