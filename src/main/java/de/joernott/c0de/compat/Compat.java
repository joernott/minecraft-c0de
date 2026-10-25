package de.joernott.c0de.compat;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;

/**
 * Small differences between the Minecraft versions of the mod (task 12.1), each behind one method, so that the rest
 * of the code reads the same on all versions.
 */
public final class Compat {
    /** The permission level of administrators: level 2, the game masters. */
    public static final int ADMIN_LEVEL = 2;

    private Compat() {}

    /** The level a server player is in. */
    public static ServerLevel level(ServerPlayer player) {
        //? if >=1.21.6 {
        /*return player.level();
        *///?} else {
        return player.serverLevel();
        //?}
    }

    /** The server of a server player. */
    public static net.minecraft.server.MinecraftServer server(ServerPlayer player) {
        //? if >=1.21.6 {
        /*return player.level().getServer();
        *///?} else {
        return player.server;
        //?}
    }

    /** True for administrators: operators with permission level 2 or higher. */
    public static boolean isAdministrator(Player player) {
        //? if >=1.21.11 {
        /*return player.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER);
        *///?} else {
        return player.hasPermissions(ADMIN_LEVEL);
        //?}
    }

    /** True if the player is an administrator, online or not (as far as the server knows the player). */
    public static boolean isAdministrator(net.minecraft.server.MinecraftServer server, UUID player) {
        if (player == null) {
            return false;
        }
        ServerPlayer online = server.getPlayerList().getPlayer(player);
        if (online != null) {
            return isAdministrator(online);
        }
        //? if >=1.21.9 {
        /*return server.services().nameToIdCache().get(player)
                .map(known -> server.getProfilePermissions(known)
                        .level()
                        .isEqualOrHigherThan(net.minecraft.server.permissions.PermissionLevel.GAMEMASTERS))
                .orElse(false);
        *///?} else {
        return server.getProfileCache() != null
                && server.getProfileCache().get(player)
                        .map(profile -> server.getProfilePermissions(profile) >= ADMIN_LEVEL)
                        .orElse(false);
        //?}
    }

    /** The name of a player the server knows, online or not. */
    public static java.util.Optional<String> knownName(net.minecraft.server.MinecraftServer server, UUID player) {
        ServerPlayer online = server.getPlayerList().getPlayer(player);
        if (online != null) {
            return java.util.Optional.of(name(online.getGameProfile()));
        }
        //? if >=1.21.9 {
        /*return server.services().nameToIdCache().get(player).map(known -> known.name());
        *///?} else {
        return server.getProfileCache() == null
                ? java.util.Optional.empty()
                : server.getProfileCache().get(player).map(GameProfile::getName);
        //?}
    }

    /** True if the block is within the reach of the player, plus a tolerance in blocks. */
    public static boolean canReach(Player player, BlockPos pos, double tolerance) {
        //? if >=1.21.11 {
        /*return player.isWithinBlockInteractionRange(pos, tolerance);
        *///?} else {
        return player.canInteractWithBlock(pos, tolerance);
        //?}
    }

    /** Teleports a player to a position of a level, looking in the given direction. */
    public static void teleport(ServerPlayer player, ServerLevel level, double x, double y, double z, float yaw, float pitch) {
        //? if >=1.21.2 {
        /*player.teleportTo(level, x, y, z, java.util.Set.of(), yaw, pitch, true);
        *///?} else {
        player.teleportTo(level, x, y, z, yaw, pitch);
        //?}
    }

    /** Sends a player to their respawn point. */
    public static void respawn(ServerPlayer player) {
        //? if >=1.21.2 {
        /*player.teleport(player.findRespawnPositionAndUseSpawnBlock(
                true, net.minecraft.world.level.portal.TeleportTransition.DO_NOTHING));
        *///?} else {
        player.changeDimension(player.findRespawnPositionAndUseSpawnBlock(
                true, net.minecraft.world.level.portal.DimensionTransition.DO_NOTHING));
        //?}
    }

    /** The name of a player profile. */
    public static String name(GameProfile profile) {
        //? if >=1.21.9 {
        /*return profile.name();
        *///?} else {
        return profile.getName();
        //?}
    }

    /** The UUID of a player profile. */
    public static UUID id(GameProfile profile) {
        //? if >=1.21.9 {
        /*return profile.id();
        *///?} else {
        return profile.getId();
        //?}
    }

    /** The lowest y coordinate of a level. */
    public static int minY(LevelHeightAccessor level) {
        //? if >=1.21.2 {
        /*return level.getMinY();
        *///?} else {
        return level.getMinBuildHeight();
        //?}
    }

    /** The y coordinate above the highest block of a level. */
    public static int maxBuildHeight(LevelHeightAccessor level) {
        //? if >=1.21.2 {
        /*return level.getMaxY() + 1;
        *///?} else {
        return level.getMaxBuildHeight();
        //?}
    }

    /** Shows a message to the player: above the hotbar (overlay) or in the chat. */
    public static void message(net.minecraft.world.entity.player.Player player, Component message, boolean overlay) {
        //? if >=26.1 {
        /*if (overlay) {
            player.sendOverlayMessage(message);
        } else {
            player.sendSystemMessage(message);
        }
        *///?} else {
        player.displayClientMessage(message, overlay);
        //?}
    }

    /** Gives an item to a player, or drops it if the inventory is full. */
    public static void give(net.minecraft.world.entity.player.Player player, net.minecraft.world.item.ItemStack stack) {
        //? if >=26.3 {
        /*player.getInventory().placeItemBackInInventory(stack, net.minecraft.util.Prediction.SERVER_ONLY);
        *///?} else {
        player.getInventory().placeItemBackInInventory(stack);
        //?}
    }

    /** Removes items from the inventory and the crafting grid of a player. */
    public static void removeItems(
            net.minecraft.world.entity.player.Player player,
            java.util.function.Predicate<net.minecraft.world.item.ItemStack> items,
            int count) {
        //? if >=26.3 {
        /*player.getInventory().clearOrCountMatchingItems(items, false, count, player.inventoryMenu.getCraftSlots());
        *///?} else {
        player.getInventory().clearOrCountMatchingItems(items, count, player.inventoryMenu.getCraftSlots());
        //?}
    }

    /** The chunk of a position. */
    public static net.minecraft.world.level.ChunkPos chunk(net.minecraft.core.BlockPos pos) {
        //? if >=26.1 {
        /*return net.minecraft.world.level.ChunkPos.containing(pos);
        *///?} else {
        return new net.minecraft.world.level.ChunkPos(pos);
        //?}
    }

    /** The name of an item. */
    public static Component name(Item item) {
        //? if >=26.1 {
        /*return item.getName(item.getDefaultInstance());
        *///?} elif >=1.21.2 {
        /*return item.getName();
        *///?} else {
        return item.getDescription();
        //?}
    }

    /** The result of a block interaction with an item that succeeded (useItemOn). */
    //? if >=1.21.2 {
    /*public static InteractionResult itemSuccess(Level level) {
        return InteractionResult.SUCCESS;
    }
    *///?} else {
    public static net.minecraft.world.ItemInteractionResult itemSuccess(Level level) {
        return net.minecraft.world.ItemInteractionResult.sidedSuccess(level.isClientSide());
    }
    //?}

    /** The result of an interaction that succeeded, on the client and the server. */
    public static InteractionResult success(Level level) {
        //? if >=1.21.2 {
        /*return InteractionResult.SUCCESS;
        *///?} else {
        return InteractionResult.sidedSuccess(level.isClientSide());
        //?}
    }

    /** Loads the data of a block entity, including its components, from a tag. */
    public static void loadBlockEntity(
            net.minecraft.world.level.block.entity.BlockEntity entity,
            net.minecraft.nbt.CompoundTag tag,
            net.minecraft.core.HolderLookup.Provider registries) {
        //? if >=1.21.6 {
        /*entity.loadWithComponents(net.minecraft.world.level.storage.TagValueInput.create(
                net.minecraft.util.ProblemReporter.DISCARDING, registries, tag));
        *///?} else {
        entity.loadWithComponents(tag, registries);
        //?}
    }
}
