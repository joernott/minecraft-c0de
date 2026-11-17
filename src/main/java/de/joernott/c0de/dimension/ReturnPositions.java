package de.joernott.c0de.dimension;

import de.joernott.c0de.compat.C0deSavedData;
import de.joernott.c0de.compat.Nbt;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * The positions players had when they entered the c0de dimension from another dimension, and where they left the
 * c0de dimension the last time (task 18.1: the wrench at a trigger block without a program leads back there). Stored
 * in the save data of the overworld ({@code data/c0de_return_positions.dat}).
 */
public class ReturnPositions extends C0deSavedData {
    private static final String NAME = "c0de_return_positions";

    private static final C0deSavedData.Kind<ReturnPositions> KIND =
            new C0deSavedData.Kind<>(NAME, ReturnPositions::new, ReturnPositions::load);

    private final Map<UUID, Position> positions = new HashMap<>();
    private final Map<UUID, Position> left = new HashMap<>();

    /** The return positions of the server. */
    public static ReturnPositions get(MinecraftServer server) {
        return KIND.get(server.overworld().getDataStorage());
    }

    /** Stores the current position of the player. */
    public void store(ServerPlayer player) {
        positions.put(
                player.getUUID(),
                new Position(player.level().dimension(), player.position(), player.getYRot(), player.getXRot()));
        setDirty();
    }

    /** Stores where the player leaves the c0de dimension. */
    public void storeLeft(ServerPlayer player) {
        left.put(
                player.getUUID(),
                new Position(player.level().dimension(), player.position(), player.getYRot(), player.getXRot()));
        setDirty();
    }

    /** Where the player left the c0de dimension the last time, if they ever did. */
    public Optional<Position> left(UUID player) {
        return Optional.ofNullable(left.get(player));
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
        tag.put("players", write(positions));
        tag.put("left", write(left));
        return tag;
    }

    private static CompoundTag write(Map<UUID, Position> map) {
        CompoundTag players = new CompoundTag();
        map.forEach((uuid, position) -> {
            CompoundTag entry = new CompoundTag();
            entry.putString("dimension", position.dimension().location().toString());
            entry.putDouble("x", position.position().x);
            entry.putDouble("y", position.position().y);
            entry.putDouble("z", position.position().z);
            entry.putFloat("yaw", position.yaw());
            entry.putFloat("pitch", position.pitch());
            players.put(uuid.toString(), entry);
        });
        return players;
    }

    private static ReturnPositions load(CompoundTag tag, HolderLookup.Provider registries) {
        ReturnPositions result = new ReturnPositions();
        read(Nbt.compound(tag, "players"), result.positions);
        read(Nbt.compound(tag, "left"), result.left);
        return result;
    }

    private static void read(CompoundTag players, Map<UUID, Position> map) {
        for (String key : Nbt.keys(players)) {
            CompoundTag entry = Nbt.compound(players, key);
            ResourceLocation dimension = ResourceLocation.tryParse(Nbt.string(entry, "dimension"));
            if (dimension == null) {
                continue;
            }
            map.put(
                    UUID.fromString(key),
                    new Position(
                            ResourceKey.create(Registries.DIMENSION, dimension),
                            new Vec3(Nbt.doubleValue(entry, "x"), Nbt.doubleValue(entry, "y"), Nbt.doubleValue(entry, "z")),
                            Nbt.floatValue(entry, "yaw"),
                            Nbt.floatValue(entry, "pitch")));
        }
    }

    /** A position in a dimension with the viewing direction. */
    public record Position(ResourceKey<Level> dimension, Vec3 position, float yaw, float pitch) {}
}
