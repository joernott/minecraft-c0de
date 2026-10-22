package de.joernott.c0de.gametest;

import de.joernott.c0de.blockentity.TriggerBlockEntity;
import de.joernott.c0de.content.C0deBlocks;
import de.joernott.c0de.content.C0deItems;
import de.joernott.c0de.core.cursor.Cursor;
import de.joernott.c0de.core.cursor.CursorSettings;
import de.joernott.c0de.core.cursor.CursorSnapshot;
import de.joernott.c0de.core.line.Notation;
import de.joernott.c0de.core.program.Program;
import de.joernott.c0de.core.program.ProgramParser;
import de.joernott.c0de.core.value.IntegerValue;
import de.joernott.c0de.core.value.Value;
import de.joernott.c0de.dialog.SheetProtection;
import de.joernott.c0de.dimension.C0deDimension;
import de.joernott.c0de.item.ValueItem;
import de.joernott.c0de.library.ModLibraries;
import de.joernott.c0de.process.SnapshotNbt;
import de.joernott.c0de.sheet.LevelSheet;
import de.joernott.c0de.sheet.SheetValidation;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

/** GameTests of processes (sprint 8). Every test uses its own sheet and program name. */
public final class ProcessTests {
    private ProcessTests() {}

    private static ServerLevel c0de(GameTestHelper helper) {
        ServerLevel level = helper.getLevel().getServer().getLevel(C0deDimension.LEVEL);
        helper.assertTrue(level != null, "The c0de dimension doesn't exist");
        return level;
    }

    private static String twice(String name) {
        return """
                PROGRAM %s
                NOOP
                FUNCTION twice
                    PARAMETER value INTEGER
                    RESULT result INTEGER
                BEGIN
                    ASSIGN result value * 2 ;
                    ASSIGN result result / ( value - value ) ;
                END FUNCTION
                """
                .formatted(name);
    }

    private static ItemStack variable(String name, String type, String value) {
        ItemStack item = new ItemStack(C0deItems.VARIABLE_ITEM.get());
        new ValueItem.Contents(name, type, value).writeTo(item);
        return item;
    }

    /** Places a configured trigger block in the test structure with the items in its input inventory. */
    private static TriggerBlockEntity trigger(GameTestHelper helper, String program, ItemStack... inputs) {
        helper.setBlock(BlockPos.ZERO, C0deBlocks.TRIGGER_BLOCK.get());
        TriggerBlockEntity trigger = (TriggerBlockEntity) helper.getBlockEntity(BlockPos.ZERO);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        helper.assertTrue(
                trigger.configure(player, program, "twice", 20).isEmpty(), "The trigger block can't be configured");
        for (int i = 0; i < inputs.length; i++) {
            trigger.input().setItem(i, inputs[i]);
        }
        return trigger;
    }

    /** A process that crashes returns its input items and stores the error. */
    public static void crashedProcessesReturnTheirInputs(GameTestHelper helper) {
        ServerLevel level = c0de(helper);
        SheetBuilder sheet = new SheetBuilder(level, 510).place(twice("crashing"));
        SheetValidation.validate(level, 510);
        TriggerBlockEntity trigger = trigger(helper, "crashing", variable("value", "INTEGER", "21"));
        trigger.start();
        helper.assertTrue(trigger.isRunning(), "The process didn't start: " + trigger.status());
        BlockPos pid = LevelSheet.pidPos(510, trigger.pid());
        helper.assertTrue(level.getBlockState(pid).is(C0deBlocks.PID_BLOCK.get()), "There is no PID block");
        helper.assertTrue(SheetProtection.isProtected(level, sheet.pos(5, 6)), "The sheet is not protected");
        helper.runAfterDelay(40, () -> {
            try {
                helper.assertTrue(!trigger.isRunning(), "The process is still running");
                helper.assertTrue(trigger.exitCode() == Cursor.CRASH_EXIT_CODE, "Exit code " + trigger.exitCode());
                helper.assertTrue(
                        trigger.error().getString("code").equals("303"), "The error is " + trigger.error());
                ValueItem.Contents returned = ValueItem.Contents.of(trigger.output().getItem(0));
                helper.assertTrue(
                        returned.name().equals("value") && returned.value().equals("21"),
                        "The input was not returned: " + returned);
                helper.assertTrue(level.getBlockState(pid).isAir(), "The PID block was not removed");
                helper.assertTrue(!SheetProtection.isProtected(level, sheet.pos(5, 6)), "The sheet is still protected");
            } finally {
                sheet.clear();
                SheetValidation.validate(level, 510);
            }
            helper.succeed();
        });
    }

    /**
     * Of a stack of uninitialized variable items, a process takes one per result without parameter; the rest stays
     * in the input inventory. Initialized items that are not used are passed through to the output inventory.
     * Items in the output inventory don't prevent the start as long as there is space; a full one does.
     */
    public static void processesTakeOnlyTheItemsTheyUse(GameTestHelper helper) {
        ServerLevel level = c0de(helper);
        SheetBuilder sheet = new SheetBuilder(level, 514).place("""
                PROGRAM pairs
                NOOP
                FUNCTION pair
                    RESULT first INTEGER
                    RESULT second INTEGER
                BEGIN
                    ASSIGN first 1 ;
                    ASSIGN second 2 ;
                END FUNCTION
                """);
        SheetValidation.validate(level, 514);
        helper.setBlock(BlockPos.ZERO, C0deBlocks.TRIGGER_BLOCK.get());
        TriggerBlockEntity trigger = (TriggerBlockEntity) helper.getBlockEntity(BlockPos.ZERO);
        trigger.configure(helper.makeMockServerPlayerInLevel(), "pairs", "pair", 20);
        trigger.input().setItem(0, new ItemStack(C0deItems.VARIABLE_ITEM.get(), 5));
        trigger.input().setItem(1, variable("spare", "INTEGER", "3"));
        trigger.output().setItem(0, variable("old", "INTEGER", "4"));
        trigger.start();
        helper.assertTrue(trigger.isRunning(), "The process didn't start: " + trigger.status());
        helper.assertTrue(trigger.input().getItem(0).getCount() == 3, "Uninitialized items left: "
                + trigger.input().getItem(0).getCount());
        helper.assertTrue(trigger.input().getItem(1).isEmpty(), "The spare item was not passed through");
        helper.runAfterDelay(40, () -> {
            try {
                helper.assertTrue(!trigger.isRunning(), "The process is still running");
                String names = "";
                for (int i = 0; i < trigger.output().getContainerSize(); i++) {
                    if (!trigger.output().getItem(i).isEmpty()) {
                        names += ValueItem.Contents.of(trigger.output().getItem(i)).name() + " ";
                    }
                }
                helper.assertTrue(names.equals("old first second spare "), "The output contains " + names);
                for (int i = 0; i < trigger.output().getContainerSize(); i++) {
                    trigger.output().setItem(i, variable("old" + i, "INTEGER", "4"));
                }
                trigger.start();
                helper.assertTrue(!trigger.isRunning(), "The process started with a full output inventory");
                helper.assertTrue(trigger.status().contains("no space"), "The status is " + trigger.status());
            } finally {
                sheet.clear();
                SheetValidation.validate(level, 514);
            }
            helper.succeed();
        });
    }

    /** The output of a trigger block goes into the input of an adjacent trigger block, its only neighbor. */
    public static void triggerBlocksCanBeChained(GameTestHelper helper) {
        ServerLevel level = c0de(helper);
        SheetBuilder sheet = new SheetBuilder(level, 515).place("""
                PROGRAM chained
                NOOP
                FUNCTION pair
                    RESULT first INTEGER
                BEGIN
                    ASSIGN first 1 ;
                END FUNCTION
                """);
        SheetValidation.validate(level, 515);
        helper.setBlock(BlockPos.ZERO, C0deBlocks.TRIGGER_BLOCK.get());
        helper.setBlock(new BlockPos(1, 0, 0), C0deBlocks.TRIGGER_BLOCK.get());
        TriggerBlockEntity first = (TriggerBlockEntity) helper.getBlockEntity(BlockPos.ZERO);
        TriggerBlockEntity second = (TriggerBlockEntity) helper.getBlockEntity(new BlockPos(1, 0, 0));
        first.configure(helper.makeMockServerPlayerInLevel(), "chained", "pair", 20);
        first.input().setItem(0, new ItemStack(C0deItems.VARIABLE_ITEM.get()));
        first.input().setItem(1, variable("spare", "INTEGER", "3"));
        first.start();
        helper.assertTrue(first.isRunning(), "The process didn't start: " + first.status());
        helper.runAfterDelay(40, () -> {
            try {
                helper.assertTrue(!first.isRunning(), "The process is still running");
                helper.assertTrue(first.output().isEmpty(), "The output was not transferred");
                String names = "";
                for (int i = 0; i < second.input().getContainerSize(); i++) {
                    if (!second.input().getItem(i).isEmpty()) {
                        names += ValueItem.Contents.of(second.input().getItem(i)).name() + " ";
                    }
                }
                helper.assertTrue(names.equals("first spare "), "The next input contains " + names);
            } finally {
                sheet.clear();
                SheetValidation.validate(level, 515);
            }
            helper.succeed();
        });
    }

    /** A process returns its results as variable items, and a comparator reads its exit code. */
    public static void processesReturnTheirResults(GameTestHelper helper) {
        ServerLevel level = c0de(helper);
        SheetBuilder sheet = new SheetBuilder(level, 511)
                .place(twice("doubling").replace("    ASSIGN result result / ( value - value ) ;\n", "    EXIT 7 ;\n"));
        SheetValidation.validate(level, 511);
        TriggerBlockEntity trigger = trigger(helper, "doubling", variable("value", "INTEGER", "21"));
        trigger.start();
        helper.assertTrue(trigger.isRunning(), "The process didn't start: " + trigger.status());
        helper.runAfterDelay(40, () -> {
            try {
                helper.assertTrue(trigger.exitCode() == 7, "Exit code " + trigger.exitCode());
                helper.assertTrue(trigger.comparatorSignal() == 7, "Comparator " + trigger.comparatorSignal());
                ValueItem.Contents result = ValueItem.Contents.of(trigger.output().getItem(0));
                helper.assertTrue(
                        result.name().equals("result") && result.type().equals("INTEGER") && result.value().equals("42"),
                        "The result is " + result);
            } finally {
                sheet.clear();
                SheetValidation.validate(level, 511);
            }
            helper.succeed();
        });
    }

    /** A trigger block doesn't start without the inputs of the entry function. */
    public static void missingInputsPreventTheStart(GameTestHelper helper) {
        ServerLevel level = c0de(helper);
        SheetBuilder sheet = new SheetBuilder(level, 512).place(twice("waiting"));
        try {
            SheetValidation.validate(level, 512);
            TriggerBlockEntity trigger = trigger(helper, "waiting", variable("other", "INTEGER", "1"));
            trigger.start();
            helper.assertTrue(!trigger.isRunning(), "The process started without its input");
            helper.assertTrue(trigger.status().contains("value"), "The status is " + trigger.status());
            trigger.input().setItem(0, variable("value", "STRING", "\"1\""));
            trigger.start();
            helper.assertTrue(trigger.status().contains("type"), "The wrong type is accepted: " + trigger.status());
        } finally {
            sheet.clear();
            SheetValidation.validate(level, 512);
        }
        helper.succeed();
    }

    /** Removing the trigger block stops its process and removes the PID block. */
    public static void removingTheTriggerStopsTheProcess(GameTestHelper helper) {
        ServerLevel level = c0de(helper);
        SheetBuilder sheet = new SheetBuilder(level, 513)
                .place(twice("stopping").replace("    ASSIGN result result / ( value - value ) ;\n", """
                            WHILE TRUE ;
                            BEGIN
                                ASSIGN result result + 1 ;
                            END WHILE
                        """));
        SheetValidation.validate(level, 513);
        TriggerBlockEntity trigger = trigger(helper, "stopping", variable("value", "INTEGER", "1"));
        trigger.start();
        BlockPos pid = LevelSheet.pidPos(513, trigger.pid());
        helper.runAfterDelay(5, () -> {
            try {
                helper.assertTrue(trigger.isRunning(), "The endless loop ended");
                helper.setBlock(BlockPos.ZERO, Blocks.AIR);
                helper.assertTrue(level.getBlockState(pid).isAir(), "The PID block was not removed");
            } finally {
                sheet.clear();
                SheetValidation.validate(level, 513);
            }
            helper.succeed();
        });
    }

    /** A cursor stored as NBT continues where it stopped (task 8.7). */
    @SuppressWarnings("unchecked")
    public static void cursorsSurviveSavingAsNbt(GameTestHelper helper) {
        Program program = ProgramParser.parse(Notation.parse(
                """
                PROGRAM saving
                NOOP
                FUNCTION count
                    RESULT total INTEGER
                BEGIN
                    FOR i INTEGER 1 ; 10 ;
                    BEGIN
                        ASSIGN total total + i ;
                    END FOR
                END FUNCTION
                """));
        CursorSettings settings = CursorSettings.of(ModLibraries.registry());
        Cursor cursor = new Cursor(program, "count", Map.of(), settings);
        cursor.run(7);
        Object stored = SnapshotNbt.fromNbt(SnapshotNbt.toNbt(CursorSnapshot.save(cursor)));
        Cursor restored = CursorSnapshot.restore((Map<String, Object>) stored, program, settings);
        restored.run(1000);
        Value total = restored.results().get("total");
        helper.assertTrue(new IntegerValue(55).equals(total), "The total is " + total);
        helper.succeed();
    }

    /** Variable items get their subtype and value in their dialog; constants can't be changed once set. */
    public static void valueItemsAreEdited(GameTestHelper helper) {
        ItemStack variable = variable("", "ARRAY", "");
        ValueItem item = (ValueItem) variable.getItem();
        helper.assertTrue(
                item.apply(variable, "list", "ARRAY", "[1]").isPresent(), "An ARRAY without subtype was accepted");
        helper.assertTrue(
                item.apply(variable, "list", "STRING", "").isPresent(), "The kind of type could be changed");
        helper.assertTrue(
                item.apply(variable, "list", "ARRAY INTEGER", "[1, 2]").isEmpty(), "A valid value was rejected");
        ValueItem.Contents contents = ValueItem.Contents.of(variable);
        helper.assertTrue(
                contents.type().equals("ARRAY INTEGER") && contents.value().equals("[1,2]"), "Stored: " + contents);

        ItemStack constant = new ItemStack(C0deItems.CONSTANT_ITEM.get());
        new ValueItem.Contents("", "INTEGER", "").writeTo(constant);
        ValueItem constantItem = (ValueItem) constant.getItem();
        helper.assertTrue(constantItem.apply(constant, "limit", "INTEGER", "10").isEmpty(), "The constant was not set");
        helper.assertTrue(
                constantItem.apply(constant, "limit", "INTEGER", "11").isPresent(), "The constant could be changed");
        helper.succeed();
    }
}
