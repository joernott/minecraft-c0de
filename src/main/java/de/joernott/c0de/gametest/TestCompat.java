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
