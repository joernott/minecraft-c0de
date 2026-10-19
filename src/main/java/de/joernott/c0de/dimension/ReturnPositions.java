package de.joernott.c0de.dimension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;

/**
 * The positions players had when they entered the c0de dimension from another dimension. Stored in
 * the save data of the overworld ({@code data/c0de_return_positions.dat}).
 */
public class ReturnPositions extends SavedData {
    private static final String NAME = "c0de_return_positions";

    // Vanilla (Fabric) can't load saved data without a data fixer type; NeoForge allows null. This type has
    // no fixes that would touch the data of this class.
    private static final DataFixTypes DATA_FIX_TYPE =
        /*? if fabric {*/DataFixTypes.SAVED_DATA_COMMAND_STORAGE;
        /*?} else *///null;

    private static final SavedData.Factory<ReturnPositions> FACTORY =
            new SavedData.Factory<>(ReturnPositions::new, ReturnPositions::load, DATA_FIX_TYPE);

    private final Map<UUID, Position> positions = new HashMap<>();

    /** The return positions of the server. */
    public static ReturnPositions get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, NAME);
    }

    /** Stores the current position of the player. */
    public void store(ServerPlayer player) {
        positions.put(
                player.getUUID(),
                new Position(player.level().dimension(), player.position(), player.getYRot(), player.getXRot()));
        setDirty();
    }

    /** Removes and returns the stored position of the player. */
    public Optional<Position> take(UUID player) {
        Position position = positions.remove(player);
        if (position != null) {
            setDirty();
        }
        return Optional.ofNullable(position);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag players = new CompoundTag();
        positions.forEach((uuid, position) -> {
            CompoundTag entry = new CompoundTag();
            entry.putString("dimension", position.dimension().location().toString());
            entry.putDouble("x", position.position().x);
            entry.putDouble("y", position.position().y);
            entry.putDouble("z", position.position().z);
            entry.putFloat("yaw", position.yaw());
            entry.putFloat("pitch", position.pitch());
            players.put(uuid.toString(), entry);
        });
        tag.put("players", players);
        return tag;
    }

    private static ReturnPositions load(CompoundTag tag, HolderLookup.Provider registries) {
        ReturnPositions result = new ReturnPositions();
        CompoundTag players = tag.getCompound("players");
        for (String key : players.getAllKeys()) {
            CompoundTag entry = players.getCompound(key);
            ResourceLocation dimension = ResourceLocation.tryParse(entry.getString("dimension"));
            if (dimension == null) {
                continue;
            }
            result.positions.put(
                    UUID.fromString(key),
                    new Position(
                            ResourceKey.create(Registries.DIMENSION, dimension),
                            new Vec3(entry.getDouble("x"), entry.getDouble("y"), entry.getDouble("z")),
                            entry.getFloat("yaw"),
                            entry.getFloat("pitch")));
        }
        return result;
    }

    /** A position in a dimension with the viewing direction. */
    public record Position(ResourceKey<Level> dimension, Vec3 position, float yaw, float pitch) {}
}
