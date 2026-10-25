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
 * The positions players had when they entered the c0de dimension from another dimension. Stored in
 * the save data of the overworld ({@code data/c0de_return_positions.dat}).
 */
public class ReturnPositions extends C0deSavedData {
    private static final String NAME = "c0de_return_positions";

    private static final C0deSavedData.Kind<ReturnPositions> KIND =
            new C0deSavedData.Kind<>(NAME, ReturnPositions::new, ReturnPositions::load);

    private final Map<UUID, Position> positions = new HashMap<>();

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
        CompoundTag players = Nbt.compound(tag, "players");
        for (String key : Nbt.keys(players)) {
            CompoundTag entry = Nbt.compound(players, key);
            ResourceLocation dimension = ResourceLocation.tryParse(Nbt.string(entry, "dimension"));
            if (dimension == null) {
                continue;
            }
            result.positions.put(
                    UUID.fromString(key),
                    new Position(
                            ResourceKey.create(Registries.DIMENSION, dimension),
                            new Vec3(Nbt.doubleValue(entry, "x"), Nbt.doubleValue(entry, "y"), Nbt.doubleValue(entry, "z")),
                            Nbt.floatValue(entry, "yaw"),
                            Nbt.floatValue(entry, "pitch")));
        }
        return result;
    }

    /** A position in a dimension with the viewing direction. */
    public record Position(ResourceKey<Level> dimension, Vec3 position, float yaw, float pitch) {}
}
