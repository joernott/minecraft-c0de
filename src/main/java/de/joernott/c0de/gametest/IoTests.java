package de.joernott.c0de.gametest;

import de.joernott.c0de.blockentity.TriggerBlockEntity;
import de.joernott.c0de.content.C0deBlocks;
import de.joernott.c0de.content.C0deItems;
import de.joernott.c0de.dimension.C0deDimension;
import de.joernott.c0de.io.DisplayBlock;
import de.joernott.c0de.io.DisplayBlockEntity;
import de.joernott.c0de.io.IoBlockEntity;
import de.joernott.c0de.io.LaptopBlockEntity;
import de.joernott.c0de.io.Terminals;
import de.joernott.c0de.item.LiteralItem;
import de.joernott.c0de.item.ValueItem;
import de.joernott.c0de.sheet.SheetValidation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import java.util.List;
import net.minecraft.world.item.ItemStack;

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
                    CALL PRINT
                        PARAMETER value "Hello" ;
                    CALL LOCATE
                        PARAMETER row 3 ;
                    CALL PRINT
                        PARAMETER value "World" ;
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

    /**
     * INPUT takes the variable item with the name of the variable, then a literal item of the type; WRITE fills an
     * uninitialized variable item. The used items end up in the output of the I/O block.
     */
    public static void ioBlocksExchangeItems(GameTestHelper helper) {
        ServerLevel c0de = helper.getLevel().getServer().getLevel(C0deDimension.LEVEL);
        SheetBuilder sheet = new SheetBuilder(c0de, 526).place("""
                PROGRAM exchanging
                NOOP
                FUNCTION run
                BEGIN
                    VARIABLE count INTEGER
                    VARIABLE step INTEGER
                    CALL INPUT
                        PARAMETER variable count ;
                    CALL INPUT
                        PARAMETER variable step ;
                    CALL WRITE
                        PARAMETER value count + step ;
                END FUNCTION
                """);
        SheetValidation.validate(c0de, 526);
        BlockPos pos = new BlockPos(1, 2, 1);
        helper.setBlock(pos, C0deBlocks.IO_BLOCK.get());
        IoBlockEntity io = (IoBlockEntity) helper.getBlockEntity(pos);
        io.bind(helper.getLevel(), "exchanging", "run");
        ItemStack count = new ItemStack(C0deItems.VARIABLE_ITEM.get());
        new ValueItem.Contents("count", "INTEGER", "20").writeTo(count);
        ItemStack literal = new ItemStack(C0deItems.LITERAL_ITEM.get());
        new LiteralItem.Contents("INTEGER", "5").writeTo(literal);
        io.input().setItem(0, count);
        io.input().setItem(1, literal);

        helper.setBlock(BlockPos.ZERO, C0deBlocks.TRIGGER_BLOCK.get());
        TriggerBlockEntity trigger = (TriggerBlockEntity) helper.getBlockEntity(BlockPos.ZERO);
        trigger.configure(helper.makeMockServerPlayerInLevel(), "exchanging", "run", 20);
        trigger.start();
        helper.assertTrue(trigger.isRunning(), "The process didn't start: " + trigger.status());
        helper.runAfterDelay(30, () -> {
            try {
                helper.assertTrue(trigger.isRunning(), "WRITE didn't wait for an uninitialized variable item");
                helper.assertTrue(io.input().isEmpty(), "INPUT didn't take the items");
                io.input().setItem(0, new ItemStack(C0deItems.VARIABLE_ITEM.get()));
            } catch (RuntimeException e) {
                sheet.clear();
                SheetValidation.validate(c0de, 526);
                throw e;
            }
            helper.runAfterDelay(20, () -> {
                try {
                    helper.assertTrue(!trigger.isRunning(), "The process is still running");
                    String written = null;
                    boolean emptyLiteral = false;
                    int uninitialized = 0;
                    for (int i = 0; i < io.output().getContainerSize(); i++) {
                        ItemStack stack = io.output().getItem(i);
                        if (stack.getItem() instanceof LiteralItem) {
                            LiteralItem.Contents contents = LiteralItem.Contents.of(stack);
                            emptyLiteral = contents.isEmpty() && contents.type().equals("INTEGER");
                        } else if (stack.getItem() instanceof ValueItem) {
                            ValueItem.Contents contents = ValueItem.Contents.of(stack);
                            if (contents.isEmpty()) {
                                uninitialized += stack.getCount();
                            } else {
                                written = contents.name() + "=" + contents.value();
                            }
                        }
                    }
                    helper.assertTrue("output=25".equals(written), "WRITE wrote " + written);
                    helper.assertTrue(emptyLiteral, "The literal item didn't come out empty");
                    helper.assertTrue(uninitialized == 1, uninitialized + " uninitialized variable items came out");
                } finally {
                    sheet.clear();
                    SheetValidation.validate(c0de, 526);
                }
                helper.succeed();
            });
        });
    }

    /**
     * A laptop shows what the process prints. INPUT waits for a line typed on it; a line that doesn't fit the type
     * is refused, the next one is read.
     */
    public static void laptopsTakeTypedLines(GameTestHelper helper) {
        ServerLevel c0de = helper.getLevel().getServer().getLevel(C0deDimension.LEVEL);
        SheetBuilder sheet = new SheetBuilder(c0de, 528).place("""
                PROGRAM typing
                NOOP
                FUNCTION ask
                BEGIN
                    VARIABLE count INTEGER
                    CALL INPUT
                        PARAMETER variable count ;
                    CALL PRINT
                        PARAMETER value count * 2 ;
                END FUNCTION
                """);
        SheetValidation.validate(c0de, 528);
        BlockPos pos = new BlockPos(1, 2, 1);
        helper.setBlock(pos, C0deBlocks.LAPTOP.get());
        LaptopBlockEntity laptop = (LaptopBlockEntity) helper.getBlockEntity(pos);
        Terminals terminals = Terminals.get(helper.getLevel().getServer());
        Terminals.Terminal terminal = terminals.terminal(laptop.terminal());
        terminals.bind(helper.getLevel().getServer(), terminal, "typing", "ask");

        helper.setBlock(BlockPos.ZERO, C0deBlocks.TRIGGER_BLOCK.get());
        TriggerBlockEntity trigger = (TriggerBlockEntity) helper.getBlockEntity(BlockPos.ZERO);
        trigger.configure(helper.makeMockServerPlayerInLevel(), "typing", "ask", 20);
        trigger.start();
        helper.assertTrue(trigger.isRunning(), "The process didn't start: " + trigger.status());
        helper.runAfterDelay(10, () -> {
            try {
                helper.assertTrue(terminal.prompt().equals("count (INTEGER)"), "The prompt is " + terminal.prompt());
                terminals.line(terminal, "many");
                terminals.line(terminal, "21");
            } catch (RuntimeException e) {
                sheet.clear();
                SheetValidation.validate(c0de, 528);
                throw e;
            }
            helper.runAfterDelay(10, () -> {
                try {
                    helper.assertTrue(!trigger.isRunning(), "The process is still running");
                    List<String> rows = terminal.screen().rows().stream().map(row -> row.text()).toList();
                    helper.assertTrue(
                            rows.get(0).equals("many") && rows.get(1).startsWith("? ") && rows.get(2).equals("21")
                                    && rows.get(3).equals("42"),
                            "The terminal shows " + rows.subList(0, 4));
                    helper.assertTrue(terminal.prompt().isEmpty(), "INPUT still waits");
                } finally {
                    sheet.clear();
                    SheetValidation.validate(c0de, 528);
                    helper.getLevel().removeBlock(pos, false);
                }
                helper.succeed();
            });
        });
    }
}
