package de.joernott.c0de.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.ServerOpListEntry;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

/** The APIs of the GameTests that differ between the Minecraft versions (task 12.1). */
final class TestCompat {
    private TestCompat() {}

    /** The most ticks {@link #await} waits: less than the timeout of the tests (100 ticks). */
    static final int MAX_WAIT = 90;

    /**
     * Runs the checks of a test as soon as the condition holds, at the latest after {@link #MAX_WAIT} ticks. The
     * processes of all tests share a time budget per tick (setting {@code tickBudget}), so while many tests run at
     * once, a process may need more ticks than usual.
     */
    static void await(GameTestHelper helper, java.util.function.BooleanSupplier condition, Runnable checks) {
        await(helper, condition, checks, MAX_WAIT);
    }

    private static void await(
            GameTestHelper helper, java.util.function.BooleanSupplier condition, Runnable checks, int ticksLeft) {
        if (condition.getAsBoolean() || ticksLeft <= 0) {
            checks.run();
        } else {
            helper.runAfterDelay(1, () -> await(helper, condition, checks, ticksLeft - 1));
        }
    }

    /** The block entity at a position relative to the test. */
    static BlockEntity blockEntity(GameTestHelper helper, BlockPos pos) {
        //? if >=1.21.5 {
        /*return helper.getBlockEntity(pos, BlockEntity.class);
        *///?} else {
        return helper.getBlockEntity(pos);
        //?}
    }

    /** Makes the player an operator with the highest permission level. */
    static void op(MinecraftServer server, ServerPlayer player) {
        //? if >=1.21.9 {
        /*server.getPlayerList().getOps().add(new ServerOpListEntry(
                new net.minecraft.server.players.NameAndId(player.getGameProfile()),
                net.minecraft.server.permissions.LevelBasedPermissionSet.OWNER,
                false));
        *///?} else {
        server.getPlayerList().getOps().add(new ServerOpListEntry(player.getGameProfile(), 4, false));
        //?}
    }

    static void deop(MinecraftServer server, ServerPlayer player) {
        //? if >=1.21.9 {
        /*server.getPlayerList().getOps().remove(new net.minecraft.server.players.NameAndId(player.getGameProfile()));
        *///?} else {
        server.getPlayerList().getOps().remove(player.getGameProfile());
        //?}
    }

    /** The registered block, or air. */
    static Block block(ResourceLocation id) {
        //? if >=1.21.2 {
        /*return BuiltInRegistries.BLOCK.getValue(id);
        *///?} else {
        return BuiltInRegistries.BLOCK.get(id);
        //?}
    }

    /** The registered item, or air. */
    static Item item(ResourceLocation id) {
        //? if >=1.21.2 {
        /*return BuiltInRegistries.ITEM.getValue(id);
        *///?} else {
        return BuiltInRegistries.ITEM.get(id);
        //?}
    }

    /** True if the recipe exists. */
    static boolean hasRecipe(MinecraftServer server, ResourceLocation id) {
        //? if >=1.21.2 {
        /*return server.getRecipeManager()
                .byKey(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.RECIPE, id))
                .isPresent();
        *///?} else {
        return server.getRecipeManager().byKey(id).isPresent();
        //?}
    }
}
