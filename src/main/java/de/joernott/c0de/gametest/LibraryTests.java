package de.joernott.c0de.gametest;

import de.joernott.c0de.blockentity.TriggerBlockEntity;
import de.joernott.c0de.content.C0deBlocks;
import de.joernott.c0de.content.C0deItems;
import de.joernott.c0de.dimension.C0deDimension;
import de.joernott.c0de.item.ValueItem;
import de.joernott.c0de.process.Activator;
import de.joernott.c0de.sheet.SheetOwners;
import de.joernott.c0de.sheet.SheetValidation;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/** GameTests of the library c0de/minecraft (sprint 10). */
public final class LibraryTests {
    private static final String PROGRAM =
            """
            PROGRAM worldtest
            NOOP
            INCLUDE c0de/minecraft
            NOOP
            FUNCTION run
                PARAMETER lever STRUCT c0de/minecraft/POSITION
                RESULT before BOOLEAN
                RESULT after BOOLEAN
                RESULT denied INTEGER
                RESULT barrel BOOLEAN
            BEGIN
                VARIABLE problem ERROR
                VARIABLE below STRUCT c0de/minecraft/POSITION
                ASSIGN below lever ;
                ASSIGN below.y lever.y - 1 ;
                CALL c0de/minecraft/GET_BLOCK
                    PARAMETER position below ;
                    RESULT has_inventory barrel
                CALL c0de/minecraft/GET_LEVER
                    PARAMETER position lever ;
                    RESULT result before
                CALL c0de/minecraft/SET_LEVER
                    PARAMETER position lever ;
                    PARAMETER on TRUE ;
                CALL c0de/minecraft/GET_LEVER
                    PARAMETER position lever ;
                    RESULT result after
                CALL c0de/minecraft/REMOVE_BLOCK problem
                    PARAMETER position lever ;
                ASSIGN denied problem.code ;
            END FUNCTION
            """;

    private LibraryTests() {}

    private static ItemStack variable(String name, String type, String value) {
        ItemStack item = new ItemStack(C0deItems.VARIABLE_ITEM.get());
        new ValueItem.Contents(name, type, value).writeTo(item);
        return item;
    }

    /**
     * A program switches a lever with SET_LEVER and reads it with GET_LEVER; REMOVE_BLOCK is not allowed for an
     * owner who is not an administrator. GET_BLOCK finds the inventory of the barrel below the lever.
     */
    public static void programsSwitchLevers(GameTestHelper helper) {
        ServerLevel c0de = helper.getLevel().getServer().getLevel(C0deDimension.LEVEL);
        SheetBuilder sheet = new SheetBuilder(c0de, 530).place(PROGRAM);
        SheetValidation.validate(c0de, 530);
        helper.setBlock(new BlockPos(2, 0, 0), Blocks.BARREL);
        helper.setBlock(new BlockPos(2, 1, 0), Blocks.LEVER.defaultBlockState()
                .setValue(LeverBlock.FACE, AttachFace.FLOOR));
        BlockPos lever = helper.absolutePos(new BlockPos(2, 1, 0));
        helper.setBlock(BlockPos.ZERO, C0deBlocks.TRIGGER_BLOCK.get());
        TriggerBlockEntity trigger = (TriggerBlockEntity) TestCompat.blockEntity(helper, BlockPos.ZERO);
        trigger.configure(helper.makeMockServerPlayerInLevel(), "worldtest", "run", 20);
        trigger.input().setItem(0, variable(
                "lever",
                "STRUCT c0de/minecraft/POSITION",
                "{\"x\": %d, \"y\": %d, \"z\": %d, \"dimension\": \"\"}"
                        .formatted(lever.getX(), lever.getY(), lever.getZ())));
        trigger.input().setItem(1, variable("", "", ""));
        trigger.input().setItem(2, variable("", "", ""));
        trigger.input().setItem(3, variable("", "", ""));
        trigger.start();
        helper.assertTrue(trigger.isRunning(), "The process didn't start: " + trigger.status());
        helper.runAfterDelay(40, () -> {
            try {
                helper.assertTrue(!trigger.isRunning(), "The process is still running");
                helper.assertTrue(trigger.exitCode() == 0, "Exit code " + trigger.exitCode() + " " + trigger.error());
                String results = "";
                for (int i = 0; i < 4; i++) {
                    ValueItem.Contents result = ValueItem.Contents.of(trigger.output().getItem(i));
                    results += result.name() + "=" + result.value() + " ";
                }
                helper.assertTrue(
                        results.equals("before=false after=true denied=311 barrel=true "), "The results are " + results);
                BlockState state = helper.getLevel().getBlockState(lever);
                helper.assertTrue(
                        state.is(Blocks.LEVER) && state.getValue(BlockStateProperties.POWERED),
                        "The lever is " + state);
            } finally {
                sheet.clear();
                SheetValidation.validate(c0de, 530);
            }
            helper.succeed();
        });
    }

    /**
     * The trigger block of an administrator doesn't give administrator rights to a program on the sheet of another
     * player (task 11.11): REMOVE_BLOCK is denied.
     */
    public static void administratorsRunOnlyTheirOwnPrograms(GameTestHelper helper) {
        ServerLevel c0de = helper.getLevel().getServer().getLevel(C0deDimension.LEVEL);
        // Each test has a program of its own name, because tests run at the same time
        SheetBuilder sheet = new SheetBuilder(c0de, 536).place(PROGRAM.replace("worldtest", "ownprogram"));
        SheetValidation.validate(c0de, 536);
        ServerPlayer author = helper.makeMockServerPlayerInLevel();
        ServerPlayer admin = helper.makeMockServerPlayerInLevel();
        TestCompat.op(helper.getLevel().getServer(), admin);
        SheetOwners.get(c0de).check(author, 536, true);
        helper.setBlock(new BlockPos(2, 0, 0), Blocks.STONE);
        helper.setBlock(new BlockPos(2, 1, 0), Blocks.LEVER.defaultBlockState().setValue(LeverBlock.FACE, AttachFace.FLOOR));
        BlockPos lever = helper.absolutePos(new BlockPos(2, 1, 0));
        helper.setBlock(BlockPos.ZERO, C0deBlocks.TRIGGER_BLOCK.get());
        TriggerBlockEntity trigger = (TriggerBlockEntity) TestCompat.blockEntity(helper, BlockPos.ZERO);
        trigger.configure(admin, "ownprogram", "run", 20);
        trigger.input().setItem(0, variable(
                "lever",
                "STRUCT c0de/minecraft/POSITION",
                "{\"x\": %d, \"y\": %d, \"z\": %d, \"dimension\": \"\"}"
                        .formatted(lever.getX(), lever.getY(), lever.getZ())));
        for (int i = 1; i <= 4; i++) {
            trigger.input().setItem(i, variable("", "", ""));
        }
        trigger.start();
        helper.assertTrue(trigger.isRunning(), "The process didn't start: " + trigger.status());
        helper.runAfterDelay(40, () -> {
            try {
                helper.assertTrue(!trigger.isRunning(), "The process is still running");
                String denied = "";
                for (int i = 0; i < trigger.output().getContainerSize(); i++) {
                    ValueItem.Contents result = ValueItem.Contents.of(trigger.output().getItem(i));
                    if (result.name().equals("denied")) {
                        denied = result.value();
                    }
                }
                helper.assertTrue(denied.equals("311"), "REMOVE_BLOCK returned " + denied + " instead of 311");
                helper.assertTrue(helper.getLevel().getBlockState(lever).is(Blocks.LEVER), "The lever was removed");
            } finally {
                TestCompat.deop(helper.getLevel().getServer(), admin);
                sheet.clear();
                SheetValidation.validate(c0de, 536);
            }
            helper.succeed();
        });
    }

    /**
     * The Run property decides who may start a trigger block, and a player who is not an administrator can't use
     * restricted functions through the trigger block of an administrator: the process crashes (task 11.11.8).
     */
    public static void triggersRunForTheRightActivators(GameTestHelper helper) {
        ServerLevel c0de = helper.getLevel().getServer().getLevel(C0deDimension.LEVEL);
        SheetBuilder sheet = new SheetBuilder(c0de, 538).place(PROGRAM.replace("worldtest", "activators"));
        SheetValidation.validate(c0de, 538);
        ServerPlayer admin = helper.makeMockServerPlayerInLevel();
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        TestCompat.op(helper.getLevel().getServer(), admin);
        SheetOwners.get(c0de).check(admin, 538, true);
        helper.setBlock(new BlockPos(2, 0, 0), Blocks.STONE);
        helper.setBlock(new BlockPos(2, 1, 0), Blocks.LEVER.defaultBlockState().setValue(LeverBlock.FACE, AttachFace.FLOOR));
        BlockPos lever = helper.absolutePos(new BlockPos(2, 1, 0));
        helper.setBlock(BlockPos.ZERO, C0deBlocks.TRIGGER_BLOCK.get());
        TriggerBlockEntity trigger = (TriggerBlockEntity) TestCompat.blockEntity(helper, BlockPos.ZERO);
        trigger.configure(admin, "activators", "run", 20, TriggerBlockEntity.Run.OWNER);
        helper.assertTrue(
                trigger.configure(player, "activators", "run", 20).isPresent(), "Another player changed the trigger block");
        trigger.input().setItem(0, variable(
                "lever",
                "STRUCT c0de/minecraft/POSITION",
                "{\"x\": %d, \"y\": %d, \"z\": %d, \"dimension\": \"\"}"
                        .formatted(lever.getX(), lever.getY(), lever.getZ())));
        for (int i = 1; i <= 4; i++) {
            trigger.input().setItem(i, variable("", "", ""));
        }
        trigger.start(Activator.NONE);
        helper.assertTrue(!trigger.isRunning(), "A signal without activator started an 'Only me' trigger block");
        trigger.start(Activator.player(player));
        helper.assertTrue(!trigger.isRunning(), "Another player started an 'Only me' trigger block");
        trigger.configure(admin, "activators", "run", 20, TriggerBlockEntity.Run.PLAYERS);
        trigger.start(Activator.player(player));
        helper.assertTrue(trigger.isRunning(), "A player can't start a 'Players' trigger block: " + trigger.status());
        helper.runAfterDelay(40, () -> {
            try {
                helper.assertTrue(!trigger.isRunning(), "The process is still running");
                helper.assertTrue(
                        trigger.exitCode() == 15, "REMOVE_BLOCK didn't crash the process: exit code " + trigger.exitCode());
                helper.assertTrue(helper.getLevel().getBlockState(lever).is(Blocks.LEVER), "The lever was removed");
            } finally {
                TestCompat.deop(helper.getLevel().getServer(), admin);
                sheet.clear();
                SheetValidation.validate(c0de, 538);
            }
            helper.succeed();
        });
    }
}
