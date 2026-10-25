package de.joernott.c0de.gametest;

import de.joernott.c0de.C0de;
import de.joernott.c0de.compat.Compat;
import de.joernott.c0de.content.C0deBlocks;
import de.joernott.c0de.core.config.C0deConfig;
import de.joernott.c0de.core.sheet.SheetGeometry;
import de.joernott.c0de.dimension.C0deDimension;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;

/** GameTests of the c0de dimension (sprint 2). */
public final class DimensionTests {
    private DimensionTests() {}

    private static ServerLevel c0de(GameTestHelper helper) {
        ServerLevel level = helper.getLevel().getServer().getLevel(C0deDimension.LEVEL);
        helper.assertTrue(level != null, "The c0de dimension doesn't exist");
        return level;
    }

    public static void dimensionHasTheRequiredProperties(GameTestHelper helper) {
        ServerLevel level = c0de(helper);
        helper.assertTrue(Compat.minY(level) == SheetGeometry.MIN_Y, "Bottom is not at y=-16");
        int maxBuildHeight = C0de.config().get(C0deConfig.MAX_BUILD_HEIGHT);
        helper.assertTrue(
                Compat.maxBuildHeight(level) == maxBuildHeight + 1,
                "The highest usable y coordinate is " + (Compat.maxBuildHeight(level) - 1) + " instead of " + maxBuildHeight);
        helper.assertTrue(!level.dimensionType().hasSkyLight(), "The dimension has sky light (and weather)");
        helper.assertTrue(level.dimensionType().hasFixedTime(), "The dimension has a daylight cycle");
        Biome biome = level.getBiome(new BlockPos(5, SheetGeometry.CODE_Y, 5)).value();
        helper.assertTrue(!biome.hasPrecipitation(), "It can rain in the c0de dimension");
        helper.succeed();
    }

    public static void floorHasTheGreenbarPattern(GameTestHelper helper) {
        ServerLevel level = c0de(helper);
        // Sheets 0 and 3, two lines each, and positions outside of the sheets
        int[][] areas = {{0, 0}, {3 * SheetGeometry.SHEET_WIDTH, 1000}, {-SheetGeometry.SHEET_WIDTH, 0}, {0, -4}};
        for (int[] area : areas) {
            for (int dz = 0; dz < 2; dz++) {
                for (int dx = 0; dx < SheetGeometry.SHEET_WIDTH; dx++) {
                    int x = area[0] + dx;
                    int z = area[1] + dz;
                    BlockState expected = C0deBlocks.floorBlock(SheetGeometry.floorAt(x, z));
                    BlockState actual = level.getBlockState(new BlockPos(x, SheetGeometry.FLOOR_Y, z));
                    helper.assertTrue(
                            actual.equals(expected),
                            "Floor at " + x + ", " + z + " is " + actual + " instead of " + expected);
                    helper.assertTrue(
                            level.getBlockState(new BlockPos(x, SheetGeometry.CODE_Y, z)).isAir(),
                            "The code layer at " + x + ", " + z + " is not empty");
                    helper.assertTrue(
                            level.getBlockState(new BlockPos(x, SheetGeometry.FLOOR_Y - 1, z)).isAir(),
                            "There are blocks below the floor at " + x + ", " + z);
                }
            }
        }
        helper.succeed();
    }

    public static void floorIsUnbreakable(GameTestHelper helper) {
        ServerLevel level = c0de(helper);
        BlockPos[] positions = {
            new BlockPos(1, SheetGeometry.FLOOR_Y, 0), // white
            new BlockPos(1, SheetGeometry.FLOOR_Y, 1), // light green
            new BlockPos(0, SheetGeometry.FLOOR_Y, 0), // black
            new BlockPos(4, SheetGeometry.FLOOR_Y, 0), // cursor rail
        };
        for (BlockPos pos : positions) {
            BlockState state = level.getBlockState(pos);
            helper.assertTrue(state.getDestroySpeed(level, pos) < 0, state + " can be broken");
            helper.assertTrue(
                    state.getBlock().getExplosionResistance() >= 3600000.0F, state + " can be destroyed by explosions");
            helper.assertTrue(state.getPistonPushReaction() == PushReaction.BLOCK, state + " can be moved by pistons");
        }
        helper.succeed();
    }

    public static void noMobsSpawn(GameTestHelper helper) {
        ServerLevel level = c0de(helper);
        Biome biome = level.getBiome(new BlockPos(5, SheetGeometry.CODE_Y, 5)).value();
        for (MobCategory category : MobCategory.values()) {
            helper.assertTrue(
                    biome.getMobSettings().getMobs(category).isEmpty(), "Mobs of the category " + category + " spawn");
        }
        helper.succeed();
    }

    public static void playerEntersAndLeavesTheDimension(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        BlockPos trigger = helper.absolutePos(BlockPos.ZERO);
        Vec3 start = player.position();
        net.minecraft.resources.ResourceKey<Level> origin = player.level().dimension();

        C0deDimension.enter(player, trigger);
        helper.assertTrue(player.level().dimension().equals(C0deDimension.LEVEL), "The player is not in the c0de dimension");
        Vec3 arrival = new Vec3(SheetGeometry.CODE_MIN_X + 0.5, SheetGeometry.CODE_Y, -1.5);
        helper.assertTrue(
                player.position().distanceTo(arrival) < 0.01,
                "The player arrived at " + player.position() + " instead of " + arrival);

        C0deDimension.leave(player);
        helper.assertTrue(player.level().dimension().equals(origin), "The player didn't return to " + origin.location());
        helper.assertTrue(
                player.position().distanceTo(start) < 0.01,
                "The player returned to " + player.position() + " instead of " + start);
        helper.succeed();
    }
}
