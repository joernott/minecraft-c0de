package de.joernott.c0de.gametest;

import de.joernott.c0de.blockentity.TriggerBlockEntity;
import de.joernott.c0de.content.C0deBlocks;
import de.joernott.c0de.content.C0deItems;
import de.joernott.c0de.dimension.C0deDimension;
import de.joernott.c0de.item.ValueItem;
import de.joernott.c0de.process.Activator;
import de.joernott.c0de.sheet.SheetOwners;
import de.joernott.c0de.sheet.SheetValidation;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/** GameTests of the library c0de/minecraft (sprints 10 and 13). */
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
        TestCompat.await(helper, () -> !trigger.isRunning(), () -> {
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
        TestCompat.await(helper, () -> !trigger.isRunning(), () -> {
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

    private static final String MINING = """
            PROGRAM mining
            NOOP
            INCLUDE c0de/minecraft
            NOOP
            FUNCTION run
                PARAMETER base STRUCT c0de/minecraft/POSITION
                RESULT full INTEGER
                RESULT missing INTEGER
                RESULT denied INTEGER
                RESULT ticks INTEGER
            BEGIN
                VARIABLE problem ERROR
                VARIABLE start INTEGER
                VARIABLE tools STRUCT c0de/minecraft/POSITION
                VARIABLE output STRUCT c0de/minecraft/POSITION
                VARIABLE hopper STRUCT c0de/minecraft/POSITION
                VARIABLE target STRUCT c0de/minecraft/POSITION
                ASSIGN tools base ;
                ASSIGN tools.x base.x + 2 ;
                ASSIGN output tools ;
                ASSIGN output.z base.z + 2 ;
                ASSIGN hopper tools ;
                ASSIGN hopper.z base.z + 4 ;
                ASSIGN target base ;
                ASSIGN target.x base.x + 4 ;
                CALL c0de/minecraft/TICKS
                    RESULT result start
                CALL c0de/minecraft/MINE_BLOCK
                    PARAMETER position target ;
                    PARAMETER tool_inventory tools ;
                    PARAMETER output_inventory output ;
                CALL c0de/minecraft/TICKS
                    RESULT result ticks
                ASSIGN ticks ticks - start ;
                ASSIGN target.z base.z + 1 ;
                CALL c0de/minecraft/MINE_BLOCK
                    PARAMETER position target ;
                    PARAMETER tool_inventory tools ;
                    PARAMETER output_inventory output ;
                ASSIGN target.z base.z + 2 ;
                CALL c0de/minecraft/MINE_BLOCK
                    PARAMETER position target ;
                    PARAMETER tool_inventory tools ;
                    PARAMETER output_inventory output ;
                ASSIGN target.z base.z + 4 ;
                CALL c0de/minecraft/MINE_BLOCK problem
                    PARAMETER position target ;
                    PARAMETER tool_inventory tools ;
                    PARAMETER output_inventory hopper ;
                ASSIGN full problem.code ;
                ASSIGN target.z base.z + 3 ;
                CALL c0de/minecraft/MINE_BLOCK
                    PARAMETER position target ;
                    PARAMETER tool_inventory tools ;
                    PARAMETER output_inventory output ;
                ASSIGN target.x base.x + 5 ;
                ASSIGN target.z base.z ;
                CALL c0de/minecraft/PLACE_BLOCK
                    PARAMETER position target ;
                    PARAMETER id "minecraft:cobblestone" ;
                    PARAMETER inventory output ;
                ASSIGN target.z base.z + 1 ;
                CALL c0de/minecraft/PLACE_BLOCK problem
                    PARAMETER position target ;
                    PARAMETER id "minecraft:gold_block" ;
                    PARAMETER inventory output ;
                ASSIGN missing problem.code ;
                CALL c0de/minecraft/PLACE_BLOCK problem
                    PARAMETER position target ;
                    PARAMETER id "minecraft:cobblestone" ;
                ASSIGN denied problem.code ;
            END FUNCTION
            """;

    /**
     * MINE_BLOCK and PLACE_BLOCK with inventories work for owners who are not administrators (tasks 13.10, 13.11):
     * MINE_BLOCK takes the first suitable tool, damages it, waits like a player and refuses an inventory without
     * room; PLACE_BLOCK takes the block from the inventory.
     */
    public static void programsMineWithTools(GameTestHelper helper) {
        ServerLevel c0de = helper.getLevel().getServer().getLevel(C0deDimension.LEVEL);
        SheetBuilder sheet = new SheetBuilder(c0de, 543).place(MINING);
        SheetValidation.validate(c0de, 543);
        helper.setBlock(new BlockPos(2, 0, 0), Blocks.BARREL);
        helper.setBlock(new BlockPos(2, 0, 2), Blocks.BARREL);
        helper.setBlock(new BlockPos(2, 0, 4), Blocks.HOPPER);
        Container tools = (Container) TestCompat.blockEntity(helper, new BlockPos(2, 0, 0));
        Container output = (Container) TestCompat.blockEntity(helper, new BlockPos(2, 0, 2));
        Container hopper = (Container) TestCompat.blockEntity(helper, new BlockPos(2, 0, 4));
        tools.setItem(0, new ItemStack(Items.IRON_PICKAXE));
        tools.setItem(1, new ItemStack(Items.STONE_PICKAXE));
        tools.setItem(2, new ItemStack(Items.DIAMOND_PICKAXE));
        tools.setItem(3, new ItemStack(Items.SHEARS));
        for (int i = 0; i < hopper.getContainerSize(); i++) {
            hopper.setItem(i, new ItemStack(Items.STONE, 64));
        }
        helper.setBlock(new BlockPos(4, 0, 0), Blocks.COBBLESTONE);
        helper.setBlock(new BlockPos(4, 0, 1), Blocks.OBSIDIAN);
        helper.setBlock(
                new BlockPos(4, 0, 2), Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true));
        helper.setBlock(new BlockPos(4, 0, 3), Blocks.DIRT);
        helper.setBlock(new BlockPos(4, 0, 4), Blocks.DIRT);
        BlockPos base = helper.absolutePos(BlockPos.ZERO);
        helper.setBlock(BlockPos.ZERO, C0deBlocks.TRIGGER_BLOCK.get());
        TriggerBlockEntity trigger = (TriggerBlockEntity) TestCompat.blockEntity(helper, BlockPos.ZERO);
        trigger.configure(helper.makeMockServerPlayerInLevel(), "mining", "run", 20);
        trigger.input().setItem(0, variable(
                "base",
                "STRUCT c0de/minecraft/POSITION",
                "{\"x\": %d, \"y\": %d, \"z\": %d, \"dimension\": \"\"}".formatted(base.getX(), base.getY(), base.getZ())));
        for (int i = 1; i <= 4; i++) {
            trigger.input().setItem(i, variable("", "", ""));
        }
        trigger.start();
        helper.assertTrue(trigger.isRunning(), "The process didn't start: " + trigger.status());
        // Obsidian alone takes a player 188 ticks with a diamond pickaxe
        TestCompat.await(helper, () -> !trigger.isRunning(), () -> {
            try {
                helper.assertTrue(!trigger.isRunning(), "The process is still running");
                helper.assertTrue(trigger.exitCode() == 0, "Exit code " + trigger.exitCode() + " " + trigger.error());
                Map<String, String> results = new HashMap<>();
                for (int i = 0; i < trigger.output().getContainerSize(); i++) {
                    ValueItem.Contents result = ValueItem.Contents.of(trigger.output().getItem(i));
                    results.put(result.name(), result.value());
                }
                helper.assertTrue(
                        results.get("full").equals("312")
                                && results.get("missing").equals("313")
                                && results.get("denied").equals("311"),
                        "The errors are " + results);
                // A player needs 10 ticks for cobblestone with an iron pickaxe; TICKS and MINE_BLOCK take a step each
                int ticks = Integer.parseInt(results.get("ticks"));
                helper.assertTrue(ticks >= 12 && ticks <= 30, "Mining cobblestone took " + ticks + " ticks");
                for (int z = 0; z < 4; z++) {
                    helper.assertBlockPresent(Blocks.AIR, new BlockPos(4, 0, z));
                }
                helper.assertBlockPresent(Blocks.DIRT, new BlockPos(4, 0, 4));
                helper.assertBlockPresent(Blocks.COBBLESTONE, new BlockPos(5, 0, 0));
                helper.assertBlockPresent(Blocks.AIR, new BlockPos(5, 0, 1));
                String damage = "";
                for (int i = 0; i < 4; i++) {
                    damage += tools.getItem(i).getDamageValue() + " ";
                }
                helper.assertTrue(damage.equals("1 0 1 1 "), "The damage of the tools is " + damage);
                helper.assertTrue(
                        output.countItem(Items.OBSIDIAN) == 1
                                && output.countItem(Items.OAK_LEAVES) == 1
                                && output.countItem(Items.DIRT) == 1
                                && output.countItem(Items.COBBLESTONE) == 0,
                        "The output inventory has the wrong items");
            } finally {
                sheet.clear();
                SheetValidation.validate(c0de, 543);
            }
            helper.succeed();
        }, 380);
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
        TestCompat.await(helper, () -> !trigger.isRunning(), () -> {
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
