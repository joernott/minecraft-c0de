package de.joernott.c0de.gametest;

import de.joernott.c0de.blockentity.TriggerBlockEntity;
import de.joernott.c0de.content.C0deBlocks;
import de.joernott.c0de.content.C0deItems;
import de.joernott.c0de.dimension.C0deDimension;
import de.joernott.c0de.item.ValueItem;
import de.joernott.c0de.sheet.SheetValidation;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
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
            BEGIN
                VARIABLE problem ERROR
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
     * owner who is not an administrator.
     */
    public static void programsSwitchLevers(GameTestHelper helper) {
        ServerLevel c0de = helper.getLevel().getServer().getLevel(C0deDimension.LEVEL);
        SheetBuilder sheet = new SheetBuilder(c0de, 530).place(PROGRAM);
        SheetValidation.validate(c0de, 530);
        helper.setBlock(new BlockPos(2, 0, 0), Blocks.STONE);
        helper.setBlock(new BlockPos(2, 1, 0), Blocks.LEVER.defaultBlockState()
                .setValue(LeverBlock.FACE, AttachFace.FLOOR));
        BlockPos lever = helper.absolutePos(new BlockPos(2, 1, 0));
        helper.setBlock(BlockPos.ZERO, C0deBlocks.TRIGGER_BLOCK.get());
        TriggerBlockEntity trigger = (TriggerBlockEntity) helper.getBlockEntity(BlockPos.ZERO);
        trigger.configure(helper.makeMockServerPlayerInLevel(), "worldtest", "run", 20);
        trigger.input().setItem(0, variable(
                "lever",
                "STRUCT c0de/minecraft/POSITION",
                "{\"x\": %d, \"y\": %d, \"z\": %d, \"dimension\": \"\"}"
                        .formatted(lever.getX(), lever.getY(), lever.getZ())));
        trigger.input().setItem(1, variable("", "", ""));
        trigger.input().setItem(2, variable("", "", ""));
        trigger.start();
        helper.assertTrue(trigger.isRunning(), "The process didn't start: " + trigger.status());
        helper.runAfterDelay(40, () -> {
            try {
                helper.assertTrue(!trigger.isRunning(), "The process is still running");
                helper.assertTrue(trigger.exitCode() == 0, "Exit code " + trigger.exitCode() + " " + trigger.error());
                String results = "";
                for (int i = 0; i < 3; i++) {
                    ValueItem.Contents result = ValueItem.Contents.of(trigger.output().getItem(i));
                    results += result.name() + "=" + result.value() + " ";
                }
                helper.assertTrue(
                        results.equals("before=false after=true denied=311 "), "The results are " + results);
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
}
