package de.joernott.c0de.gametest;

import de.joernott.c0de.C0de;
import de.joernott.c0de.blockentity.TriggerBlockEntity;
import de.joernott.c0de.content.C0deBlocks;
import de.joernott.c0de.content.C0deItems;
import de.joernott.c0de.dimension.C0deDimension;
import de.joernott.c0de.process.ProcessManager;
import de.joernott.c0de.sheet.SheetValidation;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

/** GameTests of the performance of processes (task 11.10). */
public final class PerformanceTests {
    /** The number of processes that run at the same time. */
    public static final int PROCESSES = 100;

    private PerformanceTests() {}

    /**
     * 100 processes of one program run at the same time; one step of all of them must take only a small part of
     * a tick (50 ms), and saving them must be quick. The measured times are written to the log.
     */
    public static void manyProcessesRunAtTheSameTime(GameTestHelper helper) {
        ServerLevel c0de = helper.getLevel().getServer().getLevel(C0deDimension.LEVEL);
        SheetBuilder sheet = new SheetBuilder(c0de, 532).place("""
                PROGRAM busy
                NOOP
                FUNCTION count
                    RESULT sum INTEGER
                BEGIN
                    FOR i INTEGER 1 ; 1000 ;
                    BEGIN
                        ASSIGN sum sum + i ;
                    END FOR
                END FUNCTION
                """);
        SheetValidation.validate(c0de, 532);
        ProcessManager manager = ProcessManager.get(helper.getLevel().getServer());
        ServerPlayer owner = helper.makeMockServerPlayerInLevel();
        List<BlockPos> positions = new ArrayList<>();
        List<TriggerBlockEntity> triggers = new ArrayList<>();
        for (int i = 0; i < PROCESSES; i++) {
            BlockPos pos = new BlockPos(2 + i % 10, 1, 2 + i / 10);
            positions.add(pos);
            helper.setBlock(pos, C0deBlocks.TRIGGER_BLOCK.get());
            TriggerBlockEntity trigger = (TriggerBlockEntity) helper.getBlockEntity(pos);
            trigger.configure(owner, "busy", "count", 20);
            trigger.input().setItem(0, new ItemStack(C0deItems.VARIABLE_ITEM.get()));
            trigger.start();
            triggers.add(trigger);
        }
        long running = triggers.stream().filter(TriggerBlockEntity::isRunning).count();
        manager.resetTickTimes();
        helper.runAfterDelay(100, () -> {
            try {
                helper.assertTrue(
                        running == PROCESSES, "Only " + running + " processes started: " + triggers.get(0).status());
                long[] times = manager.tickTimes();
                long start = System.nanoTime();
                manager.save(new CompoundTag(), helper.getLevel().registryAccess());
                long saveMicros = (System.nanoTime() - start) / 1000;
                C0de.LOGGER.info(
                        "{} processes: {} µs per tick on average, {} µs at most; saving them took {} µs",
                        PROCESSES, times[0], times[1], saveMicros);
                helper.assertTrue(times[0] < 10_000, "A tick of the processes takes " + times[0] + " µs");
                helper.assertTrue(saveMicros < 200_000, "Saving the processes takes " + saveMicros + " µs");
            } finally {
                triggers.stream()
                        .filter(TriggerBlockEntity::isRunning)
                        .forEach(trigger -> manager.stop(helper.getLevel().getServer(), trigger.processId()));
                positions.forEach(pos -> helper.setBlock(pos, Blocks.AIR));
                sheet.clear();
                SheetValidation.validate(c0de, 532);
            }
            helper.succeed();
        });
    }
}
