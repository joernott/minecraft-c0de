package de.joernott.c0de.gametest;

import de.joernott.c0de.blockentity.TriggerBlockEntity;
import de.joernott.c0de.compat.Compat;
import de.joernott.c0de.content.C0deBlocks;
import de.joernott.c0de.content.C0deItems;
import de.joernott.c0de.dimension.C0deDimension;
import de.joernott.c0de.item.ValueItem;
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

/** GameTests of block data, MOVE_ITEMS and COMMAND of c0de/minecraft (tasks 16.11 to 16.13). */
public final class WorldAccessTests {
    private WorldAccessTests() {}

    private static ItemStack variable(String name, String type, String value) {
        ItemStack item = new ItemStack(C0deItems.VARIABLE_ITEM.get());
        new ValueItem.Contents(name, type, value).writeTo(item);
        return item;
    }

    /** Starts the function run of a program with the position of the test as parameter base. */
    private static TriggerBlockEntity start(
            GameTestHelper helper, BlockPos at, ServerPlayer owner, String program, int results) {
        BlockPos base = helper.absolutePos(BlockPos.ZERO);
        helper.setBlock(at, C0deBlocks.TRIGGER_BLOCK.get());
        TriggerBlockEntity trigger = (TriggerBlockEntity) TestCompat.blockEntity(helper, at);
        trigger.configure(owner, program, "run", 20);
        trigger.input().setItem(0, variable(
                "base",
                "STRUCT c0de/minecraft/POSITION",
                "{\"x\": %d, \"y\": %d, \"z\": %d, \"dimension\": \"\"}".formatted(base.getX(), base.getY(), base.getZ())));
        for (int i = 1; i <= results; i++) {
            trigger.input().setItem(i, variable("", "", ""));
        }
        trigger.start();
        helper.assertTrue(trigger.isRunning(), "The process didn't start: " + trigger.status());
        return trigger;
    }

    private static Map<String, String> results(TriggerBlockEntity trigger) {
        Map<String, String> results = new HashMap<>();
        for (int i = 0; i < trigger.output().getContainerSize(); i++) {
            ValueItem.Contents result = ValueItem.Contents.of(trigger.output().getItem(i));
            if (result != null && result.name() != null && !result.name().isEmpty()) {
                results.put(result.name(), result.value());
            }
        }
        return results;
    }

    private static final String DATA = """
            PROGRAM blockdata
            NOOP
            INCLUDE c0de/minecraft
            NOOP
            FUNCTION run
                PARAMETER base STRUCT c0de/minecraft/POSITION
                RESULT data STRING
                RESULT invalid INTEGER
                RESULT noentity INTEGER
                RESULT stone STRING
            BEGIN
                VARIABLE problem ERROR
                VARIABLE chest STRUCT c0de/minecraft/POSITION
                VARIABLE copy STRUCT c0de/minecraft/POSITION
                ASSIGN chest base ;
                ASSIGN chest.x base.x + 2 ;
                ASSIGN copy base ;
                ASSIGN copy.x base.x + 4 ;
                CALL c0de/minecraft/GET_BLOCK
                    PARAMETER position chest ;
                    RESULT data data
                CALL c0de/minecraft/PLACE_BLOCK
                    PARAMETER position copy ;
                    PARAMETER id "minecraft:chest" ;
                    PARAMETER data data ;
                ASSIGN copy.z base.z + 2 ;
                CALL c0de/minecraft/PLACE_BLOCK problem
                    PARAMETER position copy ;
                    PARAMETER id "minecraft:chest" ;
                    PARAMETER data "{Items:[" ;
                ASSIGN invalid problem.code ;
                CALL c0de/minecraft/PLACE_BLOCK problem
                    PARAMETER position copy ;
                    PARAMETER id "minecraft:stone" ;
                    PARAMETER data "{}" ;
                ASSIGN noentity problem.code ;
                ASSIGN copy.z base.z + 4 ;
                CALL c0de/minecraft/GET_BLOCK
                    PARAMETER position copy ;
                    RESULT data stone
            END FUNCTION
            """;

    /**
     * GET_BLOCK returns the data of a chest, and PLACE_BLOCK places a copy of it with its content (task 16.11). Data
     * that isn't SNBT and data for a block without block entity are refused before anything is placed.
     */
    public static void blockDataIsCopied(GameTestHelper helper) {
        ServerLevel c0de = helper.getLevel().getServer().getLevel(C0deDimension.LEVEL);
        SheetBuilder sheet = new SheetBuilder(c0de, 550).place(DATA);
        SheetValidation.validate(c0de, 550);
        ServerPlayer admin = helper.makeMockServerPlayerInLevel();
        TestCompat.op(helper.getLevel().getServer(), admin);
        SheetOwners.get(c0de).check(admin, 550, true);
        helper.setBlock(new BlockPos(2, 0, 0), Blocks.CHEST);
        Container chest = (Container) TestCompat.blockEntity(helper, new BlockPos(2, 0, 0));
        chest.setItem(0, new ItemStack(Items.COBBLESTONE, 12));
        chest.setItem(5, new ItemStack(Items.DIAMOND, 3));
        helper.setBlock(new BlockPos(4, 0, 4), Blocks.STONE);
        TriggerBlockEntity trigger = start(helper, BlockPos.ZERO, admin, "blockdata", 4);
        TestCompat.await(helper, () -> !trigger.isRunning(), () -> {
            try {
                helper.assertTrue(!trigger.isRunning(), "The process is still running");
                helper.assertTrue(trigger.exitCode() == 0, "Exit code " + trigger.exitCode() + " " + trigger.error());
                Map<String, String> results = results(trigger);
                helper.assertTrue(
                        results.get("data").contains("minecraft:cobblestone")
                                && !results.get("data").contains("x:"),
                        "The data is " + results.get("data"));
                helper.assertTrue(
                        results.get("invalid").equals("309") && results.get("noentity").equals("309"),
                        "The errors are " + results);
                helper.assertTrue(results.get("stone").equals("\"\""), "Stone has the data " + results.get("stone"));
                helper.assertBlockPresent(Blocks.CHEST, new BlockPos(4, 0, 0));
                Container copy = (Container) TestCompat.blockEntity(helper, new BlockPos(4, 0, 0));
                helper.assertTrue(
                        copy.getItem(0).is(Items.COBBLESTONE)
                                && copy.getItem(0).getCount() == 12
                                && copy.getItem(5).is(Items.DIAMOND)
                                && copy.getItem(5).getCount() == 3,
                        "The copy contains " + copy.getItem(0) + " and " + copy.getItem(5));
                helper.assertBlockPresent(Blocks.AIR, new BlockPos(4, 0, 2));
            } finally {
                TestCompat.deop(helper.getLevel().getServer(), admin);
                sheet.clear();
                SheetValidation.validate(c0de, 550);
            }
            helper.succeed();
        });
    }

    private static final String MOVING = """
            PROGRAM moving
            NOOP
            INCLUDE c0de/minecraft
            NOOP
            FUNCTION run
                PARAMETER base STRUCT c0de/minecraft/POSITION
                RESULT moved INTEGER
                RESULT ticks INTEGER
                RESULT one INTEGER
                RESULT diamonds INTEGER
                RESULT badfilter INTEGER
                RESULT badcount INTEGER
                RESULT both INTEGER
                RESULT locked INTEGER
                RESULT data INTEGER
            BEGIN
                VARIABLE problem ERROR
                VARIABLE start INTEGER
                VARIABLE ids ARRAY STRING
                VARIABLE source STRUCT c0de/minecraft/POSITION
                VARIABLE target STRUCT c0de/minecraft/POSITION
                VARIABLE safe STRUCT c0de/minecraft/POSITION
                VARIABLE place STRUCT c0de/minecraft/POSITION
                ASSIGN source base ;
                ASSIGN source.x base.x + 2 ;
                ASSIGN target source ;
                ASSIGN target.z base.z + 2 ;
                ASSIGN safe base ;
                ASSIGN safe.x base.x + 4 ;
                ASSIGN place base ;
                ASSIGN place.x base.x + 4 ;
                ASSIGN place.z base.z + 4 ;
                CALL c0de/minecraft/TICKS
                    RESULT result start
                CALL c0de/minecraft/MOVE_ITEMS
                    PARAMETER from source ;
                    PARAMETER to target ;
                    PARAMETER count -1 ;
                    PARAMETER filter "$logs | &raw_" ;
                    RESULT result moved
                CALL c0de/minecraft/TICKS
                    RESULT result ticks
                ASSIGN ticks ticks - start ;
                CALL c0de/minecraft/MOVE_ITEMS
                    PARAMETER from source ;
                    PARAMETER to target ;
                    PARAMETER id "minecraft:cobblestone" ;
                    RESULT result one
                CALL INSERT
                    PARAMETER array ids ;
                    PARAMETER index 0 ;
                    PARAMETER value "minecraft:diamond" ;
                    RESULT result ids
                CALL c0de/minecraft/MOVE_ITEMS
                    PARAMETER from source ;
                    PARAMETER to target ;
                    PARAMETER count 2 ;
                    PARAMETER filter ids ;
                    RESULT result diamonds
                CALL c0de/minecraft/MOVE_ITEMS problem
                    PARAMETER from source ;
                    PARAMETER to target ;
                    PARAMETER filter "#tooltip" ;
                ASSIGN badfilter problem.code ;
                CALL c0de/minecraft/MOVE_ITEMS problem
                    PARAMETER from source ;
                    PARAMETER to target ;
                    PARAMETER count 0 ;
                ASSIGN badcount problem.code ;
                CALL c0de/minecraft/MOVE_ITEMS problem
                    PARAMETER from source ;
                    PARAMETER to target ;
                    PARAMETER id "minecraft:diamond" ;
                    PARAMETER filter "diamond" ;
                ASSIGN both problem.code ;
                CALL c0de/minecraft/MOVE_ITEMS problem
                    PARAMETER from safe ;
                    PARAMETER to target ;
                ASSIGN locked problem.code ;
                CALL c0de/minecraft/PLACE_BLOCK problem
                    PARAMETER position place ;
                    PARAMETER id "minecraft:chest" ;
                    PARAMETER inventory target ;
                    PARAMETER data "{}" ;
                ASSIGN data problem.code ;
            END FUNCTION
            """;

    /**
     * MOVE_ITEMS works for owners who are not administrators (task 16.12): it moves all items that match a filter,
     * one item of an ID by default, the items of an array of IDs, and waits one tick per stack; filters it can't
     * search, a count of 0, id together with filter and locked containers are refused, and data for PLACE_BLOCK still
     * needs a permission.
     */
    public static void itemsAreMovedWithFilters(GameTestHelper helper) {
        ServerLevel c0de = helper.getLevel().getServer().getLevel(C0deDimension.LEVEL);
        SheetBuilder sheet = new SheetBuilder(c0de, 552).place(MOVING);
        SheetValidation.validate(c0de, 552);
        helper.setBlock(new BlockPos(2, 0, 0), Blocks.BARREL);
        helper.setBlock(new BlockPos(2, 0, 2), Blocks.BARREL);
        helper.setBlock(new BlockPos(4, 0, 0), Blocks.BARREL);
        Container source = (Container) TestCompat.blockEntity(helper, new BlockPos(2, 0, 0));
        Container target = (Container) TestCompat.blockEntity(helper, new BlockPos(2, 0, 2));
        TestCompat.lock(TestCompat.blockEntity(helper, new BlockPos(4, 0, 0)));
        Container safe = (Container) TestCompat.blockEntity(helper, new BlockPos(4, 0, 0));
        safe.setItem(0, new ItemStack(Items.GOLD_INGOT, 5));
        source.setItem(0, new ItemStack(Items.COBBLESTONE, 64));
        source.setItem(1, new ItemStack(Items.OAK_LOG, 10));
        source.setItem(2, new ItemStack(Items.RAW_IRON, 5));
        source.setItem(3, new ItemStack(Items.DIAMOND, 3));
        target.setItem(0, new ItemStack(Items.CHEST));
        TriggerBlockEntity trigger = start(helper, BlockPos.ZERO, helper.makeMockServerPlayerInLevel(), "moving", 9);
        TestCompat.await(helper, () -> !trigger.isRunning(), () -> {
            try {
                helper.assertTrue(!trigger.isRunning(), "The process is still running");
                helper.assertTrue(trigger.exitCode() == 0, "Exit code " + trigger.exitCode() + " " + trigger.error());
                Map<String, String> results = results(trigger);
                helper.assertTrue(
                        results.get("moved").equals("15")
                                && results.get("one").equals("1")
                                && results.get("diamonds").equals("2"),
                        "The moved items are " + results);
                helper.assertTrue(
                        results.get("badfilter").equals("309")
                                && results.get("badcount").equals("309")
                                && results.get("both").equals("309")
                                && results.get("locked").equals("311")
                                && results.get("data").equals("311"),
                        "The errors are " + results);
                // Two stacks over 2 blocks wait 2 ticks; TICKS and MOVE_ITEMS take a step each
                int ticks = Integer.parseInt(results.get("ticks"));
                helper.assertTrue(ticks >= 3 && ticks <= 30, "Moving two stacks took " + ticks + " ticks");
                helper.assertTrue(
                        target.countItem(Items.OAK_LOG) == 10
                                && target.countItem(Items.RAW_IRON) == 5
                                && target.countItem(Items.COBBLESTONE) == 1
                                && target.countItem(Items.DIAMOND) == 2
                                && source.countItem(Items.COBBLESTONE) == 63
                                && source.countItem(Items.DIAMOND) == 1,
                        "The items weren't moved as expected");
                helper.assertTrue(safe.countItem(Items.GOLD_INGOT) == 5, "Items were taken from a locked barrel");
                helper.assertBlockPresent(Blocks.AIR, new BlockPos(4, 0, 4));
            } finally {
                sheet.clear();
                SheetValidation.validate(c0de, 552);
            }
            helper.succeed();
        });
    }

    private static final String COMMANDS = """
            PROGRAM commands
            NOOP
            INCLUDE c0de/minecraft
            NOOP
            FUNCTION run
                PARAMETER base STRUCT c0de/minecraft/POSITION
                RESULT placed INTEGER
                RESULT output STRING
                RESULT failed INTEGER
                RESULT denied INTEGER
            BEGIN
                VARIABLE problem ERROR
                VARIABLE message STRING
                CALL c0de/minecraft/COMMAND problem
                    PARAMETER command "/setblock ~2 ~ ~ minecraft:gold_block" ;
                    RESULT result placed
                    RESULT output output
                CALL c0de/minecraft/COMMAND problem
                    PARAMETER command "nonsense" ;
                    RESULT result failed
                    RESULT output message
                ASSIGN denied problem.code ;
            END FUNCTION
            """;

    /**
     * COMMAND runs commands like a command block at the trigger block for administrators and returns their success
     * count and output; a command that fails is not an error. For other owners, it raises PERMISSION_DENIED (task
     * 16.13).
     */
    public static void commandsNeedAdministrators(GameTestHelper helper) {
        ServerLevel c0de = helper.getLevel().getServer().getLevel(C0deDimension.LEVEL);
        SheetBuilder sheet = new SheetBuilder(c0de, 554).place(COMMANDS);
        SheetValidation.validate(c0de, 554);
        ServerPlayer admin = helper.makeMockServerPlayerInLevel();
        TestCompat.op(helper.getLevel().getServer(), admin);
        SheetOwners.get(c0de).check(admin, 554, true);
        boolean enabled = Compat.commandBlocksWork(helper.getLevel());
        TriggerBlockEntity byAdmin = start(helper, BlockPos.ZERO, admin, "commands", 4);
        TriggerBlockEntity byPlayer =
                start(helper, new BlockPos(0, 0, 4), helper.makeMockServerPlayerInLevel(), "commands", 4);
        TestCompat.await(helper, () -> !byAdmin.isRunning() && !byPlayer.isRunning(), () -> {
            try {
                helper.assertTrue(!byAdmin.isRunning() && !byPlayer.isRunning(), "A process is still running");
                Map<String, String> admins = results(byAdmin);
                Map<String, String> players = results(byPlayer);
                if (enabled) {
                    helper.assertTrue(
                            admins.get("placed").equals("1")
                                    && !admins.get("output").isEmpty()
                                    && admins.get("failed").equals("0")
                                    && admins.get("denied").equals("0"),
                            "The administrator's results are " + admins);
                    helper.assertBlockPresent(Blocks.GOLD_BLOCK, new BlockPos(2, 0, 0));
                } else {
                    helper.assertTrue(admins.get("denied").equals("311"), "Command blocks are off, but " + admins);
                }
                helper.assertTrue(
                        players.get("placed").equals("0") && players.get("denied").equals("311"),
                        "The player's results are " + players);
                helper.assertBlockPresent(Blocks.AIR, new BlockPos(2, 0, 4));
            } finally {
                TestCompat.deop(helper.getLevel().getServer(), admin);
                sheet.clear();
                SheetValidation.validate(c0de, 554);
            }
            helper.succeed();
        });
    }

    /**
     * REMOVE_BLOCK destroys a block like a player who breaks it ({@code destroyBlock}): the contents of a container
     * fall out as items, also without {@code drop}, which only decides whether the block itself drops (task 17.5).
     */
    public static void removedContainersSpillTheirItems(GameTestHelper helper) {
        BlockPos chest = new BlockPos(1, 1, 1);
        helper.setBlock(chest, net.minecraft.world.level.block.Blocks.CHEST);
        BlockPos absolute = helper.absolutePos(chest);
        net.minecraft.server.level.ServerLevel level = helper.getLevel();
        ((net.minecraft.world.level.block.entity.ChestBlockEntity) level.getBlockEntity(absolute))
                .setItem(0, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND, 3));
        // What REMOVE_BLOCK does with drop FALSE
        level.destroyBlock(absolute, false);
        net.minecraft.world.phys.AABB around = new net.minecraft.world.phys.AABB(absolute).inflate(3);
        TestCompat.await(helper, () -> count(level, around, net.minecraft.world.item.Items.DIAMOND) == 3, () -> {
            if (count(level, around, net.minecraft.world.item.Items.CHEST) != 0) {
                throw new AssertionError("The chest dropped itself without drop");
            }
            helper.succeed();
        });
    }

    private static int count(
            net.minecraft.server.level.ServerLevel level,
            net.minecraft.world.phys.AABB area,
            net.minecraft.world.item.Item item) {
        return level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, area).stream()
                .filter(entity -> entity.getItem().is(item))
                .mapToInt(entity -> entity.getItem().getCount())
                .sum();
    }
}
